package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.dao.component.DependencyTableDao;
import nju.edu.cn.qysca.dao.component.DependencyTreeDao;
import nju.edu.cn.qysca.dao.project.ProjectDao;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.ComponentCompareTreeDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentDetailDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.project.dos.*;
import nju.edu.cn.qysca.domain.project.dtos.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.utils.excel.ExcelUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private DependencyTreeDao dependencyTreeDao;

    @Autowired
    private DependencyTableDao dependencyTableDao;

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private MavenService mavenService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 分页获取根项目信息
     *
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectDO> 项目信息分页结果
     */
    @Override
    public Page<ProjectDO> findRootPage(int number, int size) {
        Pageable pageable = PageRequest.of(number - 1, size);
        return projectDao.findRootPage(pageable);
    }


    /**
     * 模糊查询项目名称
     * @param name 项目名称
     * @return List<String> 模糊查询项目名称列表
     */
    @Override
    public List<String> searchProjectName(String name) {
        return projectDao.searchProjectName(name);
    }

    /**
     * 根据名称查询项目 并返回项目的最新版本
     * @param name 项目名称
     * @return ProjectDO 项目信息
     */
    @Override
    public ProjectDO findProject(String name) {
        return projectDao.findProject(name);
    }


    /**
     * 根据项目Id返回子项目信息
     * @param  groupId 组织Id
     * @param  artifactId 项目Id
     * @param  version 项目版本
     * @return SubProjectDTO 子项目信息
     */
    @Override
    public SubProjectDTO findSubProject(String groupId, String artifactId, String version) {
        ProjectDO projectDO =  projectDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        List<ProjectDO> subProject = projectDao.findSubProject(projectDO.getId());
        List<ComponentDO> subComponent = componentDao.findSubComponent(projectDO.getId());
        SubProjectDTO subProjectDTO = new SubProjectDTO();
        subProjectDTO.setSubProject(subProject);
        subProjectDTO.setSubComponent(subComponent);
        return subProjectDTO;
    }

    /**
     * 新增/更新项目信息
     *
     * @param saveProjectDTO 保存项目接口信息
     * @return Boolean 新增项目是否成功
     */
    @Override
    @Transactional
    public Boolean saveProject(SaveProjectDTO saveProjectDTO) {
        ProjectDO projectDO = null;
        if(StringUtils.isEmpty(saveProjectDTO.getId())){
            projectDO = new ProjectDO();
            BeanUtils.copyProperties(saveProjectDTO, projectDO);
            projectDO.setState("CREATED");
            projectDO.setLock(false);
            projectDO.setRelease(false);
            projectDO.setRoot(saveProjectDTO.getParentId() == null);
        }else{
            projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(saveProjectDTO.getGroupId(), saveProjectDTO.getArtifactId(), saveProjectDTO.getVersion());
            projectDO.setDescription(saveProjectDTO.getDescription());
            projectDO.setType(saveProjectDTO.getType());
        }
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        projectDO.setTime(timeStamp);
        projectDao.save(projectDO);
        if(saveProjectDTO.getParentId() != null) {
            ProjectDO parentProjectDO = projectDao.findProjectDOById(saveProjectDTO.getParentId());
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentProjectDO.getChildProject()));
            temp.add(projectDO.getId());
            parentProjectDO.setChildProject(temp.toArray(new String[temp.size()]));
            projectDao.save(parentProjectDO);
        }
        return true;
    }


    /**
     * 在保存项目依赖时将项目状态改为RUNNING
     * @param groupId 项目组织Id
     * @param artifactId 项目工件Id
     * @param version 项目版本
     */
    @Override
    public void changeProjectState(String groupId, String artifactId, String version) {
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        projectDO.setState("RUNNING");
        projectDao.save(projectDO);
    }

    /**
     * 保存项目依赖关系
     *
     * @param saveProjectDependencyDTO 保存项目接口信息
     */
    @Async("taskExecutor")
    @Override
    @Transactional
    public void saveProjectDependency(SaveProjectDependencyDTO saveProjectDependencyDTO) {
        try {
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(saveProjectDependencyDTO.getFilePath(), saveProjectDependencyDTO.getBuilder(), 0);
            DependencyTreeDO projectDependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(saveProjectDependencyDTO.getGroupId(), saveProjectDependencyDTO.getArtifactId(), saveProjectDependencyDTO.getVersion());
            if(projectDependencyTreeDO == null){
                projectDependencyTreeDO = new DependencyTreeDO();
                projectDependencyTreeDO.setGroupId(saveProjectDependencyDTO.getGroupId());
                projectDependencyTreeDO.setArtifactId(saveProjectDependencyDTO.getArtifactId());
                projectDependencyTreeDO.setVersion(saveProjectDependencyDTO.getVersion());
                projectDependencyTreeDO.setTree(componentDependencyTreeDO);
            }else{
                projectDependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(saveProjectDependencyDTO.getGroupId(), saveProjectDependencyDTO.getArtifactId(), saveProjectDependencyDTO.getVersion());
                projectDependencyTreeDO.setTree(componentDependencyTreeDO);
            }
            dependencyTreeDao.save(projectDependencyTreeDO);
            // 批量更新依赖平铺表
            List<DependencyTableDO> projectDependencyTableDOS = createProjectDependencyTable(projectDependencyTreeDO);
            for (DependencyTableDO dependencyTableDO : projectDependencyTableDOS) {
                dependencyTableDO.setLanguage(saveProjectDependencyDTO.getLanguage());
            }
            dependencyTableDao.saveAll(projectDependencyTableDOS);
            // 更改状态为SUCCESS
            ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(saveProjectDependencyDTO.getGroupId(), saveProjectDependencyDTO.getArtifactId(), saveProjectDependencyDTO.getVersion());
            projectDO.setBuilder(saveProjectDependencyDTO.getBuilder());
            projectDO.setScanner(saveProjectDependencyDTO.getScanner());
            projectDO.setLanguage(saveProjectDependencyDTO.getLanguage());
            projectDO.setState("SUCCESS");
            projectDao.save(projectDO);
            File file = new File(saveProjectDependencyDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(saveProjectDependencyDTO.getFilePath().substring(0, saveProjectDependencyDTO.getFilePath().lastIndexOf("/")));
        } catch (Exception e) {
            ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(saveProjectDependencyDTO.getGroupId(), saveProjectDependencyDTO.getArtifactId(), saveProjectDependencyDTO.getVersion());
            projectDO.setState("FAILED");
            projectDao.save(projectDO);
            File file = new File(saveProjectDependencyDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(saveProjectDependencyDTO.getFilePath().substring(0, saveProjectDependencyDTO.getFilePath().lastIndexOf("/")));
        }
    }

    /**
     * 升级项目
     *
     * @param upgradeProjectDTO 升级项目接口信息
     * @return 升级项目是否成功
     */
    @Override
    @Transactional
    public Boolean upgradeProject(UpgradeProjectDTO upgradeProjectDTO) {
        // 保存新项目
        ProjectDO oldProjectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(upgradeProjectDTO.getGroupId(), upgradeProjectDTO.getArtifactId(), upgradeProjectDTO.getOldVersion());
        ProjectDO newProjectDO = new ProjectDO();
        BeanUtils.copyProperties(oldProjectDO, newProjectDO);
        newProjectDO.setId(null);
        newProjectDO.setVersion(upgradeProjectDTO.getNewVersion());
        newProjectDO.setDescription(upgradeProjectDTO.getDescription());
        newProjectDO.setCreator(upgradeProjectDTO.getCreator());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        newProjectDO.setTime(timeStamp);
        projectDao.save(newProjectDO);
        // 新增新项目关系 删除旧项目关系
        if(!StringUtils.isEmpty(upgradeProjectDTO.getParentGroupId()) && !StringUtils.isEmpty(upgradeProjectDTO.getParentArtifactId()) && !StringUtils.isEmpty(upgradeProjectDTO.getParentVersion())){
            ProjectDO parentProjectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(upgradeProjectDTO.getParentGroupId(), upgradeProjectDTO.getParentArtifactId(), upgradeProjectDTO.getParentVersion());
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentProjectDO.getChildProject()));
            temp.remove(oldProjectDO.getId());
            temp.add(newProjectDO.getId());
            parentProjectDO.setChildProject(temp.toArray(new String[temp.size()]));
            projectDao.save(parentProjectDO);
        }
        return true;
    }


    /**
     * 删除某个项目某个版本
     *
     * @param deleteProjectDTO 删除项目DTO
     * @return 删除某个项目某个版本是否成功
     */
    @Override
    @Transactional
    public Boolean deleteProjectVersion(DeleteProjectDTO deleteProjectDTO) {
        // 先删除在父项目中的依赖信息
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(deleteProjectDTO.getGroupId(), deleteProjectDTO.getArtifactId(), deleteProjectDTO.getVersion());
        if(!StringUtils.isEmpty(deleteProjectDTO.getParentGroupId()) && !StringUtils.isEmpty(deleteProjectDTO.getParentArtifactId())&& !StringUtils.isEmpty(deleteProjectDTO.getParentVersion())) {
            ProjectDO parentProjectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(deleteProjectDTO.getParentGroupId(), deleteProjectDTO.getParentArtifactId(), deleteProjectDTO.getParentVersion());
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentProjectDO.getChildProject()));
            temp.remove(projectDO.getId());
            parentProjectDO.setChildProject(temp.toArray(new String[temp.size()]));
            projectDao.save(parentProjectDO);
        }
        // 如果项目本身是其他项目的子项目则无需继续操作
        if(!projectDao.findParentProject(projectDO.getId()).isEmpty()) {
            return Boolean.TRUE;
        }
        // 递归删除子项目 若子项目在其他项目中引用则只删除关系，否则删除项目
        deleteProjectRecursively(projectDO.getId());
        return Boolean.TRUE;
    }
    private void deleteProjectRecursively(String projectId) {
        ProjectDO projectDO = projectDao.findProjectDOById(projectId);
        if(projectDO != null){
            deleteChildrenRecursively(projectDO.getChildProject());
            projectDao.delete(projectDO);
            dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(projectDO.getGroupId(), projectDO.getArtifactId(), projectDO.getVersion());
            dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(projectDO.getGroupId(), projectDO.getArtifactId(), projectDO.getVersion());
        }
    }

    private void deleteChildrenRecursively(String[] children) {
        if(children == null || children.length == 0){
            return;
        }
        for(String child : children){
            List<ProjectDO> parentProject = projectDao.findParentProject(child);
            if(parentProject.size() <= 1){
                deleteProjectRecursively(child);
            }
        }
    }


    /**
     * 在项目中增加组件
     * @param projectComponentDTO 项目组件信息接口
     * @return 在项目中增加组件是否成功
     */
    @Override
    @Transactional
    public Boolean saveProjectComponent(ProjectComponentDTO projectComponentDTO) {
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(projectComponentDTO.getParentGroupId(), projectComponentDTO.getParentArtifactId(), projectComponentDTO.getParentVersion());
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(projectDO.getChildComponent()));
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(projectComponentDTO.getGroupId(), projectComponentDTO.getArtifactId(), projectComponentDTO.getVersion());
        temp.add(componentDO.getId());
        projectDO.setChildComponent(temp.toArray(new String[temp.size()]));
        projectDao.save(projectDO);
        return Boolean.TRUE;
    }

    /**
     * 在项目中删除组件
     * @param projectComponentDTO 项目组件信息接口
     * @return 在项目中删除组件是否成功
     */
    @Override
    public Boolean deleteProjectComponent(ProjectComponentDTO projectComponentDTO) {
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(projectComponentDTO.getParentGroupId(), projectComponentDTO.getParentArtifactId(), projectComponentDTO.getParentVersion());
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(projectDO.getChildComponent()));
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(projectComponentDTO.getGroupId(), projectComponentDTO.getArtifactId(), projectComponentDTO.getVersion());
        temp.remove(componentDO.getId());
        projectDO.setChildComponent(temp.toArray(new String[temp.size()]));
        projectDao.save(projectDO);
        return Boolean.TRUE;
    }

    /**
     * 改变项目的锁定状态
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本号
     */
    @Override
    public void changeLockState(String groupId, String artifactId, String version) {
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        if(projectDO.getLock()){
            projectDO.setLock(false);
        }else{
            projectDO.setLock(true);
        }
        projectDao.save(projectDO);
    }

    /**
     * 改变项目的发布状态
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本号
     */
    @Override
    public void changeReleaseState(String groupId, String artifactId, String version) {
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        if(projectDO.getRelease()) {
            projectDO.setRelease(false);
        } else {
            projectDO.setRelease(true);
        }
        projectDao.save(projectDO);
    }

    /**
     * 分页获取指定项目的版本信息
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectDO> 项目版本信息分页结果
     */
    @Override
    public Page<ProjectDO> findProjectVersionPage(String groupId, String artifactId, int number, int size) {
        // 数据库页号从0开始，需减1
        return projectDao.findAllByGroupIdAndArtifactId(groupId, artifactId, PageRequest.of(number - 1, size, Sort.by(Sort.Order.desc("version").nullsLast())));
    }

    /**
     * 检查指定项目扫描中组件的个数
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return Integer 扫描中组件的个数
     */
    @Override
    public Integer checkRunningProject(String groupId, String artifactId){
        return projectDao.countByGroupIdAndArtifactIdAndState(groupId, artifactId, "RUNNING");
    }

    /**
     * 获取指定项目的所有版本列表
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return List<String> 版本列表
     */
    @Override
    public List<String> getVersionsList(String groupId, String artifactId) {
        return projectDao.findVersionsByGroupIdAndArtifactId(groupId, artifactId);
    }

    /**
     * 获取指定项目指定版本的详细信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return ProjectDO 项目版本的详细信息
     */
    @Override
    public ProjectDO findProjectVersionInfo(ProjectSearchDTO projectSearchDTO) {
        return projectDao.findByGroupIdAndArtifactIdAndVersion(
                projectSearchDTO.getGroupId(),
                projectSearchDTO.getArtifactId(),
                projectSearchDTO.getVersion());
    }

    /**
     * 查询项目依赖树信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return DependencyTreeDO 项目依赖树信息
     */
    @Override
    public DependencyTreeDO findProjectDependencyTree(ProjectSearchDTO projectSearchDTO) {
        return dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                projectSearchDTO.getGroupId(),
                projectSearchDTO.getArtifactId(),
                projectSearchDTO.getVersion());
    }

    /**
     * 分页查询项目依赖平铺信息
     *
     * @param projectSearchPageDTO 带分页项目版本搜索信息
     * @return Page<ComponentTableDTO> 依赖平铺信息分页
     */
    @Override
    public Page<ComponentTableDTO> findProjectDependencyTable(ProjectSearchPageDTO projectSearchPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(projectSearchPageDTO.getNumber() - 1, projectSearchPageDTO.getSize(), Sort.by(orders));
        return dependencyTableDao.findByGroupIdAndArtifactIdAndVersion(
                projectSearchPageDTO.getGroupId(),
                projectSearchPageDTO.getArtifactId(),
                projectSearchPageDTO.getVersion(), pageable);
    }

    /**
     * 导出项目依赖平铺信息（简明）Excel
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @param response         Http服务响应
     */
    @Override
    public void exportTableExcelBrief(ProjectSearchDTO projectSearchDTO, HttpServletResponse response) {
        List<TableExcelBriefDTO> resList = dependencyTableDao.findTableListByGroupIdAndArtifactIdAndVersion(
                projectSearchDTO.getGroupId(), projectSearchDTO.getArtifactId(), projectSearchDTO.getVersion());
        String fileName = projectSearchDTO.getArtifactId() + "-" + projectSearchDTO.getVersion() + "-dependencyTable-brief";
        try {
            ExcelUtils.export(response, fileName, resList, TableExcelBriefDTO.class);
        } catch (Exception e) {
            throw new PlatformException(500, "导出Excel失败");
        }
    }

    /**
     * 导出项目依赖平铺信息（详细）Excel
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @param response         Http服务响应
     */
    @Override
    public void exportTableExcelDetail(ProjectSearchDTO projectSearchDTO, HttpServletResponse response) {
        List<TableExcelDetailDTO> resList = new ArrayList<>();
        String fileName = projectSearchDTO.getArtifactId() + "-" + projectSearchDTO.getVersion() + "-dependencyTable-detail";
        // 先获取依赖平铺的简明信息
        List<TableExcelBriefDTO> briefList = dependencyTableDao.findTableListByGroupIdAndArtifactIdAndVersion(
                projectSearchDTO.getGroupId(), projectSearchDTO.getArtifactId(), projectSearchDTO.getVersion());
        for (TableExcelBriefDTO brief : briefList) {
            TableExcelDetailDTO detail = new TableExcelDetailDTO();
            BeanUtils.copyProperties(brief, detail);
            ComponentDetailDTO componentDetailDTO = new ComponentDetailDTO();
            // 获取对应依赖组件的详细信息
            ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(detail.getCGroupId(), detail.getCArtifactId(), detail.getCVersion());
            if (componentDO == null) {
                resList.add(detail);
                continue;
            }
            BeanUtils.copyProperties(componentDO, componentDetailDTO);
            detail.setName(componentDetailDTO.getName());
            detail.setDescription(componentDetailDTO.getDescription());
            detail.setUrl(componentDetailDTO.getUrl());
            detail.setDownloadUrl(componentDetailDTO.getDownloadUrl());
            detail.setSourceUrl(componentDetailDTO.getSourceUrl());
            // 拼接许可证信息和开发者信息的字符串
            StringBuilder liName = new StringBuilder();
            StringBuilder liUrl = new StringBuilder();
            StringBuilder devId = new StringBuilder();
            StringBuilder devName = new StringBuilder();
            StringBuilder devEmail = new StringBuilder();
            for (LicenseDO licenseDO : componentDetailDTO.getLicenses()) {
                liName.append(licenseDO.getName()).append(";");
                liUrl.append(licenseDO.getUrl()).append(";");
            }
            for (DeveloperDO developerDO : componentDetailDTO.getDevelopers()) {
                devId.append(developerDO.getId()).append(";");
                devName.append(developerDO.getName()).append(";");
                devEmail.append(developerDO.getEmail()).append(";");
            }
            detail.setLicensesName(liName.toString());
            detail.setLicensesUrl(liUrl.toString());
            detail.setDevelopersId(devId.toString());
            detail.setDevelopersName(devName.toString());
            detail.setDevelopersEmail(devEmail.toString());
            resList.add(detail);
        }
        try {
            ExcelUtils.export(response, fileName, resList, TableExcelDetailDTO.class);
        } catch (Exception e) {
            throw new PlatformException(500, "导出Excel失败");
        }
    }

    /**
     * 生成项目版本对比树
     *
     * @param versionCompareReqDTO 需对比的项目版本
     * @return VersionCompareTreeDTO 对比树
     */
    @Override
    public VersionCompareTreeDTO getProjectVersionCompareTree(VersionCompareReqDTO versionCompareReqDTO) {
        // 获取需对比的两个项目版本依赖树信息
        DependencyTreeDO fromDependencyTree = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                versionCompareReqDTO.getGroupId(), versionCompareReqDTO.getArtifactId(), versionCompareReqDTO.getFromVersion());
        if (fromDependencyTree == null) {
            throw new PlatformException(500, "被对比的项目版本依赖树信息不存在");
        }
        DependencyTreeDO toDependencyTree = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                versionCompareReqDTO.getGroupId(), versionCompareReqDTO.getArtifactId(), versionCompareReqDTO.getToVersion());
        if (toDependencyTree == null) {
            throw new PlatformException(500, "待对比的项目版本依赖树信息不存在");
        }
        // 打上对比标记，生成对比树
        VersionCompareTreeDTO versionCompareTreeDTO = new VersionCompareTreeDTO();
        BeanUtils.copyProperties(versionCompareReqDTO, versionCompareTreeDTO);
        // 递归处理
        versionCompareTreeDTO.setTree(recursionDealWithChange(
                fromDependencyTree.getTree(), toDependencyTree.getTree()));
        return versionCompareTreeDTO;
    }

    /**
     * 递归处理变更的组件
     *
     * @param from 被对比者
     * @param to   待对比者
     * @return ComponentCompareTreeDTO 对比树
     */
    private ComponentCompareTreeDTO recursionDealWithChange(ComponentDependencyTreeDO from, ComponentDependencyTreeDO to) {
        if (from == null || to == null) {
            return null;
        }
        // 变更的组件打上CHANGE标记
        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
        BeanUtils.copyProperties(to, root);
        root.setMark("CHANGE");
        // 分析各子树应属于何种标记
        Map<String, ComponentDependencyTreeDO> fromMap = new HashMap<>();
        Map<String, ComponentDependencyTreeDO> toMap = new HashMap<>();
        Set<String> intersection = new HashSet<>();
        for (ComponentDependencyTreeDO fromChild : from.getDependencies()) {
            fromMap.put(fromChild.getGroupId() + fromChild.getArtifactId() + fromChild.getOpensource(), fromChild);
        }
        for (ComponentDependencyTreeDO toChild : to.getDependencies()) {
            toMap.put(toChild.getGroupId() + toChild.getArtifactId() + toChild.getOpensource(), toChild);
        }
        for (String key : toMap.keySet()) {
            // 求交集
            if (fromMap.containsKey(key)) {
                intersection.add(key);
            }
        }
        // 依次进行分析
        for (ComponentDependencyTreeDO toChild : to.getDependencies()) {
            String key = toChild.getGroupId() + toChild.getArtifactId() + toChild.getOpensource();
            if (intersection.contains(key)) {
                if (fromMap.get(key).getVersion().equals(toMap.get(key).getVersion())) {
                    root.getDependencies().add(recursionMark(toMap.get(key), "SAME"));
                } else {
                    // CHANGE
                    root.getDependencies().add(recursionDealWithChange(fromMap.get(key), toMap.get(key)));
                }
            } else {
                root.getDependencies().add(recursionMark(toMap.get(key), "ADD"));
            }
        }
        for (ComponentDependencyTreeDO fromChild : from.getDependencies()) {
            String key = fromChild.getGroupId() + fromChild.getArtifactId() + fromChild.getOpensource();
            if (!intersection.contains(key)) {
                root.getDependencies().add(recursionMark(fromMap.get(key), "DELETE"));
            }
        }
        return root;
    }

    /**
     * 递归打上标记
     *
     * @param tree 树状信息
     * @param mark 标记符号（ADD,DELETE,SAME）
     * @return ComponentCompareTreeDTO 对比树
     */
    private ComponentCompareTreeDTO recursionMark(ComponentDependencyTreeDO tree, String mark) {
        if (tree == null) {
            return null;
        }
        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
        BeanUtils.copyProperties(tree, root);
        root.setMark(mark);
        for (ComponentDependencyTreeDO child : tree.getDependencies()) {
            ComponentCompareTreeDTO childAns = recursionMark(child, mark);
            if (childAns != null) {
                root.getDependencies().add(childAns);
            }
        }
        return root;
    }

    /**
     * 保存项目依赖平铺表
     *
     * @param projectDependencyTreeDO 项目依赖信息树状
     */
    private List<DependencyTableDO> createProjectDependencyTable(DependencyTreeDO projectDependencyTreeDO) {
        // 先删除已有记录
        dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(projectDependencyTreeDO.getGroupId(), projectDependencyTreeDO.getArtifactId(), projectDependencyTreeDO.getVersion());
        List<DependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(projectDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            DependencyTableDO projectDependencyTableDO = new DependencyTableDO();
            projectDependencyTableDO.setGroupId(projectDependencyTreeDO.getGroupId());
            projectDependencyTableDO.setArtifactId(projectDependencyTreeDO.getArtifactId());
            projectDependencyTableDO.setVersion(projectDependencyTreeDO.getVersion());
            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
            projectDependencyTableDO.setCGroupId(componentDependencyTreeDO.getGroupId());
            projectDependencyTableDO.setCArtifactId(componentDependencyTreeDO.getArtifactId());
            projectDependencyTableDO.setCVersion(componentDependencyTreeDO.getVersion());
            projectDependencyTableDO.setScope(componentDependencyTreeDO.getScope());
            projectDependencyTableDO.setDepth(componentDependencyTreeDO.getDepth());
            projectDependencyTableDO.setDirect(projectDependencyTableDO.getDepth() == 1);
            projectDependencyTableDO.setOpensource(componentDependencyTreeDO.getOpensource());
            result.add(projectDependencyTableDO);
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
