package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.domain.component.*;
import nju.edu.cn.qysca.domain.project.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.utils.ZipUtil;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ComponentServiceImpl implements ComponentService {
    @Autowired
    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private JavaCloseComponentDao javaCloseComponentDao;

    @Autowired
    private JavaOpenDependencyTreeDao javaOpenDependencyTreeDao;

    @Autowired
    private JavaCloseDependencyTreeDao javaCloseDependencyTreeDao;

    @Autowired
    private JavaOpenDependencyTableDao javaOpenDependencyTableDao;

    @Autowired
    private JavaCloseDependencyTableDao javaCloseDependencyTableDao;

    @Autowired
    private MavenService mavenService;

    /**
     * 分页查询开源组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaOpenComponentDO> 查询结果
     */
    public Page<JavaOpenComponentDO> findOpenComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置查询条件
        JavaOpenComponentDO searcher = new JavaOpenComponentDO();
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("") ? null : searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("") ? null : searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("") ? null : searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "description", "url", "downloadUrl", "sourceUrl", "developers", "licenses", "pom")
                .withIgnoreNullValues();
        Example<JavaOpenComponentDO> example = Example.of(searcher, matcher);
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "language"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        return javaOpenComponentDao.findAll(example, pageable);
    }

    /**
     * 分页查询闭源组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaCloseComponentDO> 查询结果
     */
    @Override
    public Page<JavaCloseComponentDO> findCloseComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置查询条件
        JavaCloseComponentDO searcher = new JavaCloseComponentDO();
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("") ? null : searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("") ? null : searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("") ? null : searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "description", "url", "downloadUrl", "sourceUrl", "developers", "licenses", "pom")
                .withIgnoreNullValues();
        Example<JavaCloseComponentDO> example = Example.of(searcher, matcher);
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "language"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        return javaCloseComponentDao.findAll(example, pageable);
    }

    /**
     * 存储闭源组件信息
     *
     * @param saveCloseComponentDTO 保存闭源组件接口信息
     * @return 存储闭源组件信息
     */
    @Override
    public Boolean saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO) {
        // 接口获得详细信息
        try {
            Model model = null;
            MavenXpp3Reader reader = new MavenXpp3Reader();
            if(saveCloseComponentDTO.getBuilder().equals("zip")){
                unzip(saveCloseComponentDTO.getFilePath());
                File file = new File(saveCloseComponentDTO.getFilePath());
                model = reader.read(new FileReader(file.getParent() + "/pom.xml"));
            } else if (saveCloseComponentDTO.getBuilder().equals("maven")) {
                model = reader.read(new FileReader(saveCloseComponentDTO.getFilePath()));
            }
            JavaCloseComponentDO javaCloseComponentDO = createJavaCloseComponentDO(model, saveCloseComponentDTO.getLanguage(), saveCloseComponentDTO.getFilePath());
            // 存储闭源组件详细信息
            javaCloseComponentDao.save(javaCloseComponentDO);
            //存储闭源组件树状依赖信息
            JavaCloseDependencyTreeDO javaCloseDependencyTreeDO = createJavaCloseDependencyTreeDO(model, saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder());
            javaCloseDependencyTreeDao.save(javaCloseDependencyTreeDO);
            //存储闭源组件平铺依赖信息
            List<JavaCloseDependencyTableDO> javaCloseDependencyTableDOList = createJavaCloseDependencyTableDO(javaCloseDependencyTreeDO.getTree(), model.getGroupId(), model.getArtifactId(), model.getVersion());
            javaCloseDependencyTableDao.saveAll(javaCloseDependencyTableDOList);
        } catch (Exception e) {
            throw new PlatformException("存储闭源组件信息失败", e);
        }
        return true;
    }


    /**
     * 查询开源组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaOpenDependencyTreeDO 依赖树信息
     */
    @Override
    public JavaOpenDependencyTreeDO findOpenComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        return javaOpenDependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
    }

    /**
     * 查询闭源组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaCloseDependencyTreeDO 依赖树信息
     */
    @Override
    public JavaCloseDependencyTreeDO findCloseComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        return javaCloseDependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
    }

    /**
     * 分页查询开源组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<JavaOpenDependencyTableDO> 分页查询结果
     */
    @Override
    public Page<JavaOpenDependencyTableDO> findOpenComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        return javaOpenDependencyTableDao.findByParentGroupIdAndParentArtifactIdAndParentVersion(
                componentGavPageDTO.getGroupId(),
                componentGavPageDTO.getArtifactId(),
                componentGavPageDTO.getVersion(), pageable);
    }

    /**
     * 分页查询闭源组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<JavaCloseDependencyTableDO> 分页查询结果
     */
    @Override
    public Page<JavaCloseDependencyTableDO> findCloseComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        return javaCloseDependencyTableDao.findByParentGroupIdAndParentArtifactIdAndParentVersion(
                componentGavPageDTO.getGroupId(),
                componentGavPageDTO.getArtifactId(),
                componentGavPageDTO.getVersion(), pageable);
    }

    /**
     * 查询指定开源组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaOpenComponentDO 开源组件详细信息
     */
    @Override
    public JavaOpenComponentDO findOpenComponentDetail(ComponentGavDTO componentGavDTO) {
        return javaOpenComponentDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
    }

    /**
     * 查询指定闭源组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaCloseComponentDO 闭源组件详细信息
     */
    @Override
    public JavaCloseComponentDO findCloseComponentDetail(ComponentGavDTO componentGavDTO) {
        return javaCloseComponentDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
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
                    developer.setDeveloperId(mavenDeveloper.getId());
                    developer.setDeveloperName(mavenDeveloper.getName());
                    developer.setDeveloperEmail(mavenDeveloper.getEmail());
                    return developer;
                })
                .collect(Collectors.toList());
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
                    license.setLicenseName(mavenLicense.getName());
                    license.setLicenseUrl(mavenLicense.getUrl());
                    return license;
                })
                .collect(Collectors.toList());
    }

    /**
     * 根据pom文件生成闭源组件详细信息
     *
     * @param model    pom文件信息
     * @param language 组件语言
     * @param filePath pom文件路径
     * @return JavaCloseComponentDO 闭源组件详细信息
     */
    private JavaCloseComponentDO createJavaCloseComponentDO(Model model, String language, String filePath) {
        JavaCloseComponentDO javaCloseComponentDO = new JavaCloseComponentDO();
        javaCloseComponentDO.setId(UUIDGenerator.getUUID());
        javaCloseComponentDO.setLanguage(language);
        javaCloseComponentDO.setName(model.getName());
        javaCloseComponentDO.setGroupId(model.getGroupId());
        javaCloseComponentDO.setArtifactId(model.getArtifactId());
        javaCloseComponentDO.setVersion(model.getVersion());
        javaCloseComponentDO.setDescription(model.getDescription());
        javaCloseComponentDO.setUrl(model.getUrl());
        javaCloseComponentDO.setDownloadUrl(model.getDistributionManagement().getDownloadUrl());
        javaCloseComponentDO.setSourceUrl(model.getScm().getUrl());
        javaCloseComponentDO.setLicenses(getLicense(model));
        javaCloseComponentDO.setDevelopers(getDevelopers(model));
        javaCloseComponentDO.setPom(filePath);
        return javaCloseComponentDO;
    }

    /**
     * 根据pom文件生成依赖树信息
     *
     * @param model    pom文件
     * @param filePath pom文件路径
     * @param builder  构建工具
     * @return JavaCloseDependencyTreeDO 闭源组件依赖树信息
     * @throws Exception
     */
    private JavaCloseDependencyTreeDO createJavaCloseDependencyTreeDO(Model model, String filePath, String builder) throws Exception {
        JavaCloseDependencyTreeDO javaCloseDependencyTreeDO = new JavaCloseDependencyTreeDO();
        javaCloseDependencyTreeDO.setId(UUIDGenerator.getUUID());
        javaCloseDependencyTreeDO.setGroupId(model.getGroupId());
        javaCloseDependencyTreeDO.setArtifactId(model.getArtifactId());
        javaCloseDependencyTreeDO.setVersion(model.getVersion());
        ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(filePath, builder);
        javaCloseDependencyTreeDO.setTree(componentDependencyTreeDO);
        return javaCloseDependencyTreeDO;
    }

    /**
     * 根据组件依赖树信息获得平铺组件信息
     *
     * @param componentDependencyTreeDO
     * @param groupId
     * @param artifactId
     * @param version
     * @return List<JavaCloseDependencyTableDO> 组件平铺依赖列表
     */
    private List<JavaCloseDependencyTableDO> createJavaCloseDependencyTableDO(ComponentDependencyTreeDO componentDependencyTreeDO, String groupId, String artifactId, String version) {
        List<JavaCloseDependencyTableDO> javaCloseDependencyTableDOList = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(componentDependencyTreeDO.getDependencies());
        while (!queue.isEmpty()) {
            JavaCloseDependencyTableDO javaCloseDependencyTableDO = new JavaCloseDependencyTableDO();
            javaCloseDependencyTableDO.setId(UUIDGenerator.getUUID());
            javaCloseDependencyTableDO.setParentGroupId(groupId);
            javaCloseDependencyTableDO.setParentArtifactId(artifactId);
            javaCloseDependencyTableDO.setParentVersion(version);
            ComponentDependencyTreeDO componentDependencyTree = queue.poll();
            BeanUtils.copyProperties(componentDependencyTree, javaCloseDependencyTableDO);
            queue.addAll(componentDependencyTree.getDependencies());
            javaCloseDependencyTableDOList.add(javaCloseDependencyTableDO);
        }
        return javaCloseDependencyTableDOList;
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
            String fileDestPath = dir + "/" + entryName;
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
