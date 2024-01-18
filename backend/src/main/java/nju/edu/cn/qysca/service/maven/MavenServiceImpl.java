package nju.edu.cn.qysca.service.maven;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import nju.edu.cn.qysca.dao.component.JavaCloseComponentDao;
import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
import nju.edu.cn.qysca.dao.project.ProjectDependencyTableDao;
import nju.edu.cn.qysca.domain.component.dos.JavaCloseComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaOpenComponentDO;
import nju.edu.cn.qysca.domain.component.dos.LicenseDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDependencyTableDO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.SpiderService;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class MavenServiceImpl implements MavenService {

    @Autowired
    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private JavaCloseComponentDao javaCloseComponentDao;


    @Autowired
    private SpiderService spiderService;

    private String FILE_SEPARATOR = "/";

    /**
     * 解析pom文件
     *
     * @param filePath
     */
    @Override
    public ComponentDependencyTreeDO projectDependencyAnalysis(String filePath, String builder) throws Exception {
        Node node = mavenDependencyTreeAnalyzer(filePath, builder);
        ComponentDependencyTreeDO componentDependencyTreeDO = convertNode(node, 0);
        return componentDependencyTreeDO;
    }

    /**
     * @param filePath 文件路径
     * @param builder  构造工具
     * @return Node 封装好的依赖信息树
     * @throws Exception
     */
    public Node mavenDependencyTreeAnalyzer(String filePath, String builder) throws Exception {
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
            //jar包不能这样实现
            File file = new File(filePath);
            request.setBaseDirectory(file.getParentFile());
            request.setGoals(Collections.singletonList("clean install:install " + filePath));
            invoker.execute(request);
            resultPath = file.getParent() + FILE_SEPARATOR + "result";
        } else if (builder.equals("zip")) {
            unzip(filePath);
            File file = new File(filePath.substring(0, filePath.lastIndexOf('/')));
            request.setBaseDirectory(file);
            resultPath = file.getPath() + FILE_SEPARATOR + "result";
        }
        request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
        invoker.execute(request);
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
        componentDependencyTreeDO.setGroupId(node.getGroupId() == null ? "-":node.getGroupId());
        componentDependencyTreeDO.setArtifactId(node.getArtifactId() == null ? "-":node.getArtifactId());
        componentDependencyTreeDO.setVersion(node.getVersion() == null ? "-":node.getVersion());
        componentDependencyTreeDO.setScope(node.getScope() == null ?"-":node.getScope());
        componentDependencyTreeDO.setLanguage("java");
        if (depth != 0) {
            // 从开源知识库中查找
            JavaOpenComponentDO javaOpenComponentDO = null;
            JavaCloseComponentDO javaCloseComponentDO = null;
            javaOpenComponentDO = javaOpenComponentDao.findByGroupIdAndArtifactIdAndVersion(node.getGroupId(), node.getArtifactId(), node.getVersion());
            // 如果开源知识库中没有则查看闭源知识库
            if (javaOpenComponentDO == null) {
                javaCloseComponentDO = javaCloseComponentDao.findByGroupIdAndArtifactIdAndVersion(node.getGroupId(), node.getArtifactId(), node.getVersion());
                // 如果闭源知识库中没有则爬取 爬取过程已经包含插入数据库的步骤
                if (javaCloseComponentDO == null) {
                    javaOpenComponentDO = spiderService.crawlByGav(node.getGroupId(), node.getArtifactId(), node.getVersion());
                    //如果不为null 插入数据库
                    if(javaOpenComponentDO != null) {
                        javaOpenComponentDO.setId(UUIDGenerator.getUUID());
                        javaOpenComponentDO.setLanguage("java");
                        javaOpenComponentDao.insert(javaOpenComponentDO);
                    }
                }
                //如果爬虫没有爬到则扫描错误 通过抛出异常处理
                if (javaOpenComponentDO == null && javaCloseComponentDO == null) {
                    throw new PlatformException(500, "扫描失败");
                }
            }
            //设置知识库中的信息
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
        ZipFile zipFile = new ZipFile(filePath);
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
