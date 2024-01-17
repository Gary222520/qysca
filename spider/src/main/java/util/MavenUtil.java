package util;

import dataAccess.MongoDBAccess;
import domain.component.*;
import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import org.apache.maven.shared.invoker.*;
import org.springframework.beans.BeanUtils;
import spider.JavaOpenPomSpider;
import util.idGenerator.UUIDGenerator;

import java.io.*;
import java.util.*;

public class MavenUtil {

    /**
     * 根据pom文件路径在pom文件路径下生成result.txt文件，文件中记录了pom文件中所有依赖的树形结构
     * 调用 maven dependency:tree命令
     * Maven_HOME
     *
     * @param filePath
     */
    public static Node mavenDependencyTreeAnalyzer(String filePath) {
        InvocationRequest request = new DefaultInvocationRequest();
        File pom = new File(filePath);
        request.setPomFile(pom);
        request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
        Invoker invoker = new DefaultInvoker();
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null && !mavenHome.isEmpty()) {
            // 使用环境变量中的MAVEN_HOME路径
            invoker.setMavenHome(new File(mavenHome));
        } else {
            System.err.println("MAVEN_HOME is not set in the environment variables.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            // 设置InvocationOutputHandler为捕获输出，使得调用mvn dependency:tree后的输出重定向（不输出到终端）
            InvocationOutputHandler outputHandler = new PrintStreamHandler(printStream, true);
            invoker.setOutputHandler(outputHandler);

            invoker.execute(request);

            // 获得 result 结果的路径
            FileInputStream fis = new FileInputStream(new File(pom.getParent() + File.separator + "result"));
            Reader reader = new BufferedReader(new InputStreamReader(fis));
            InputType type = InputType.TEXT;
            Parser parser = type.newParser();
            Node node = parser.parse(reader);
            return node;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 递归解析依赖树 返回根节点
     *
     * @param node
     * @param depth
     * @return
     */
    public ComponentDependencyTreeDO convertNode(Node node, int depth) {

        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setGroupId(node.getGroupId());
        componentDependencyTreeDO.setArtifactId(node.getArtifactId());
        componentDependencyTreeDO.setVersion(node.getVersion());
        componentDependencyTreeDO.setScope(node.getScope());
        componentDependencyTreeDO.setLanguage("Java");

        if (depth != 0) {
            // 从开源知识库中查找
            JavaOpenComponentDO javaOpenComponentDO = null;
            JavaCloseComponentDO javaCloseComponentDO = null;

            MongoDBAccess<JavaOpenComponentDO> javaOpenComponentDOMongoDBAccess = MongoDBAccess.getInstance("java_component_open_detail", JavaOpenComponentDO.class);
            javaOpenComponentDO = javaOpenComponentDOMongoDBAccess.readByGAV(node.getGroupId(), node.getArtifactId(), node.getVersion());
            // 如果开源知识库中没有则查看闭源知识库
            if (javaOpenComponentDO == null) {
                MongoDBAccess<JavaCloseComponentDO> javaCloseComponentDOMongoDBAccess = MongoDBAccess.getInstance("java_component_close_detail", JavaCloseComponentDO.class);
                javaCloseComponentDO = javaCloseComponentDOMongoDBAccess.readByGAV(node.getGroupId(), node.getArtifactId(), node.getVersion());
                // 如果闭源知识库中没有则爬取 爬取过程已经包含插入数据库的步骤
                if (javaCloseComponentDO == null) {
                    javaOpenComponentDO = JavaOpenPomSpider.getInstance().crawlByGav(node.getGroupId(), node.getArtifactId(), node.getVersion());
                }
                //如果爬虫没有爬到则扫描错误 通过抛出异常处理
            }

            //设置依赖树中的信息
            if (javaOpenComponentDO != null) {
                componentDependencyTreeDO.setName(javaOpenComponentDO.getName());
                componentDependencyTreeDO.setOpensource(true);
                List<LicenseDO> licenses = javaOpenComponentDO.getLicenses();
                StringBuilder license = new StringBuilder();
                for (LicenseDO licenseDO : licenses) {
                    license.append(licenseDO.getLicenseName()).append(";");
                }
                componentDependencyTreeDO.setLicenses(license.toString());
            } else {
                componentDependencyTreeDO.setName(javaCloseComponentDO.getName());
                componentDependencyTreeDO.setOpensource(false);
                List<LicenseDO> licenses = javaCloseComponentDO.getLicenses();
                StringBuilder license = new StringBuilder();
                for (LicenseDO licenseDO : licenses) {
                    license.append(licenseDO.getLicenseName()).append(";");
                }
                componentDependencyTreeDO.setLicenses(license.toString());
            }
            componentDependencyTreeDO.setDirect(depth == 1);
        }

        componentDependencyTreeDO.setDepth(depth);

        // 递归处理子节点
        for (Node child : node.getChildNodes()) {
            ComponentDependencyTreeDO childDependencyTreeDO = convertNode(child, depth + 1);
            componentDependencyTreeDO.getDependencies().add(childDependencyTreeDO);
        }

        return componentDependencyTreeDO;
    }

    public static List<JavaOpenDependencyTableDO> buildJavaOpenDependencyTable(JavaOpenDependencyTreeDO javaOpenDependencyTreeDO) {
        List<JavaOpenDependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(javaOpenDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            JavaOpenDependencyTableDO javaOpenDependencyTableDO = new JavaOpenDependencyTableDO();
            javaOpenDependencyTableDO.setId(UUIDGenerator.getUUID());
            javaOpenDependencyTableDO.setParentGroupId(javaOpenDependencyTreeDO.getGroupId());
            javaOpenDependencyTableDO.setParentArtifactId(javaOpenDependencyTreeDO.getArtifactId());
            javaOpenDependencyTableDO.setParentVersion(javaOpenDependencyTreeDO.getVersion());

            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
            BeanUtils.copyProperties(componentDependencyTreeDO, javaOpenDependencyTableDO);
            result.add(javaOpenDependencyTableDO);
            queue.addAll(componentDependencyTreeDO.getDependencies());
        }

        return result;
    }
}

