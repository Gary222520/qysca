//package nju.edu.cn.qysca.service.project;
//
//import nju.edu.cn.qysca.dao.component.JavaCloseComponentDao;
//import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
//import nju.edu.cn.qysca.dao.project.ProjectDependencyTableDao;
//import nju.edu.cn.qysca.dao.project.ProjectDependencyTreeDao;
//import nju.edu.cn.qysca.dao.project.ProjectInfoDao;
//import nju.edu.cn.qysca.dao.project.ProjectVersionDao;
//import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;
//import nju.edu.cn.qysca.domain.component.dtos.ComponentCompareTreeDTO;
//import nju.edu.cn.qysca.domain.component.dtos.ComponentDetailDTO;
//import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
//import nju.edu.cn.qysca.domain.component.dos.DeveloperDO;
//import nju.edu.cn.qysca.domain.component.dos.LicenseDO;
//import nju.edu.cn.qysca.domain.project.dos.*;
//import nju.edu.cn.qysca.domain.project.dtos.*;
//import nju.edu.cn.qysca.exception.PlatformException;
//import nju.edu.cn.qysca.service.maven.MavenService;
//import nju.edu.cn.qysca.utils.JsonUtil;
//import nju.edu.cn.qysca.utils.excel.ExcelUtils;
//import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
//import org.springframework.beans.BeanUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.*;
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.scheduling.annotation.Async;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.File;
//import javax.servlet.http.HttpServletResponse;
//import java.text.SimpleDateFormat;
//import java.util.*;
//
//@Service
//public class ProjectServiceImpl implements ProjectService {
//
//    @Autowired
//    private ProjectInfoDao projectInfoDao;
//
//    @Autowired
//    private ProjectDependencyTreeDao projectDependencyTreeDao;
//
//    @Autowired
//    private ProjectDependencyTableDao projectDependencyTableDao;
//
//    @Autowired
//    private ProjectVersionDao projectVersionDao;
//
//    @Autowired
//    private MavenService mavenService;
//
//    @Autowired
//    private JavaOpenComponentDao javaOpenComponentDao;
//
//    @Autowired
//    private JavaCloseComponentDao javaCloseComponentDao;
//
//    @Autowired
//    private RedisTemplate<String, Object> redisTemplate;
//
//    /**
//     * 新增项目信息
//     *
//     * @param saveProjectDTO 保存项目接口信息
//     * @return Boolean 新增项目是否成功
//     */
//    @Override
//    @Transactional
//    public Boolean saveProject(SaveProjectDTO saveProjectDTO) {
//        // 新建Mongodb项目信息
//        ProjectInfoDO projectInfoDO = new ProjectInfoDO();
//        projectInfoDO.setId(UUIDGenerator.getUUID());
//        projectInfoDO.setName(saveProjectDTO.getName());
//        projectInfoDao.save(projectInfoDO);
//        // 新建Mongodb项目版本信息
//        ProjectVersionDO projectVersionDO = new ProjectVersionDO();
//        projectVersionDO.setId(UUIDGenerator.getUUID());
//        projectVersionDO.setName(saveProjectDTO.getName());
//        projectVersionDO.setVersion(saveProjectDTO.getVersion());
//        projectVersionDO.setLanguage(saveProjectDTO.getLanguage());
//        projectVersionDO.setBuilder(saveProjectDTO.getBuilder());
//        projectVersionDO.setScanner(saveProjectDTO.getScanner());
//        projectVersionDO.setNote(saveProjectDTO.getNote());
//        Date now = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String timeStamp = dateFormat.format(now);
//        projectVersionDO.setTime(timeStamp);
//        projectVersionDO.setState("RUNNING");
//        projectVersionDao.save(projectVersionDO);
//        return true;
//    }
//
//    /**
//     * 保存项目依赖关系
//     *
//     * @param saveProjectDTO 保存项目接口信息
//     */
//    @Async("taskExecutor")
//    @Override
//    @Transactional
//    public void saveProjectDependency(SaveProjectDTO saveProjectDTO) {
//        try {
//            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(saveProjectDTO.getFilePath(), saveProjectDTO.getBuilder(), 0);
//            ProjectDependencyTreeDO projectDependencyTreeDO = new ProjectDependencyTreeDO();
//            projectDependencyTreeDO.setId(UUIDGenerator.getUUID());
//            projectDependencyTreeDO.setName(saveProjectDTO.getName());
//            projectDependencyTreeDO.setVersion(saveProjectDTO.getVersion());
//            projectDependencyTreeDO.setTree(componentDependencyTreeDO);
//            projectDependencyTreeDao.save(projectDependencyTreeDO);
//            // 批量更新依赖平铺表
//            List<ProjectDependencyTableDO> projectDependencyTableDOS = createProjectDependencyTable(projectDependencyTreeDO);
//            projectDependencyTableDao.saveAll(projectDependencyTableDOS);
//            // 更改状态为SUCCESS
//            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(saveProjectDTO.getName(), saveProjectDTO.getVersion());
//            projectVersionDO.setState("SUCCESS");
//            projectVersionDao.save(projectVersionDO);
//            File file = new File(saveProjectDTO.getFilePath());
//            redisTemplate.delete(file.getParentFile().getName());
//            deleteFolder(saveProjectDTO.getFilePath().substring(0, saveProjectDTO.getFilePath().lastIndexOf("/")));
//        } catch (Exception e) {
//            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(saveProjectDTO.getName(), saveProjectDTO.getVersion());
//            projectVersionDO.setState("FAILED");
//            projectVersionDao.save(projectVersionDO);
//            File file = new File(saveProjectDTO.getFilePath());
//            redisTemplate.delete(file.getParentFile().getName());
//            deleteFolder(saveProjectDTO.getFilePath().substring(0, saveProjectDTO.getFilePath().lastIndexOf("/")));
//        }
//    }
//
//    /**
//     * 更新项目信息
//     *
//     * @param updateProjectDTO 更新项目接口信息
//     * @return 更新项目信息是否成功
//     */
//    @Override
//    @Transactional
//    public Boolean updateProject(UpdateProjectDTO updateProjectDTO) {
//        ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
//        projectVersionDO.setBuilder(updateProjectDTO.getBuilder());
//        projectVersionDO.setLanguage(updateProjectDTO.getLanguage());
//        projectVersionDO.setScanner(updateProjectDTO.getScanner());
//        projectVersionDO.setNote(updateProjectDTO.getNote());
//        Date now = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String timeStamp = dateFormat.format(now);
//        projectVersionDO.setTime(timeStamp);
//        projectVersionDO.setState("RUNNING");
//        projectVersionDao.save(projectVersionDO);
//        return true;
//    }
//
//    /**
//     * 更新项目依赖关系
//     *
//     * @param updateProjectDTO 更新项目接口信息
//     */
//    @Async("taskExecutor")
//    @Override
//    @Transactional
//    public void updateProjectDependency(UpdateProjectDTO updateProjectDTO) {
//        try {
//            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(updateProjectDTO.getFilePath(), updateProjectDTO.getBuilder(), 0);
//            ProjectDependencyTreeDO projectDependencyTreeDO = projectDependencyTreeDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
//            if (projectDependencyTreeDO == null) {
//                projectDependencyTreeDO = new ProjectDependencyTreeDO();
//                projectDependencyTreeDO.setId(UUIDGenerator.getUUID());
//                projectDependencyTreeDO.setName(updateProjectDTO.getName());
//                projectDependencyTreeDO.setVersion(updateProjectDTO.getVersion());
//            }
//            projectDependencyTreeDO.setTree(componentDependencyTreeDO);
//            projectDependencyTreeDao.save(projectDependencyTreeDO);
//            // 批量更新依赖平铺表
//            List<ProjectDependencyTableDO> projectDependencyTableDOS = createProjectDependencyTable(projectDependencyTreeDO);
//            projectDependencyTableDao.saveAll(projectDependencyTableDOS);
//            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
//            projectVersionDO.setState("SUCCESS");
//            projectVersionDao.save(projectVersionDO);
//            File file = new File(updateProjectDTO.getFilePath());
//            redisTemplate.delete(file.getParentFile().getName());
//            deleteFolder(updateProjectDTO.getFilePath().substring(0, updateProjectDTO.getFilePath().lastIndexOf("/")));
//        } catch (Exception e) {
//            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
//            projectVersionDO.setState("FAILED");
//            projectVersionDao.save(projectVersionDO);
//            File file = new File(updateProjectDTO.getFilePath());
//            redisTemplate.delete(file.getParentFile().getName());
//            deleteFolder(updateProjectDTO.getFilePath().substring(0, updateProjectDTO.getFilePath().lastIndexOf("/")));
//        }
//    }
//
//    /**
//     * 升级项目
//     *
//     * @param upgradeProjectDTO 升级项目接口信息
//     * @return 升级项目是否成功
//     */
//    @Override
//    @Transactional
//    public Boolean upgradeProject(UpgradeProjectDTO upgradeProjectDTO) {
//        ProjectVersionDO projectVersionDO = new ProjectVersionDO();
//        projectVersionDO.setId(UUIDGenerator.getUUID());
//        projectVersionDO.setName(upgradeProjectDTO.getName());
//        projectVersionDO.setVersion(upgradeProjectDTO.getVersion());
//        projectVersionDO.setLanguage(upgradeProjectDTO.getLanguage());
//        projectVersionDO.setBuilder(upgradeProjectDTO.getBuilder());
//        projectVersionDO.setScanner(upgradeProjectDTO.getScanner());
//        projectVersionDO.setNote(upgradeProjectDTO.getNote());
//        Date now = new Date();
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String timeStamp = dateFormat.format(now);
//        projectVersionDO.setTime(timeStamp);
//        projectVersionDO.setState("RUNNING");
//        projectVersionDao.save(projectVersionDO);
//        return true;
//    }
//
//    /**
//     * 升级项目依赖
//     *
//     * @param upgradeProjectDTO 升级项目接口信息
//     */
//    @Async("taskExecutor")
//    @Override
//    @Transactional
//    public void upgradeProjectDependency(UpgradeProjectDTO upgradeProjectDTO) {
//        try {
//            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(upgradeProjectDTO.getFilePath(), upgradeProjectDTO.getBuilder(), 0);
//            ProjectDependencyTreeDO projectDependencyTreeDO = new ProjectDependencyTreeDO();
//            projectDependencyTreeDO.setId(UUIDGenerator.getUUID());
//            projectDependencyTreeDO.setName(upgradeProjectDTO.getName());
//            projectDependencyTreeDO.setVersion(upgradeProjectDTO.getVersion());
//            projectDependencyTreeDO.setTree(componentDependencyTreeDO);
//            projectDependencyTreeDao.save(projectDependencyTreeDO);
//            // 批量更新依赖平铺表
//            List<ProjectDependencyTableDO> projectDependencyTableDOS = createProjectDependencyTable(projectDependencyTreeDO);
//            projectDependencyTableDao.saveAll(projectDependencyTableDOS);
//            // 更改状态为SUCCESS
//            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(upgradeProjectDTO.getName(), upgradeProjectDTO.getVersion());
//            projectVersionDO.setState("SUCCESS");
//            projectVersionDao.save(projectVersionDO);
//            File file = new File(upgradeProjectDTO.getFilePath());
//            redisTemplate.delete(file.getParentFile().getName());
//            deleteFolder(upgradeProjectDTO.getFilePath().substring(0, upgradeProjectDTO.getFilePath().lastIndexOf("/")));
//        } catch (Exception e) {
//            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(upgradeProjectDTO.getName(), upgradeProjectDTO.getVersion());
//            projectVersionDO.setState("FAILED");
//            projectVersionDao.save(projectVersionDO);
//            File file = new File(upgradeProjectDTO.getFilePath());
//            redisTemplate.delete(file.getParentFile().getName());
//            deleteFolder(upgradeProjectDTO.getFilePath().substring(0, upgradeProjectDTO.getFilePath().lastIndexOf("/")));
//        }
//    }
//
//    /**
//     * 删除项目
//     *
//     * @param name 项目名称
//     * @return 删除项目是否成功
//     */
//    @Override
//    @Transactional
//    public Boolean deleteProject(String name) {
//        projectInfoDao.deleteByName(name);
//        projectVersionDao.deleteAllByName(name);
//        projectDependencyTreeDao.deleteAllByName(name);
//        projectDependencyTableDao.deleteAllByProjectName(name);
//        return Boolean.TRUE;
//    }
//
//    /**
//     * 删除某个项目某个版本
//     *
//     * @param name    项目名称
//     * @param version 版本名称
//     * @return 删除某个项目某个版本是否成功
//     */
//    @Override
//    @Transactional
//    public Boolean deleteProjectVersion(String name, String version) {
//        projectVersionDao.deleteByNameAndVersion(name, version);
//        projectDependencyTreeDao.deleteByNameAndVersion(name, version);
//        projectDependencyTableDao.deleteAllByProjectNameAndProjectVersion(name, version);
//        return Boolean.TRUE;
//    }
//
//    /**
//     * 分页获取项目信息
//     *
//     * @param name   项目名称
//     * @param number 页码
//     * @param size   页大小
//     * @return Page<ProjectVersionDO> 项目信息分页结果
//     */
//    @Override
//    public Page<ProjectInfoDO> findProjectInfoPage(String name, int number, int size) {
//        // 模糊查询，允许参数name为空值
//        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("id").withIgnoreNullValues();
//        ProjectInfoDO projectInfoDO = new ProjectInfoDO();
//        if (name != null && !name.equals("")) {
//            projectInfoDO.setName(name);
//        }
//        Example<ProjectInfoDO> example = Example.of(projectInfoDO, matcher);
//        // 数据库页号从0开始，需减1
//        Pageable pageable = PageRequest.of(number - 1, size);
//        return projectInfoDao.findAll(example, pageable);
//    }
//
//    /**
//     * 分页获取指定项目的版本信息
//     *
//     * @param name   项目名称
//     * @param number 页码
//     * @param size   页大小
//     * @return Page<ProjectVersionDO> 项目版本信息分页结果
//     */
//    @Override
//    public Page<ProjectVersionDO> findProjectVersionPage(String name, int number, int size) {
//        // 数据库页号从0开始，需减1
//        return projectVersionDao.findAllByName(name, PageRequest.of(number - 1, size, Sort.by(Sort.Order.desc("version").nullsLast())));
//    }
//
//    /**
//     * 检查指定项目扫描中组件的个数
//     *
//     * @param name 项目名称
//     * @return Integer 扫描中组件的个数
//     */
//    @Override
//    public Integer checkRunningProject(String name) {
//        return projectVersionDao.countByNameAndState(name, "RUNNING");
//    }
//
//    /**
//     * 获取指定项目的所有版本列表
//     *
//     * @param name 项目名称
//     * @return List<String> 版本列表
//     */
//    @Override
//    public List<String> getVersionsList(String name) {
//        List<String> kvList = projectVersionDao.findVersionsByName(name, Sort.by(new Sort.Order(Sort.Direction.DESC, "version")));
//        List<String> ans = new ArrayList<>();
//        for (String s : kvList) {
//            ans.add(JsonUtil.extractValue(s, "version"));
//        }
//        return ans;
//    }
//
//    /**
//     * 获取指定项目指定版本的详细信息
//     *
//     * @param projectSearchDTO 项目版本搜索信息
//     * @return ProjectVersionDO 项目版本的详细信息
//     */
//    @Override
//    public ProjectVersionDO findProjectVersionInfo(ProjectSearchDTO projectSearchDTO) {
//        return projectVersionDao.findByNameAndVersion(
//                projectSearchDTO.getName(),
//                projectSearchDTO.getVersion());
//    }
//
//    /**
//     * 查询项目依赖树信息
//     *
//     * @param projectSearchDTO 项目版本搜索信息
//     * @return ProjectDependencyTreeDO 项目依赖树信息
//     */
//    @Override
//    public ProjectDependencyTreeDO findProjectDependencyTree(ProjectSearchDTO projectSearchDTO) {
//        return projectDependencyTreeDao.findByNameAndVersion(
//                projectSearchDTO.getName(),
//                projectSearchDTO.getVersion());
//    }
//
//    /**
//     * 分页查询项目依赖平铺信息
//     *
//     * @param projectSearchPageDTO 带分页项目版本搜索信息
//     * @return Page<ComponentTableDTO> 依赖平铺信息分页
//     */
//    @Override
//    public Page<ComponentTableDTO> findProjectDependencyTable(ProjectSearchPageDTO projectSearchPageDTO) {
//        // 设置排序规则
//        List<Sort.Order> orders = new ArrayList<>();
//        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
//        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
//        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
//        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
//        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
//        // 数据库页号从0开始，需减1
//        Pageable pageable = PageRequest.of(projectSearchPageDTO.getNumber() - 1, projectSearchPageDTO.getSize(), Sort.by(orders));
//        return projectDependencyTableDao.findByNV(
//                projectSearchPageDTO.getName(),
//                projectSearchPageDTO.getVersion(), pageable);
//    }
//
//    /**
//     * 导出项目依赖平铺信息（简明）Excel
//     *
//     * @param projectSearchDTO 项目版本搜索信息
//     * @param response         Http服务响应
//     */
//    @Override
//    public void exportTableExcelBrief(ProjectSearchDTO projectSearchDTO, HttpServletResponse response) {
//        List<TableExcelBriefDTO> resList = projectDependencyTableDao.findTableListByProject(
//                projectSearchDTO.getName(), projectSearchDTO.getVersion());
//        String fileName = projectSearchDTO.getName() + "-" + projectSearchDTO.getVersion() + "-dependencyTable-brief";
//        try {
//            ExcelUtils.export(response, fileName, resList, TableExcelBriefDTO.class);
//        } catch (Exception e) {
//            throw new PlatformException(500, "导出Excel失败");
//        }
//    }
//
//    /**
//     * 导出项目依赖平铺信息（详细）Excel
//     *
//     * @param projectSearchDTO 项目版本搜索信息
//     * @param response         Http服务响应
//     */
//    @Override
//    public void exportTableExcelDetail(ProjectSearchDTO projectSearchDTO, HttpServletResponse response) {
//        List<TableExcelDetailDTO> resList = new ArrayList<>();
//        String fileName = projectSearchDTO.getName() + "-" + projectSearchDTO.getVersion() + "-dependencyTable-detail";
//        // 先获取依赖平铺的简明信息
//        List<TableExcelBriefDTO> briefList = projectDependencyTableDao.findTableListByProject(
//                projectSearchDTO.getName(), projectSearchDTO.getVersion());
//        for (TableExcelBriefDTO brief : briefList) {
//            TableExcelDetailDTO detail = new TableExcelDetailDTO();
//            BeanUtils.copyProperties(brief, detail);
//            ComponentDetailDTO componentDetailDTO;
//            // 获取对应依赖组件的详细信息
//            if (detail.getOpensource()) {
//                componentDetailDTO = javaOpenComponentDao.findDetailByGav(
//                        detail.getGroupId(), detail.getArtifactId(), detail.getVersion());
//            } else {
//                componentDetailDTO = javaCloseComponentDao.findDetailByGav(
//                        detail.getGroupId(), detail.getArtifactId(), detail.getVersion());
//            }
//            if (componentDetailDTO == null) {
//                resList.add(detail);
//                continue;
//            }
//            detail.setDescription(componentDetailDTO.getDescription());
//            detail.setUrl(componentDetailDTO.getUrl());
//            detail.setDownloadUrl(componentDetailDTO.getDownloadUrl());
//            detail.setSourceUrl(componentDetailDTO.getSourceUrl());
//            // 拼接许可证信息和开发者信息的字符串
//            StringBuilder liName = new StringBuilder();
//            StringBuilder liUrl = new StringBuilder();
//            StringBuilder devId = new StringBuilder();
//            StringBuilder devName = new StringBuilder();
//            StringBuilder devEmail = new StringBuilder();
//            for (LicenseDO licenseDO : componentDetailDTO.getLicenses()) {
//                liName.append(licenseDO.getLicenseName()).append(";");
//                liUrl.append(licenseDO.getLicenseUrl()).append(";");
//            }
//            for (DeveloperDO developerDO : componentDetailDTO.getDevelopers()) {
//                devId.append(developerDO.getDeveloperId()).append(";");
//                devName.append(developerDO.getDeveloperName()).append(";");
//                devEmail.append(developerDO.getDeveloperEmail()).append(";");
//            }
//            detail.setLicensesName(liName.toString());
//            detail.setLicensesUrl(liUrl.toString());
//            detail.setDevelopersId(devId.toString());
//            detail.setDevelopersName(devName.toString());
//            detail.setDevelopersEmail(devEmail.toString());
//            resList.add(detail);
//        }
//        try {
//            ExcelUtils.export(response, fileName, resList, TableExcelDetailDTO.class);
//        } catch (Exception e) {
//            throw new PlatformException(500, "导出Excel失败");
//        }
//    }
//
//    /**
//     * 生成项目版本对比树
//     *
//     * @param versionCompareReqDTO 需对比的项目版本
//     * @return VersionCompareTreeDTO 对比树
//     */
//    @Override
//    public VersionCompareTreeDTO getProjectVersionCompareTree(VersionCompareReqDTO versionCompareReqDTO) {
//        // 获取需对比的两个项目版本依赖树信息
//        ProjectDependencyTreeDO fromDependencyTree = projectDependencyTreeDao.findByNameAndVersion(
//                versionCompareReqDTO.getName(), versionCompareReqDTO.getFromVersion());
//        if (fromDependencyTree == null) {
//            throw new PlatformException(500, "被对比的项目版本依赖树信息不存在");
//        }
//        ProjectDependencyTreeDO toDependencyTree = projectDependencyTreeDao.findByNameAndVersion(
//                versionCompareReqDTO.getName(), versionCompareReqDTO.getToVersion());
//        if (toDependencyTree == null) {
//            throw new PlatformException(500, "待对比的项目版本依赖树信息不存在");
//        }
//        // 打上对比标记，生成对比树
//        VersionCompareTreeDTO versionCompareTreeDTO = new VersionCompareTreeDTO();
//        BeanUtils.copyProperties(versionCompareReqDTO, versionCompareTreeDTO);
//        // 递归处理
//        versionCompareTreeDTO.setTree(recursionDealWithChange(
//                fromDependencyTree.getTree(), toDependencyTree.getTree()));
//        return versionCompareTreeDTO;
//    }
//
//    /**
//     * 递归处理变更的组件
//     *
//     * @param from 被对比者
//     * @param to   待对比者
//     * @return ComponentCompareTreeDTO 对比树
//     */
//    private ComponentCompareTreeDTO recursionDealWithChange(ComponentDependencyTreeDO from, ComponentDependencyTreeDO to) {
//        if (from == null || to == null) {
//            return null;
//        }
//        // 变更的组件打上CHANGE标记
//        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
//        BeanUtils.copyProperties(to, root);
//        root.setMark("CHANGE");
//        // 分析各子树应属于何种标记
//        Map<String, ComponentDependencyTreeDO> fromMap = new HashMap<>();
//        Map<String, ComponentDependencyTreeDO> toMap = new HashMap<>();
//        Set<String> intersection = new HashSet<>();
//        for (ComponentDependencyTreeDO fromChild : from.getDependencies()) {
//            fromMap.put(fromChild.getGroupId() + fromChild.getArtifactId() + fromChild.getOpensource(), fromChild);
//        }
//        for (ComponentDependencyTreeDO toChild : to.getDependencies()) {
//            toMap.put(toChild.getGroupId() + toChild.getArtifactId() + toChild.getOpensource(), toChild);
//        }
//        for (String key : toMap.keySet()) {
//            // 求交集
//            if (fromMap.containsKey(key)) {
//                intersection.add(key);
//            }
//        }
//        // 依次进行分析
//        for (ComponentDependencyTreeDO toChild : to.getDependencies()) {
//            String key = toChild.getGroupId() + toChild.getArtifactId() + toChild.getOpensource();
//            if (intersection.contains(key)) {
//                if (fromMap.get(key).getVersion().equals(toMap.get(key).getVersion())) {
//                    root.getDependencies().add(recursionMark(toMap.get(key), "SAME"));
//                } else {
//                    // CHANGE
//                    root.getDependencies().add(recursionDealWithChange(fromMap.get(key), toMap.get(key)));
//                }
//            } else {
//                root.getDependencies().add(recursionMark(toMap.get(key), "ADD"));
//            }
//        }
//        for (ComponentDependencyTreeDO fromChild : from.getDependencies()) {
//            String key = fromChild.getGroupId() + fromChild.getArtifactId() + fromChild.getOpensource();
//            if (!intersection.contains(key)) {
//                root.getDependencies().add(recursionMark(fromMap.get(key), "DELETE"));
//            }
//        }
//        return root;
//    }
//
//    /**
//     * 递归打上标记
//     *
//     * @param tree 树状信息
//     * @param mark 标记符号（ADD,DELETE,SAME）
//     * @return ComponentCompareTreeDTO 对比树
//     */
//    private ComponentCompareTreeDTO recursionMark(ComponentDependencyTreeDO tree, String mark) {
//        if (tree == null) {
//            return null;
//        }
//        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
//        BeanUtils.copyProperties(tree, root);
//        root.setMark(mark);
//        for (ComponentDependencyTreeDO child : tree.getDependencies()) {
//            ComponentCompareTreeDTO childAns = recursionMark(child, mark);
//            if (childAns != null) {
//                root.getDependencies().add(childAns);
//            }
//        }
//        return root;
//    }
//
//    /**
//     * 保存项目依赖平铺表
//     *
//     * @param projectDependencyTreeDO 项目依赖信息树状
//     */
//    private List<ProjectDependencyTableDO> createProjectDependencyTable(ProjectDependencyTreeDO projectDependencyTreeDO) {
//        // 先删除已有记录
//        projectDependencyTableDao.deleteAllByProjectNameAndProjectVersion(projectDependencyTreeDO.getName(), projectDependencyTreeDO.getVersion());
//        List<ProjectDependencyTableDO> result = new ArrayList<>();
//        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(projectDependencyTreeDO.getTree().getDependencies());
//        while (!queue.isEmpty()) {
//            ProjectDependencyTableDO projectDependencyTableDO = new ProjectDependencyTableDO();
//            projectDependencyTableDO.setId(UUIDGenerator.getUUID());
//            projectDependencyTableDO.setProjectName(projectDependencyTreeDO.getName());
//            projectDependencyTableDO.setProjectVersion(projectDependencyTreeDO.getVersion());
//            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
//            BeanUtils.copyProperties(componentDependencyTreeDO, projectDependencyTableDO);
//            result.add(projectDependencyTableDO);
//            queue.addAll(componentDependencyTreeDO.getDependencies());
//        }
//        return result;
//    }
//
//    /**
//     * 根据文件路径删除文件夹
//     *
//     * @param filePath 文件路径
//     */
//    private void deleteFolder(String filePath) {
//        File folder = new File(filePath);
//        if (folder.exists()) {
//            deleteFolderFile(folder);
//        }
//    }
//
//    /**
//     * 递归删除文件夹下的文件
//     *
//     * @param folder 文件夹
//     */
//    private void deleteFolderFile(File folder) {
//        File[] files = folder.listFiles();
//        for (File file : files) {
//            if (file.isDirectory()) {
//                deleteFolderFile(file);
//            }
//            file.delete();
//        }
//        folder.delete();
//    }
//}
