package nju.edu.cn.qysca.service.maven;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.spider.JavaSpiderService;
import nju.edu.cn.qysca.utils.FolderUtil;
import nju.edu.cn.qysca.utils.ZipUtil;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class MavenServiceImpl implements MavenService {

    private final String FILE_SEPARATOR = "/";
    @Autowired
    private JavaComponentDao javaComponentDao;
    @Autowired
    private JavaSpiderService javaSpiderService;
    @Autowired
    private LicenseService licenseService;
    @Value("${tempPomFolder}")
    private String tempFolder;

    /**
     * 解析组件
     *
     * @param filePath 上传文件路径
     * @param builder  构建器
     * @param type     组件类型
     * @return JavaComponentDO 组件信息
     */
    @Override
    public JavaComponentDO componentAnalysis(String filePath, String builder, String type) {
        String pomFilePath = analyzePomPath(filePath, builder);
        Model model = pomToModel(pomFilePath);
        JavaComponentDO javaComponentDO = new JavaComponentDO();
        javaComponentDO.setName(getGroupIdFromPomModel(model) + ":" + getArtifactIdFromPomModel(model));
        javaComponentDO.setVersion(getVersionFromPomModel(model));
        javaComponentDO.setLanguage("java");
        javaComponentDO.setJName(model.getName() == null ? "-" : model.getName());
        javaComponentDO.setType(type);
        javaComponentDO.setDescription(model.getDescription() == null ? "-" : model.getDescription());
        javaComponentDO.setUrl(model.getUrl() == null ? "-" : model.getUrl());
        javaComponentDO.setDownloadUrl(model.getDistributionManagement() == null ? "-" : model.getDistributionManagement().getDownloadUrl());
        javaComponentDO.setSourceUrl(model.getScm() == null ? "-" : model.getScm().getUrl());
        javaComponentDO.setPUrl(getMavenPUrl(getGroupIdFromPomModel(model), getArtifactIdFromPomModel(model), model.getVersion(), model.getPackaging()));
        List<String> licenses = new ArrayList<>();
        for (String license : getLicense(model)) {
            licenses.addAll(licenseService.searchLicense(license));
        }
        javaComponentDO.setLicenses(licenses.toArray(new String[0]));
        javaComponentDO.setDevelopers(getDevelopers(model));
        //TODO: hash信息解析
        // 上传jar包才可以生成hash信息
        //javaCloseComponentDO.setHashes(getHashes(model));
        return javaComponentDO;
    }

    /**
     * 解析依赖树
     *
     * @param filePath 上传文件路径
     * @param builder  构建器
     * @param type     组件类型
     * @return JavaDependencyTreeDO 依赖树信息
     */
    @Override
    public JavaDependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String type) {
        String pomFilePath = analyzePomPath(filePath, builder);
        Model model = pomToModel(pomFilePath);
        JavaDependencyTreeDO javaDependencyTreeDO = new JavaDependencyTreeDO();
        javaDependencyTreeDO.setName(getGroupIdFromPomModel(model) + ":" + getArtifactIdFromPomModel(model));
        javaDependencyTreeDO.setVersion(getVersionFromPomModel(model));
        Node node = mavenDependencyTreeAnalyzer(filePath, builder);
        JavaComponentDependencyTreeDO javaComponentDependencyTreeDO = convertNode(node, 0);
        javaComponentDependencyTreeDO.setType(type);
        javaDependencyTreeDO.setTree(javaComponentDependencyTreeDO);
        return javaDependencyTreeDO;
    }

    /**
     * 根据依赖树信息生成依赖平铺信息
     *
     * @param javaDependencyTreeDO 依赖树信息
     * @return List<JavaDependencyTableDO> 依赖平铺信息表
     */
    @Override
    public List<JavaDependencyTableDO> dependencyTableAnalysis(JavaDependencyTreeDO javaDependencyTreeDO) {
        List<JavaDependencyTableDO> closeJavaDependencyTableDOList = new ArrayList<>();
        Queue<JavaComponentDependencyTreeDO> queue = new LinkedList<>(javaDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            JavaDependencyTableDO javaDependencyTableDO = new JavaDependencyTableDO();
            javaDependencyTableDO.setName(javaDependencyTreeDO.getName());
            javaDependencyTableDO.setVersion(javaDependencyTreeDO.getVersion());
            JavaComponentDependencyTreeDO componentDependencyTree = queue.poll();
            javaDependencyTableDO.setCName(componentDependencyTree.getName());
            javaDependencyTableDO.setCVersion(componentDependencyTree.getVersion());
            javaDependencyTableDO.setDepth(componentDependencyTree.getDepth());
            javaDependencyTableDO.setDirect(componentDependencyTree.getDepth() == 1);
            javaDependencyTableDO.setType(componentDependencyTree.getType());
            javaDependencyTableDO.setLanguage("java");
            javaDependencyTableDO.setLicenses(componentDependencyTree.getLicenses());
            javaDependencyTableDO.setVulnerabilities(componentDependencyTree.getVulnerabilities());
            queue.addAll(componentDependencyTree.getDependencies());
            closeJavaDependencyTableDOList.add(javaDependencyTableDO);
        }
        return closeJavaDependencyTableDOList;
    }

    @Override
    public JavaDependencyTreeDO spiderDependency(String groupId, String artifactId, String version) {
        // 先检查组件库中是否有对应信息，若无，爬取对应信息
        JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(groupId+":"+artifactId, version);
        if (null == javaComponentDO){
            javaComponentDO = javaSpiderService.crawlByGav(groupId, artifactId, version);
            if (null == javaComponentDO){
                throw new PlatformException(500, "无法识别的组件：" + groupId + ":" + artifactId + ":" + version);
            }
            javaComponentDao.save(javaComponentDO);
        }

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);
        File tempDir = new File(tempFolder, timeStamp);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        String tempPomPath = tempDir + FILE_SEPARATOR + "pom.xml";
        try (FileWriter fileWriter = new FileWriter(tempPomPath)){
            String xml = javaSpiderService.getPomStrByGav(groupId, artifactId, version);
            fileWriter.write(xml);
            fileWriter.flush();
            JavaDependencyTreeDO javaDependencyTreeDO = dependencyTreeAnalysis(tempPomPath, "maven", "opensource");
            return javaDependencyTreeDO;
        } catch (Exception e) {
            e.printStackTrace();
            throw new PlatformException(500, "识别依赖信息失败");
        } finally {
            FolderUtil.deleteFolder(tempDir.getPath());
        }
    }

    /**
     * 将JavaComponentDependencyTreeDO转换为AppComponentDependencyTreeDO
     *
     * @param javaComponentDependencyTreeDO Java组件依赖树信息
     * @return AppComponentDependencyTreeDO App组件依赖树信息
     */
    @Override
    public AppComponentDependencyTreeDO translateComponentDependency(JavaComponentDependencyTreeDO javaComponentDependencyTreeDO) {
        if (javaComponentDependencyTreeDO == null) {
            return null;
        }
        AppComponentDependencyTreeDO appComponentDependencyTreeDO = new AppComponentDependencyTreeDO();
        appComponentDependencyTreeDO.setName(javaComponentDependencyTreeDO.getName());
        appComponentDependencyTreeDO.setVersion(javaComponentDependencyTreeDO.getVersion());
        appComponentDependencyTreeDO.setType(javaComponentDependencyTreeDO.getType());
        appComponentDependencyTreeDO.setDepth(javaComponentDependencyTreeDO.getDepth());
        appComponentDependencyTreeDO.setLicenses(javaComponentDependencyTreeDO.getLicenses());
        appComponentDependencyTreeDO.setVulnerabilities(javaComponentDependencyTreeDO.getVulnerabilities());
        appComponentDependencyTreeDO.setLanguage("java");
        List<AppComponentDependencyTreeDO> dependencyTreeDOS = new ArrayList<>();
        for (JavaComponentDependencyTreeDO childJavaComponentDependencyTreeDO : javaComponentDependencyTreeDO.getDependencies()) {
            AppComponentDependencyTreeDO childAppComponentDependencyTreeDO = translateComponentDependency(childJavaComponentDependencyTreeDO);
            dependencyTreeDOS.add(childAppComponentDependencyTreeDO);
        }
        appComponentDependencyTreeDO.setDependencies(dependencyTreeDOS);
        return appComponentDependencyTreeDO;
    }

    /**
     * 将List<JavaDependencyTableDO>转化成List<AppDependencyTableDO>
     *
     * @param javaDependencyTableDOS java组件依赖关系平铺表
     * @return List<AppDependencyTableDO> 应用组件依赖关系平铺表
     */
    @Override
    public List<AppDependencyTableDO> translateDependencyTable(List<JavaDependencyTableDO> javaDependencyTableDOS) {
        List<AppDependencyTableDO> appDependencyTableDOS = new ArrayList<>();
        for (JavaDependencyTableDO javaDependencyTableDO : javaDependencyTableDOS) {
            AppDependencyTableDO appDependencyTableDO = new AppDependencyTableDO();
            BeanUtils.copyProperties(javaDependencyTableDO, appDependencyTableDO);
            appDependencyTableDOS.add(appDependencyTableDO);
        }
        return appDependencyTableDOS;
    }

    /**
     * @param filePath pom文件路径
     * @param builder  构造工具
     * @return Node 封装好的依赖信息树
     * @throws Exception
     */
    private Node mavenDependencyTreeAnalyzer(String filePath, String builder) {
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
            try( FileInputStream fis = new FileInputStream(new File(resultPath));
                 Reader reader = new BufferedReader(new InputStreamReader(fis))) {
                InputType type = InputType.TEXT;
                Parser parser = type.newParser();
                Node node = parser.parse(reader);
                return node;
            }
        } catch (Exception e) {
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
    private JavaComponentDependencyTreeDO convertNode(Node node, int depth) throws PlatformException {
        JavaComponentDependencyTreeDO javaComponentDependencyTreeDO = new JavaComponentDependencyTreeDO();
        String groupId = node.getGroupId() == null ? "-" : node.getGroupId();
        String artifactId = node.getArtifactId() == null ? "-" : node.getArtifactId();
        javaComponentDependencyTreeDO.setName(groupId + ":" + artifactId);
        javaComponentDependencyTreeDO.setVersion(node.getVersion() == null ? "-" : node.getVersion());
        if (depth != 0) {
            // 从知识库中查找
            JavaComponentDO javaComponentDO = null;
            javaComponentDO = javaComponentDao.findByNameAndVersion(node.getGroupId() + ":" + node.getArtifactId(), node.getVersion());

            // 如果知识库中没有则爬取
            if (javaComponentDO == null) {
                javaComponentDO = javaSpiderService.crawlByGav(node.getGroupId(), node.getArtifactId(), node.getVersion());
                if (javaComponentDO != null) {
                    try {
                        javaComponentDao.save(javaComponentDO);
                    } catch (Exception e){
                        // save组件时出现错误，跳过该组件，仍继续执行
                        e.printStackTrace();
                    }

                    javaComponentDependencyTreeDO.setLicenses(String.join(",", javaComponentDO.getLicenses()));
                    javaComponentDependencyTreeDO.setVulnerabilities(String.join(",", javaComponentDO.getVulnerabilities()));
                    javaComponentDependencyTreeDO.setType("opensource");
                } else {
                    javaComponentDependencyTreeDO.setType("opensource");
                    // 如果爬虫没有爬到则打印报错信息，仍继续执行
                    System.err.println("存在未识别的组件：" + javaComponentDependencyTreeDO.getName() + ":" + javaComponentDependencyTreeDO.getVersion());
//                    //如果爬虫没有爬到则扫描错误 通过抛出异常处理
//                    throw new PlatformException(500, "存在未识别的组件");
                }
            } else {
                javaComponentDependencyTreeDO.setLicenses(String.join(",", javaComponentDO.getLicenses()));
                javaComponentDependencyTreeDO.setVulnerabilities(String.join(",", javaComponentDO.getVulnerabilities()));
                javaComponentDependencyTreeDO.setType(javaComponentDO.getType());
            }
        }
        javaComponentDependencyTreeDO.setDepth(depth);
        for (Node child : node.getChildNodes()) {
            JavaComponentDependencyTreeDO childDependencyTreeDO = convertNode(child, depth + 1);
            javaComponentDependencyTreeDO.getDependencies().add(childDependencyTreeDO);
        }
        return javaComponentDependencyTreeDO;
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
            ZipUtil.unzip(filePath);
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

    /**
     * 根据POM文件路径返回Model
     *
     * @param pomPath POM文件路径
     */
    private Model pomToModel(String pomPath) {
        try (FileReader fileReader = new FileReader(pomPath)){
            MavenXpp3Reader reader = new MavenXpp3Reader();
            return reader.read(fileReader);
        } catch (Exception e) {
            throw new PlatformException(500, "解析pom文件失败");
        }
    }

    /**
     * 从pom Model中获取groupId
     *
     * @param model pom Model
     * @return groupId
     */
    private String getGroupIdFromPomModel(Model model) {
        // ${xxx}形式的，在<property>中找
        if (model.getGroupId() != null) {
            if (model.getGroupId().startsWith("${") && model.getGroupId().endsWith("}")) {
                String propertyValue = model.getGroupId().substring(2, model.getGroupId().length() - 1);
                return model.getProperties().getProperty(propertyValue);
            } else {
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
     *
     * @param model pom Model
     * @return artifactId
     */
    private String getArtifactIdFromPomModel(Model model) {
        // ${xxx}形式的，在<property>中找
        if (model.getArtifactId() != null) {
            if (model.getArtifactId().startsWith("${") && model.getArtifactId().endsWith("}")) {
                String propertyValue = model.getArtifactId().substring(2, model.getArtifactId().length() - 1);
                return model.getProperties().getProperty(propertyValue);
            } else {
                return model.getArtifactId();
            }
        }
        if (model.getParent().getArtifactId() != null)
            return model.getParent().getArtifactId();
        return null;
    }

    /**
     * 从pom Model中获取version
     *
     * @param model pom Model
     * @return version
     */
    private String getVersionFromPomModel(Model model) {
        // ${xxx}形式的，在<property>中找
        if (model.getVersion() != null) {
            if (model.getVersion().startsWith("${") && model.getVersion().endsWith("}")) {
                String propertyValue = model.getVersion().substring(2, model.getVersion().length() - 1);
                return model.getProperties().getProperty(propertyValue);
            } else {
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
     *
     * @param groupId    组织Id
     * @param artifactId 工件id
     * @param version    版本号
     * @param packaging  打包方式，如pom、jar
     * @return PUrl
     */
    private static String getMavenPUrl(String groupId, String artifactId, String version, String packaging) {
        String pUrl = "pkg:maven/" + groupId + "/" + artifactId + "@" + version;
        if (packaging.equals("jar"))
            pUrl += "?type=jar";
        return pUrl;
    }

    /**
     * 获得Model中的License信息
     *
     * @param model pom文件
     * @return List<String> 许可证信息
     */
    private List<String> getLicense(Model model) {
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        List<String> result = new ArrayList<>();
        for (org.apache.maven.model.License license : mavenLicenses) {
            result.add(license.getName());
        }
        return result;
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

}
