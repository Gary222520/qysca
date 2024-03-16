package nju.edu.cn.qysca.service.maven;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.SpiderService;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class MavenServiceImpl implements MavenService {

    @Autowired
    private ComponentDao componentDao;


    @Autowired
    private SpiderService spiderService;

    private String FILE_SEPARATOR = "/";

    /**
     * 解析pom文件
     *
     * @param filePath
     */
    @Override
    public ComponentDependencyTreeDO applicationDependencyAnalysis(String filePath, String builder, int flag) throws Exception {
        Node node = mavenDependencyTreeAnalyzer(filePath, builder, flag);
        ComponentDependencyTreeDO componentDependencyTreeDO = convertNode(node, 0);
        return componentDependencyTreeDO;
    }

    /**
     * @param filePath 文件路径
     * @param builder  构造工具
     * @param flag     0 应用 1 闭源组件
     * @return Node 封装好的依赖信息树
     * @throws Exception
     */
    public Node mavenDependencyTreeAnalyzer(String filePath, String builder, int flag) throws Exception {
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
        String resultPath = null;
        InvocationRequest request = new DefaultInvocationRequest();
        // result文件在文件夹下
        if (builder.equals("maven")) {
            File pom = new File(filePath);
            request.setPomFile(pom);
            resultPath = pom.getParent() + FILE_SEPARATOR + "result";
        } else if (builder.equals("jar")) {
            //jar包实现方式暂为从jar包中解析出pom文件
            File file = new File(filePath);
            request.setBaseDirectory(file.getParentFile());
            resultPath = file.getParent() + FILE_SEPARATOR + "result";
            int count  = 0;
            try (ZipFile zipFile = new ZipFile(filePath)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith("META-INF/maven") && (name.endsWith("pom.xml"))) {
                        count++;
                        try (InputStream inputStream = zipFile.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(file.getParentFile() + FILE_SEPARATOR + "pom.xml")) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                }
            }
            if(count == 0) {
                throw new PlatformException(500, "No pom.xml found in the zip file");
            }
        } else if (builder.equals("zip")) {
            unzip(filePath);
            File file = new File(filePath.substring(0, filePath.lastIndexOf('/')));
            request.setBaseDirectory(file);
            resultPath = file.getPath() + FILE_SEPARATOR + "result";
        }
        request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
        invoker.execute(request);
        if (flag == 1) {
            request.setGoals(Collections.singletonList("install"));
            invoker.execute(request);
        }
        // 获得result结果的路径
        FileInputStream fis = new FileInputStream(new File(resultPath));
        Reader reader = new BufferedReader(new InputStreamReader(fis));
        InputType type = InputType.TEXT;
        Parser parser = type.newParser();
        Node node = parser.parse(reader);
        return node;
    }

    /**
     * 递归解析依赖树 返回根节点
     *
     * @param node
     * @param depth
     * @return
     * @throws PlatformException
     */
    private ComponentDependencyTreeDO convertNode(Node node, int depth) throws PlatformException {
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
                    componentDao.save(componentDO);
                    componentDependencyTreeDO.setType("opensource");
                } else {
                    componentDependencyTreeDO.setType("opensource");
                    //如果爬虫没有爬到则扫描错误 通过抛出异常处理
                    throw new PlatformException(500, "存在未识别的组件");
                }
            } else {
                componentDependencyTreeDO.setType(componentDO.getType());
            }
        }
        componentDependencyTreeDO.setDepth(depth);
        for (Node child : node.getChildNodes()) {
            ComponentDependencyTreeDO childDependencyTreeDO = convertNode(child, depth + 1);
            componentDependencyTreeDO.getDependencies().add(childDependencyTreeDO);
        }
        return componentDependencyTreeDO;
    }

    private void unzip(String filePath) throws Exception {
        File file = new File(filePath);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ZipFile zipFile = new ZipFile(filePath, Charset.forName("GBK"));
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            String entryName = zipEntry.getName();
            String fileDestPath = dir + FILE_SEPARATOR + entryName;
            if (!zipEntry.isDirectory()) {
                File destFile = new File(fileDestPath);
                destFile.getParentFile().mkdirs();
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                FileOutputStream outputStream = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
            } else {
                File dirToCreate = new File(fileDestPath);
                dirToCreate.mkdirs();
            }
        }
        zipFile.close();
    }
}
