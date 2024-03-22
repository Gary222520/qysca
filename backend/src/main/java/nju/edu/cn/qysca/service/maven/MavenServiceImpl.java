package nju.edu.cn.qysca.service.maven;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.SpiderService;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class MavenServiceImpl implements MavenService {

    @Autowired
    private ComponentDao componentDao;


    @Autowired
    private SpiderService spiderService;


    private final String FILE_SEPARATOR = "/";

    @Value("${tempPomFolder}")
    private String tempFolder;


    /**
     * 解析组件
     *
     * @param filePath 上传文件路径
     * @param builder    构建器
     * @param type       组件类型
     * @return ComponentDO 组件信息
     */
    @Override
    public ComponentDO componentAnalysis(String filePath, String builder, String type) {
        String pomFilePath = analyzePomPath(filePath, builder);
        Model model = pomToModel(pomFilePath);
        ComponentDO componentDO = new ComponentDO();
        componentDO.setGroupId(getGroupIdFromPomModel(model));
        componentDO.setArtifactId(getArtifactIdFromPomModel(model));
        componentDO.setVersion(getVersionFromPomModel(model));
        componentDO.setLanguage("java");
        componentDO.setName(model.getName() == null ? "-" : model.getName());
        componentDO.setType(type);
        componentDO.setDescription(model.getDescription() == null ? "-" : model.getDescription());
        componentDO.setUrl(model.getUrl() == null ? "-" : model.getUrl());
        componentDO.setDownloadUrl(model.getDistributionManagement() == null ? "-" : model.getDistributionManagement().getDownloadUrl());
        componentDO.setSourceUrl(model.getScm() == null ? "-" : model.getScm().getUrl());
        componentDO.setPUrl(getMavenPUrl(componentDO.getGroupId(), model.getArtifactId(), model.getVersion(), model.getPackaging()));
        componentDO.setLicenses(getLicense(model));
        componentDO.setDevelopers(getDevelopers(model));
        //TODO: hash信息解析
        // 上传jar包才可以生成hash信息
        //javaCloseComponentDO.setHashes(getHashes(model));
        return componentDO;
    }

    /**
     * 解析依赖树
     * @param filePath 上传文件路径
     * @param builder 构建器
     * @param type 组件类型
     * @return DependencyTreeDO 依赖树信息
     */
    @Override
    public DependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String type) {
        String pomFilePath = analyzePomPath(filePath, builder);
        Model model = pomToModel(pomFilePath);
        DependencyTreeDO dependencyTreeDO = new DependencyTreeDO();
        dependencyTreeDO.setGroupId(getGroupIdFromPomModel(model));
        dependencyTreeDO.setArtifactId(getArtifactIdFromPomModel(model));
        dependencyTreeDO.setVersion(getVersionFromPomModel(model));
        Node node = mavenDependencyTreeAnalyzer(filePath, builder);
        ComponentDependencyTreeDO componentDependencyTreeDO = convertNode(node, 0);
        componentDependencyTreeDO.setType(type);
        dependencyTreeDO.setTree(componentDependencyTreeDO);
        return dependencyTreeDO;
    }

    /**
     * 根据依赖树信息生成依赖平铺信息
     * @param dependencyTreeDO 依赖树信息
     * @return List<DependencyTableDO> 依赖平铺信息表
     */
    @Override
    public List<DependencyTableDO> dependencyTableAnalysis(DependencyTreeDO dependencyTreeDO) {
        List<DependencyTableDO> closeDependencyTableDOList = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(dependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            DependencyTableDO dependencyTableDO = new DependencyTableDO();
            dependencyTableDO.setGroupId(dependencyTreeDO.getGroupId());
            dependencyTableDO.setArtifactId(dependencyTreeDO.getArtifactId());
            dependencyTableDO.setVersion(dependencyTreeDO.getVersion());
            ComponentDependencyTreeDO componentDependencyTree = queue.poll();
            dependencyTableDO.setCGroupId(componentDependencyTree.getGroupId());
            dependencyTableDO.setCArtifactId(componentDependencyTree.getArtifactId());
            dependencyTableDO.setCVersion(componentDependencyTree.getVersion());
            dependencyTableDO.setScope(componentDependencyTree.getScope());
            dependencyTableDO.setDepth(componentDependencyTree.getDepth());
            dependencyTableDO.setDirect(componentDependencyTree.getDepth() == 1);
            dependencyTableDO.setType(componentDependencyTree.getType());
            dependencyTableDO.setLanguage("java");
            queue.addAll(componentDependencyTree.getDependencies());
            closeDependencyTableDOList.add(dependencyTableDO);
        }
        return closeDependencyTableDOList;
    }

    /**
     * @param filePath pom文件路径
     * @param builder  构造工具
     * @return Node 封装好的依赖信息树
     * @throws Exception
     */
    public Node mavenDependencyTreeAnalyzer(String filePath, String builder) {
        try {
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File(System.getenv("MAVEN_HOME")));
            String resultPath = null;
            InvocationRequest request = new DefaultInvocationRequest();
            // 抽象出解析Pom文件路径的函数
            String pomFile = analyzePomPath(filePath, builder);
            File tempFile = new File(pomFile);
            request.setBaseDirectory(tempFile.getParentFile());
            resultPath = tempFile.getParent() + FILE_SEPARATOR + "result";
            request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
            invoker.execute(request);
            // 获得result结果的路径
            FileInputStream fis = new FileInputStream(new File(resultPath));
            Reader reader = new BufferedReader(new InputStreamReader(fis));
            InputType type = InputType.TEXT;
            Parser parser = type.newParser();
            Node node = parser.parse(reader);
            return node;
        }catch (Exception e) {
            throw new PlatformException(500, "pom文件解析失败");
        }
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

    /**
     * 解析pom文件路径
     *
     * @param filePath 上传文件路径
     * @param builder  构造器
     * @return String pom文件路径
     * @throws Exception
     */
    private String analyzePomPath(String filePath, String builder) {
        if (builder.equals("maven")) {
            return filePath;
        } else if (builder.equals("zip")) {
            unzip(filePath);
            return filePath.substring(0, filePath.lastIndexOf('/')) + FILE_SEPARATOR + "pom.xml";
        } else if (builder.equals("jar")) {
            File file = new File(filePath);
            int count = 0;
            try (ZipFile zipFile = new ZipFile(filePath)) {
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith("META-INF/maven") && (name.endsWith("pom.xml"))) {
                        count++;
                        try (InputStream inputStream = zipFile.getInputStream(entry);
                             FileOutputStream fos = new FileOutputStream(file.getParent() + FILE_SEPARATOR + "pom.xml")) {
                            byte[] buffer = new byte[1024];
                            int length;
                            while ((length = inputStream.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                throw new PlatformException(500, "解压jar文件失败");
            }
            if (count == 0) {
                throw new PlatformException(500, "No pom.xml found in the zip file");
            }
            return file.getParent() + FILE_SEPARATOR + "pom.xml";
        }
        return null;
    }


    @Override
    public DependencyTreeDO spiderDependency(String groupId, String artifactId, String version) {
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);
        String tempPomFolder = tempFolder + timeStamp;
        File file = new File(tempPomFolder);
        file.mkdirs();
        String tempPath = tempPomFolder + "/pom.xml";
        try {
            String xml = spiderService.getPomStrByGav(groupId, artifactId, version);
            FileWriter fileWriter = new FileWriter(tempPath);
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
            DependencyTreeDO dependencyTreeDO = dependencyTreeAnalysis(tempPath, "maven", "opensource");
            deleteFolder(tempPomFolder);
            return dependencyTreeDO;
        } catch (Exception e) {
            deleteFolder(tempPomFolder);
            throw new PlatformException(500, "识别依赖信息失败");
        }
    }

    /**
     * 根据POM文件路径返回Model
     *
     * @param pomPath POM文件路径
     */
    private Model pomToModel(String pomPath) {
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            Model model = reader.read(new FileReader(pomPath));
            return model;
        } catch (Exception e) {
            throw new PlatformException(500, "解析pom文件失败");
        }
    }

    /**
     * 从pom Model中获取groupId
     * @param model pom Model
     * @return groupId
     */
    private String getGroupIdFromPomModel(Model model){
        // ${xxx}形式的，在<property>中找
        if (model.getGroupId() != null){
            if (model.getGroupId().startsWith("${") && model.getGroupId().endsWith("}")){
                String propertyValue = model.getGroupId().substring(2,model.getGroupId().length()-1);
                return model.getProperties().getProperty(propertyValue);
            } else{
                return model.getGroupId();
            }
        }
        // 没写groupId的，默认为parent的groupId
        if (model.getParent().getGroupId() != null)
            return model.getParent().getGroupId();
        return null;
    }

    /**
     * 从pom Model中获取artifactId
     * @param model pom Model
     * @return artifactId
     */
    private String getArtifactIdFromPomModel(Model model){
        // ${xxx}形式的，在<property>中找
        if (model.getArtifactId() != null){
            if (model.getArtifactId().startsWith("${") && model.getArtifactId().endsWith("}")){
                String propertyValue = model.getArtifactId().substring(2,model.getArtifactId().length()-1);
                return model.getProperties().getProperty(propertyValue);
            } else{
                return model.getArtifactId();
            }
        }
        if (model.getParent().getArtifactId() != null)
            return model.getParent().getArtifactId();
        return null;
    }

    /**
     * 从pom Model中获取version
     * @param model pom Model
     * @return version
     */
    private String getVersionFromPomModel(Model model){
        // ${xxx}形式的，在<property>中找
        if (model.getVersion() != null){
            if (model.getVersion().startsWith("${") && model.getVersion().endsWith("}")){
                String propertyValue = model.getVersion().substring(2,model.getVersion().length()-1);
                return model.getProperties().getProperty(propertyValue);
            } else{
                return model.getVersion();
            }
        }
        if (model.getParent().getVersion() != null)
            return model.getParent().getVersion();
        return null;
    }

    /**
     * 生成PUrl（仅对maven组件）
     * 例如：pkg:maven/commons-codec/commons-codec@1.15?type=jar
     * @param groupId 组织Id
     * @param artifactId 工件id
     * @param version 版本号
     * @param packaging 打包方式，如pom、jar
     * @return PUrl
     */
    private static String getMavenPUrl(String groupId, String artifactId, String version, String packaging){
        String pUrl = "pkg:maven/" + groupId + "/" + artifactId + "@" + version;
        if (packaging.equals("jar"))
            pUrl += "?type=jar";
        return pUrl;
    }

    /**
     * 获得Model中的License信息
     *
     * @param model pom文件
     * @return List<LicenseDO> 许可证信息
     */
    private List<LicenseDO> getLicense(Model model) {
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        return mavenLicenses.stream()
                .map(mavenLicense -> {
                    LicenseDO license = new LicenseDO();
                    license.setName(mavenLicense.getName() == null ? "-" : mavenLicense.getName());
                    license.setUrl(mavenLicense.getUrl() == null ? "-" : mavenLicense.getUrl());
                    return license;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获得Model中的Developer信息
     *
     * @param model pom文件
     * @return List<DeveloperDO> Developer信息
     */
    private List<DeveloperDO> getDevelopers(Model model) {
        List<org.apache.maven.model.Developer> mavenDevelopers = model.getDevelopers();
        return mavenDevelopers.stream()
                .map(mavenDeveloper -> {
                    DeveloperDO developer = new DeveloperDO();
                    developer.setId(mavenDeveloper.getId() == null ? "-" : mavenDeveloper.getId());
                    developer.setName(mavenDeveloper.getName() == null ? "-" : mavenDeveloper.getName());
                    developer.setEmail(mavenDeveloper.getEmail() == null ? "-" : mavenDeveloper.getEmail());
                    return developer;
                })
                .collect(Collectors.toList());
    }


    /**
     * 解压zip文件
     *
     * @param filePath zip文件路径
     */
    private void unzip(String filePath) {
        try {
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
        } catch (Exception e) {
            throw new PlatformException(500, "解压zip文件失败");
        }
    }

    private void deleteFolder(String filePath) {
        File folder = new File(filePath);
        if (folder.exists()) {
            deleteFolderFile(folder);
        }
    }

    /**
     * 递归删除文件夹下的文件
     *
     * @param folder 文件夹
     */
    private void deleteFolderFile(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolderFile(file);
            }
            file.delete();
        }
        folder.delete();
    }
}
