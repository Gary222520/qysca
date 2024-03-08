package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.service.spider.SpiderService;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.BeanUtils;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class ComponentServiceImpl implements ComponentService {

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private DependencyTreeDao dependencyTreeDao;

    @Autowired
    private DependencyTableDao dependencyTableDao;

    @Autowired
    private MavenService mavenService;

    @Autowired
    private SpiderService spiderService;

    @Value("${tempPomFolder}")
    private String tempFolder;

    /**
     * 分页查询组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<ComponentDO> 查询结果
     */
    @Override
    public Page<ComponentDO> findComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置查询条件
        ComponentDO searcher = new ComponentDO();
        searcher.setOpensource(searchComponentDTO.getOpensource());
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("") ? null : searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("") ? null : searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("") ? null : searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "description", "url", "downloadUrl", "sourceUrl", "pUrl", "developers", "licenses", "hashes")
                .withIgnoreNullValues();
        Example<ComponentDO> example = Example.of(searcher, matcher);
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "language"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        return componentDao.findAll(example, pageable);
    }

    /**
     * 存储闭源组件信息
     *
     * @param saveCloseComponentDTO 保存闭源组件接口信息
     * @return 存储闭源组件信息
     */
    @Transactional
    @Override
    public Boolean saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO) {
        // 接口获得详细信息
        try {
            Model model = null;
            MavenXpp3Reader reader = new MavenXpp3Reader();
            if (saveCloseComponentDTO.getBuilder().equals("zip")) {
                unzip(saveCloseComponentDTO.getFilePath());
                File file = new File(saveCloseComponentDTO.getFilePath());
                model = reader.read(new FileReader(file.getParent() + "/pom.xml"));
            } else if (saveCloseComponentDTO.getBuilder().equals("maven")) {
                model = reader.read(new FileReader(saveCloseComponentDTO.getFilePath()));
            }
            ComponentDO javaCloseComponentDO = createJavaCloseComponentDO(model, saveCloseComponentDTO.getLanguage());
            // 存储闭源组件详细信息
            componentDao.save(javaCloseComponentDO);
            //存储闭源组件树状依赖信息
            DependencyTreeDO javaCloseDependencyTreeDO = createJavaCloseDependencyTreeDO(model, saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder());
            dependencyTreeDao.save(javaCloseDependencyTreeDO);
            //存储闭源组件平铺依赖信息
            List<DependencyTableDO> dependencyTableDOList = createCloseDependencyTableDO(javaCloseDependencyTreeDO.getTree(), model.getGroupId(), model.getArtifactId(), model.getVersion());
            //为了扩展性 将语言属性抽离出来
            for(DependencyTableDO dependencyTableDO : dependencyTableDOList){
                dependencyTableDO.setLanguage("java");
            }
            dependencyTableDao.saveAll(dependencyTableDOList);
        } catch (Exception e) {
            throw new PlatformException("存储闭源组件信息失败", e);
        }
        return true;
    }


    /**
     * 查询组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return DependencyTreeDO 依赖树信息
     */
    @Transactional
    @Override
    public DependencyTreeDO findComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        DependencyTreeDO dependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);
        String tempPomFolder = tempFolder + timeStamp;
        File file = new File(tempPomFolder);
        file.mkdirs();
        String tempPath = tempPomFolder + "/pom.xml";
        if (dependencyTreeDO == null) {
            try {
                // 调用爬虫获得pom文件
                String xml = spiderService.getPomStrByGav(componentGavDTO.getGroupId(), componentGavDTO.getArtifactId(), componentGavDTO.getVersion());
                FileWriter fileWriter = new FileWriter(tempPath);
                fileWriter.write(xml);
                fileWriter.flush();
                fileWriter.close();
                ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(tempPath, "maven", 0);
                componentDependencyTreeDO.setOpensource(true);
                dependencyTreeDO.setId(UUIDGenerator.getUUID());
                dependencyTreeDO.setGroupId(componentGavDTO.getGroupId());
                dependencyTreeDO.setArtifactId(componentGavDTO.getArtifactId());
                dependencyTreeDO.setVersion(componentDependencyTreeDO.getVersion());
                dependencyTreeDO.setTree(componentDependencyTreeDO);
                dependencyTreeDao.save(dependencyTreeDO);
                // 考虑扩展性 将language属性抽离出来
                List<DependencyTableDO> temp = creatDependencyTable(dependencyTreeDO);
                for(DependencyTableDO dependencyTableDO : temp) {
                    dependencyTableDO.setLanguage("java");
                }
                dependencyTableDao.saveAll(temp);
                deleteFolder(tempPomFolder);
            } catch (Exception e) {
                deleteFolder(tempPomFolder);
                throw new PlatformException(500, "查询组件依赖树失败");
            }

        }
        return dependencyTreeDO;
    }


    /**
     * 分页查询组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<ComponentTableDTO> 分页查询结果
     */
    @Override
    public Page<ComponentTableDTO> findComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        return dependencyTableDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavPageDTO.getGroupId(),
                componentGavPageDTO.getArtifactId(),
                componentGavPageDTO.getVersion(), pageable);
    }


    /**
     * 查询指定组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return ComponentDetailDTO 组件详细信息
     */
    @Override
    public ComponentDetailDTO findComponentDetail(ComponentGavDTO componentGavDTO) {
        ComponentDetailDTO componentDetailDTO = new ComponentDetailDTO();
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
        BeanUtils.copyProperties(componentDO, componentDetailDTO);
        return componentDetailDTO;
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
     * 根据pom文件生成闭源组件详细信息
     *
     * @param model    pom文件信息
     * @param language 组件语言
     * @return ComponentDO 闭源组件详细信息
     */
    private ComponentDO createJavaCloseComponentDO(Model model, String language) {
        ComponentDO javaCloseComponentDO = new ComponentDO();
        javaCloseComponentDO.setId(UUIDGenerator.getUUID());
        javaCloseComponentDO.setLanguage(language);
        javaCloseComponentDO.setName(model.getName() == null ? "-" : model.getName());
        javaCloseComponentDO.setGroupId(model.getGroupId() == null ? "-" : model.getGroupId());
        javaCloseComponentDO.setArtifactId(model.getArtifactId() == null ? "-" : model.getArtifactId());
        javaCloseComponentDO.setVersion(model.getVersion() == null ? "-" : model.getVersion());
        javaCloseComponentDO.setOpensource(false);
        javaCloseComponentDO.setDescription(model.getDescription() == null ? "-" : model.getDescription());
        javaCloseComponentDO.setUrl(model.getUrl() == null ? "-" : model.getUrl());
        javaCloseComponentDO.setDownloadUrl(model.getDistributionManagement() == null ? "-" : model.getDistributionManagement().getDownloadUrl());
        javaCloseComponentDO.setSourceUrl(model.getScm() == null ? "-" : model.getScm().getUrl());
        javaCloseComponentDO.setPUrl("");
        javaCloseComponentDO.setLicenses(getLicense(model));
        javaCloseComponentDO.setDevelopers(getDevelopers(model));
        //TODO: hash信息解析
        //javaCloseComponentDO.setHashes(getHashes(model));
        return javaCloseComponentDO;
    }

    /**
     * 根据pom文件生成依赖树信息
     *
     * @param model    pom文件
     * @param filePath pom文件路径
     * @param builder  构建工具
     * @return DependencyTreeDO 闭源组件依赖树信息
     * @throws Exception
     */
    private DependencyTreeDO createJavaCloseDependencyTreeDO(Model model, String filePath, String builder) throws Exception {
        DependencyTreeDO javaCloseDependencyTreeDO = new DependencyTreeDO();
        javaCloseDependencyTreeDO.setId(UUIDGenerator.getUUID());
        javaCloseDependencyTreeDO.setGroupId(model.getGroupId() == null ? "-" : model.getGroupId());
        javaCloseDependencyTreeDO.setArtifactId(model.getArtifactId() == null ? "-" : model.getArtifactId());
        javaCloseDependencyTreeDO.setVersion(model.getVersion() == null ? "-" : model.getVersion());
        ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(filePath, builder, 1);
        componentDependencyTreeDO.setOpensource(false);
        javaCloseDependencyTreeDO.setTree(componentDependencyTreeDO);
        return javaCloseDependencyTreeDO;
    }

    /**
     * 根据组件依赖树信息获得平铺组件信息
     *
     * @param componentDependencyTreeDO 组件依赖树信息
     * @param groupId                   父级组件groupId
     * @param artifactId                父级组件artifactId
     * @param version                   父级组件version
     * @return List<DependencyTableDO> 组件平铺依赖列表
     */
    private List<DependencyTableDO> createCloseDependencyTableDO(ComponentDependencyTreeDO componentDependencyTreeDO, String groupId, String artifactId, String version) {
        List<DependencyTableDO> closeDependencyTableDOList = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(componentDependencyTreeDO.getDependencies());
        while (!queue.isEmpty()) {
            DependencyTableDO dependencyTableDO = new DependencyTableDO();
            dependencyTableDO.setId(UUIDGenerator.getUUID());
            dependencyTableDO.setGroupId(groupId);
            dependencyTableDO.setArtifactId(artifactId);
            dependencyTableDO.setVersion(version);
            ComponentDependencyTreeDO componentDependencyTree = queue.poll();
            dependencyTableDO.setCGroupId(componentDependencyTree.getGroupId());
            dependencyTableDO.setCArtifactId(componentDependencyTree.getArtifactId());
            dependencyTableDO.setCVersion(componentDependencyTree.getVersion());
            dependencyTableDO.setScope(componentDependencyTreeDO.getScope());
            dependencyTableDO.setDepth(componentDependencyTree.getDepth());
            dependencyTableDO.setDirect(componentDependencyTree.getDepth() == 1);
            dependencyTableDO.setOpensource(componentDependencyTree.getOpensource());
            queue.addAll(componentDependencyTree.getDependencies());
            closeDependencyTableDOList.add(dependencyTableDO);
        }
        return closeDependencyTableDOList;
    }

    /**
     * 文件解压
     *
     * @param filePath zip文件路径
     * @throws Exception
     */
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

    /**
     * 根据组件依赖树信息生成平铺信息
     *
     * @param dependencyTreeDO 组件依赖树信息
     * @return List<DependencyTableDO> 组件平铺信息
     */
    private List<DependencyTableDO> creatDependencyTable(DependencyTreeDO dependencyTreeDO) {
        // 先删除已有记录
        dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(dependencyTreeDO.getGroupId(), dependencyTreeDO.getArtifactId(), dependencyTreeDO.getVersion());
        List<DependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(dependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            DependencyTableDO dependencyTableDO = new DependencyTableDO();
            dependencyTableDO.setId(UUIDGenerator.getUUID());
            dependencyTableDO.setGroupId(dependencyTreeDO.getGroupId());
            dependencyTableDO.setArtifactId(dependencyTreeDO.getArtifactId());
            dependencyTableDO.setVersion(dependencyTreeDO.getVersion());
            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
            dependencyTableDO.setCGroupId(componentDependencyTreeDO.getGroupId());
            dependencyTableDO.setCArtifactId(componentDependencyTreeDO.getArtifactId());
            dependencyTableDO.setCVersion(componentDependencyTreeDO.getVersion());
            dependencyTableDO.setScope(componentDependencyTreeDO.getScope());
            dependencyTableDO.setDepth(componentDependencyTreeDO.getDepth());
            dependencyTableDO.setDirect(dependencyTableDO.getDepth() == 1);
            dependencyTableDO.setOpensource(componentDependencyTreeDO.getOpensource());
            result.add(dependencyTableDO);
            queue.addAll(componentDependencyTreeDO.getDependencies());
        }
        return result;
    }

    /**
     * 根据文件路径删除文件夹
     *
     * @param filePath 文件路径
     */
    private void deleteFolder(String filePath) {
        File folder = new File(filePath);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
        folder.delete();
    }
}
