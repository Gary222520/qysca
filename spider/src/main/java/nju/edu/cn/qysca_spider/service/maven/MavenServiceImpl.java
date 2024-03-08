package nju.edu.cn.qysca_spider.service.maven;

import nju.edu.cn.qysca_spider.dao.ComponentDao;
import nju.edu.cn.qysca_spider.domain.*;
import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import nju.edu.cn.qysca_spider.service.spider.JavaSpiderService;
import nju.edu.cn.qysca_spider.utils.idGenerator.UUIDGenerator;
import org.apache.maven.shared.invoker.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class MavenServiceImpl implements MavenService{

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private JavaSpiderService spiderService;

    /**
     * 临时生成的pom文件地址
     */
    public final static String POM_FILE_TEMP_PATH = "spider/src/main/resources/temp_pom.xml";

    private static String FILE_SEPARATOR = "/";

    /**
     * 创建临时的pom文件（用以调用mvn命令）
     *
     * @param pomString
     */
    public void createPomFile(String pomString) {
        try (OutputStream outputStream = new FileOutputStream(POM_FILE_TEMP_PATH);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(pomString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param filePath 文件路径
     * @return Node 封装好的依赖信息树
     */
    public Node mavenDependencyTreeAnalyzer(String filePath) {
        try {
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));

            String resultPath = null;
            InvocationRequest request = new DefaultInvocationRequest();
            File pom = new File(filePath);
            request.setPomFile(pom);
            resultPath = pom.getParent() + FILE_SEPARATOR + "result";
            request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));

            // 设置InvocationOutputHandler为捕获输出，使得调用mvn dependency:tree后的输出重定向（不输出到终端）
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);
            InvocationOutputHandler outputHandler = new PrintStreamHandler(printStream, true);
            invoker.setOutputHandler(outputHandler);

            // 执行命令
            invoker.execute(request);

            // 获得result结果的路径
            FileInputStream fis = new FileInputStream(new File(resultPath));
            Reader reader = new BufferedReader(new InputStreamReader(fis));
            InputType type = InputType.TEXT;
            Parser parser = type.newParser();
            Node node = parser.parse(reader);
            return node;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 递归解析依赖树 返回根节点
     *
     * @param node 封装好的依赖信息树
     * @param depth 深度
     * @return
     */
    public ComponentDependencyTreeDO convertNode(Node node, int depth){
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setGroupId(node.getGroupId() == null ? "-" : node.getGroupId());
        componentDependencyTreeDO.setArtifactId(node.getArtifactId() == null ? "-" : node.getArtifactId());
        componentDependencyTreeDO.setVersion(node.getVersion() == null ? "-" : node.getVersion());
        componentDependencyTreeDO.setScope(node.getScope() == null ? "-" : node.getScope());
        if (depth != 0) {
            // 从知识库中查找
            ComponentDO componentDO = null;
            componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(node.getGroupId(), node.getArtifactId(), node.getVersion());

            // 如果知识库中没有则爬取
            if (componentDO == null) {
                componentDO = spiderService.crawlByGav(node.getGroupId(), node.getArtifactId(), node.getVersion());
                if (componentDO != null) {
                    componentDependencyTreeDO.setOpensource(true);
                } else {
                    //如果爬虫没有爬到则扫描错误 报错
                    System.err.println("未识别的组件：" + node.getGroupId() + ":" + node.getArtifactId() + ":" + node.getVersion());
                }
            } else{
                System.out.println("    组件已被爬取: " + componentDO.getDownloadUrl());
                componentDependencyTreeDO.setOpensource(componentDO.getOpensource());
            }
        }
        componentDependencyTreeDO.setDepth(depth);
        for (Node child : node.getChildNodes()) {
            ComponentDependencyTreeDO childDependencyTreeDO = convertNode(child, depth + 1);
            componentDependencyTreeDO.getDependencies().add(childDependencyTreeDO);
        }
        return componentDependencyTreeDO;
    }


    /**
     * 通过JavaOpenDependencyTreeDO构建JavaOpenDependencyTableDO对象
     *
     * @param dependencyTreeDO 依赖树DO
     * @return List<DependencyTableDO> 平铺依赖表DO列表
     */
    public List<DependencyTableDO> buildDependencyTable(DependencyTreeDO dependencyTreeDO) {
        List<DependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(dependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            DependencyTableDO dependencyTableDO = new DependencyTableDO();
            dependencyTableDO.setId(UUIDGenerator.getUUID());
            // 设置依赖表DO的gav属性，表示这张表是属于这个组件的平铺依赖表
            dependencyTableDO.setGroupId(dependencyTreeDO.getGroupId());
            dependencyTableDO.setArtifactId(dependencyTreeDO.getArtifactId());
            dependencyTableDO.setVersion(dependencyTreeDO.getVersion());

            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());

            // 设置依赖表DO的其他属性
            dependencyTableDO.setCGroupId(componentDependencyTreeDO.getGroupId());
            dependencyTableDO.setCArtifactId(componentDependencyTreeDO.getArtifactId());
            dependencyTableDO.setCVersion(componentDependencyTreeDO.getVersion());
            dependencyTableDO.setDepth(componentDependencyTreeDO.getDepth());
            dependencyTableDO.setScope(componentDependencyTreeDO.getScope());
            dependencyTableDO.setOpensource(componentDependencyTreeDO.getOpensource());
            dependencyTableDO.setLanguage("java");
            dependencyTableDO.setDirect(componentDependencyTreeDO.getDepth()==1);

            result.add(dependencyTableDO);
            queue.addAll(componentDependencyTreeDO.getDependencies());
        }

        return result;
    }
}

