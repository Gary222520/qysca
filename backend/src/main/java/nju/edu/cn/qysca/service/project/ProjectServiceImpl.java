package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.dao.component.JavaCloseComponentDao;
import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
import nju.edu.cn.qysca.dao.project.ProjectDependencyTableDao;
import nju.edu.cn.qysca.dao.project.ProjectDependencyTreeDao;
import nju.edu.cn.qysca.dao.project.ProjectInfoDao;
import nju.edu.cn.qysca.dao.project.ProjectVersionDao;
import nju.edu.cn.qysca.domain.component.dtos.ComponentDetailDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.component.dos.DeveloperDO;
import nju.edu.cn.qysca.domain.component.dos.LicenseDO;
import nju.edu.cn.qysca.domain.project.dos.*;
import nju.edu.cn.qysca.domain.project.dtos.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.utils.JsonUtil;
import nju.edu.cn.qysca.utils.excel.ExcelUtils;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectInfoDao projectInfoDao;

    @Autowired
    private ProjectDependencyTreeDao projectDependencyTreeDao;

    @Autowired
    private ProjectDependencyTableDao projectDependencyTableDao;

    @Autowired
    private ProjectVersionDao projectVersionDao;

    @Autowired
    private MavenService mavenService;

    @Autowired
    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private JavaCloseComponentDao javaCloseComponentDao;

    /**
     * 新增项目信息
     *
     * @param saveProjectDTO 保存项目接口信息
     * @return Boolean 新增项目是否成功
     */
    @Override
    public Boolean saveProject(SaveProjectDTO saveProjectDTO) {
        // 新建Mongodb项目信息
        ProjectInfoDO projectInfoDO = new ProjectInfoDO();
        projectInfoDO.setId(UUIDGenerator.getUUID());
        projectInfoDO.setName(saveProjectDTO.getName());
        projectInfoDao.save(projectInfoDO);
        // 新建Mongodb项目版本信息
        ProjectVersionDO projectVersionDO = new ProjectVersionDO();
        projectVersionDO.setName(saveProjectDTO.getName());
        projectVersionDO.setVersion(saveProjectDTO.getVersion());
        projectVersionDO.setLanguage(saveProjectDTO.getLanguage());
        projectVersionDO.setBuilder(saveProjectDTO.getBuilder());
        projectVersionDO.setScanner(saveProjectDTO.getScanner());
        projectVersionDO.setNote(saveProjectDTO.getNote());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        projectVersionDO.setTime(timeStamp);
        projectVersionDO.setState("RUNNING");
        projectVersionDao.save(projectVersionDO);
        return true;
    }

    /**
     * 保存项目依赖关系
     *
     * @param saveProjectDTO 保存项目接口信息
     */
    @Async("taskExecutor")
    @Override
    public void saveProjectDependency(SaveProjectDTO saveProjectDTO) {
        try {
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(saveProjectDTO.getFilePath());
            ProjectDependencyTreeDO projectDependencyTreeDO = new ProjectDependencyTreeDO();
            projectDependencyTreeDO.setId(UUIDGenerator.getUUID());
            projectDependencyTreeDO.setName(saveProjectDTO.getName());
            projectDependencyTreeDO.setVersion(saveProjectDTO.getVersion());
            projectDependencyTreeDO.setTree(componentDependencyTreeDO);
            projectDependencyTreeDao.save(projectDependencyTreeDO);
            // 批量更新依赖平铺表
            projectDependencyTable(projectDependencyTreeDO);
            // 更改状态为SUCCESS
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(saveProjectDTO.getName(), saveProjectDTO.getVersion());
            projectVersionDO.setState("SUCCESS");
            projectVersionDao.save(projectVersionDO);
        } catch (Exception e) {
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(saveProjectDTO.getName(), saveProjectDTO.getVersion());
            projectVersionDO.setState("FAILED");
            projectVersionDao.save(projectVersionDO);
        }
    }

    /**
     * 更新项目信息
     *
     * @param updateProjectDTO 更新项目接口信息
     * @return 更新项目信息是否成功
     */
    public Boolean updateProject(UpdateProjectDTO updateProjectDTO) {
        ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
        projectVersionDO.setBuilder(updateProjectDTO.getBuilder());
        projectVersionDO.setLanguage(updateProjectDTO.getLanguage());
        projectVersionDO.setScanner(updateProjectDTO.getScanner());
        projectVersionDO.setNote(updateProjectDTO.getNote());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        projectVersionDO.setTime(timeStamp);
        projectVersionDO.setState("RUNNING");
        projectVersionDao.save(projectVersionDO);
        return true;
    }

    /**
     * 更新项目依赖关系
     *
     * @param updateProjectDTO 更新项目接口信息
     */
    @Async("taskExecutor")
    @Override
    public void updateProjectDependency(UpdateProjectDTO updateProjectDTO) {
        try {
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(updateProjectDTO.getFilePath());
            ProjectDependencyTreeDO projectDependencyTreeDO = projectDependencyTreeDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
            projectDependencyTreeDO.setTree(componentDependencyTreeDO);
            projectDependencyTreeDao.save(projectDependencyTreeDO);
            // 批量更新依赖平铺表
            projectDependencyTable(projectDependencyTreeDO);
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
            projectVersionDO.setState("SUCCESS");
            projectVersionDao.save(projectVersionDO);
        } catch (Exception e) {
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
            projectVersionDO.setState("FAILED");
            projectVersionDao.save(projectVersionDO);
        }
    }

    /**
     * 升级项目
     *
     * @param upgradeProjectDTO 升级项目接口信息
     * @return 升级项目是否成功
     */
    @Override
    public Boolean upgradeProject(UpgradeProjectDTO upgradeProjectDTO) {
        ProjectVersionDO projectVersionDO = new ProjectVersionDO();
        projectVersionDO.setName(upgradeProjectDTO.getName());
        projectVersionDO.setVersion(upgradeProjectDTO.getVersion());
        projectVersionDO.setLanguage(upgradeProjectDTO.getLanguage());
        projectVersionDO.setBuilder(upgradeProjectDTO.getBuilder());
        projectVersionDO.setScanner(upgradeProjectDTO.getScanner());
        projectVersionDO.setNote(upgradeProjectDTO.getNote());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        projectVersionDO.setTime(timeStamp);
        projectVersionDO.setState("RUNNING");
        projectVersionDao.save(projectVersionDO);
        return true;
    }

    /**
     * 升级项目依赖
     *
     * @param upgradeProjectDTO 升级项目接口信息
     */
    @Async("taskExecutor")
    @Override
    public void upgradeProjectDependency(UpgradeProjectDTO upgradeProjectDTO) {
        try {
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(upgradeProjectDTO.getFilePath());
            ProjectDependencyTreeDO projectDependencyTreeDO = new ProjectDependencyTreeDO();
            projectDependencyTreeDO.setId(UUIDGenerator.getUUID());
            projectDependencyTreeDO.setName(upgradeProjectDTO.getName());
            projectDependencyTreeDO.setVersion(upgradeProjectDTO.getVersion());
            projectDependencyTreeDO.setTree(componentDependencyTreeDO);
            projectDependencyTreeDao.save(projectDependencyTreeDO);
            // 批量更新依赖平铺表
            projectDependencyTable(projectDependencyTreeDO);
            // 更改状态为SUCCESS
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(upgradeProjectDTO.getName(), upgradeProjectDTO.getVersion());
            projectVersionDO.setState("SUCCESS");
            projectVersionDao.save(projectVersionDO);
        } catch (Exception e) {
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(upgradeProjectDTO.getName(), upgradeProjectDTO.getVersion());
            projectVersionDO.setState("FAILED");
            projectVersionDao.save(projectVersionDO);
        }
    }

    /**
     * 删除项目
     *
     * @param name 项目名称
     * @return 删除项目是否成功
     */
    @Override
    public Boolean deleteProject(String name) {
        projectInfoDao.deleteByName(name);
        projectVersionDao.deleteAllByName(name);
        projectDependencyTreeDao.deleteAllByName(name);
        projectDependencyTableDao.deleteAllByName(name);
        return Boolean.TRUE;
    }

    /**
     * 删除某个项目某个版本
     *
     * @param name    项目名称
     * @param version 版本名称
     * @return 删除某个项目某个版本是否成功
     */
    @Override
    public Boolean deleteProjectVersion(String name, String version) {
        projectVersionDao.deleteByNameAndVersion(name, version);
        projectDependencyTreeDao.deleteByNameAndVersion(name, version);
        projectDependencyTableDao.deleteAllByNameAndVersion(name, version);
        return Boolean.TRUE;
    }

    /**
     * 分页获取项目信息
     *
     * @param name   项目名称
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectVersionDO> 项目信息分页结果
     */
    @Override
    public Page<ProjectInfoDO> findProjectInfoPage(String name, int number, int size) {
        // 模糊查询，允许参数name为空值
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnorePaths("id").withIgnoreNullValues();
        ProjectInfoDO projectInfoDO = new ProjectInfoDO();
        if (name != null && !name.equals("")) {
            projectInfoDO.setName(name);
        }
        Example<ProjectInfoDO> example = Example.of(projectInfoDO, matcher);
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(number - 1, size);
        return projectInfoDao.findAll(example, pageable);
    }

    /**
     * 分页获取指定项目的版本信息
     *
     * @param name   项目名称
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectVersionDO> 项目版本信息分页结果
     */
    @Override
    public Page<ProjectVersionDO> findProjectVersionPage(String name, int number, int size) {
        // 数据库页号从0开始，需减1
        return projectVersionDao.findAllByName(name, PageRequest.of(number - 1, size, Sort.by(Sort.Order.desc("version").nullsLast())));
    }

    /**
     * 检查指定项目扫描中组件的个数
     *
     * @param name 项目名称
     * @return Integer 扫描中组件的个数
     */
    @Override
    public Integer checkRunningProject(String name) {
        return projectVersionDao.countByNameAndState(name, "RUNNING");
    }

    /**
     * 获取指定项目的所有版本列表
     *
     * @param name 项目名称
     * @return List<String> 版本列表
     */
    @Override
    public List<String> getVersionsList(String name) {
        List<String> kvList = projectVersionDao.findVersionsByName(name, Sort.by(new Sort.Order(Sort.Direction.DESC, "version")));
        List<String> ans = new ArrayList<>();
        for (String s : kvList) {
            ans.add(JsonUtil.extractValue(s, "version"));
        }
        return ans;
    }

    /**
     * 获取指定项目指定版本的详细信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return ProjectVersionDO 项目版本的详细信息
     */
    @Override
    public ProjectVersionDO findProjectVersionInfo(ProjectSearchDTO projectSearchDTO) {
        return projectVersionDao.findByNameAndVersion(
                projectSearchDTO.getName(),
                projectSearchDTO.getVersion());
    }

    /**
     * 查询项目依赖树信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return ProjectDependencyTreeDO 项目依赖树信息
     */
    @Override
    public ProjectDependencyTreeDO findProjectDependencyTree(ProjectSearchDTO projectSearchDTO) {
        return projectDependencyTreeDao.findByNameAndVersion(
                projectSearchDTO.getName(),
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
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(projectSearchPageDTO.getNumber() - 1, projectSearchPageDTO.getSize(), Sort.by(orders));
        return projectDependencyTableDao.findByNV(
                projectSearchPageDTO.getName(),
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
        List<TableExcelBriefDTO> resList = projectDependencyTableDao.findTableListByProject(
                projectSearchDTO.getName(), projectSearchDTO.getVersion());
        String fileName = projectSearchDTO.getName() + "-" + projectSearchDTO.getVersion() + "-dependencyTable-brief";
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
        String fileName = projectSearchDTO.getName() + "-" + projectSearchDTO.getVersion() + "-dependencyTable-detail";
        // 先获取依赖平铺的简明信息
        List<TableExcelBriefDTO> briefList = projectDependencyTableDao.findTableListByProject(
                projectSearchDTO.getName(), projectSearchDTO.getVersion());
        for (TableExcelBriefDTO brief : briefList) {
            TableExcelDetailDTO detail = new TableExcelDetailDTO();
            BeanUtils.copyProperties(brief, detail);
            ComponentDetailDTO componentDetailDTO;
            // 获取对应依赖组件的详细信息
            if (detail.getOpensource()) {
                componentDetailDTO = javaOpenComponentDao.findDetailByGav(
                        detail.getGroupId(), detail.getArtifactId(), detail.getVersion());
            } else {
                componentDetailDTO = javaCloseComponentDao.findDetailByGav(
                        detail.getGroupId(), detail.getArtifactId(), detail.getVersion());
            }
            if(componentDetailDTO==null){
                resList.add(detail);
                continue;
            }
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
                liName.append(licenseDO.getLicenseName()).append(";");
                liUrl.append(licenseDO.getLicenseUrl()).append(";");
            }
            for (DeveloperDO developerDO : componentDetailDTO.getDevelopers()) {
                devId.append(developerDO.getDeveloperId()).append(";");
                devName.append(developerDO.getDeveloperName()).append(";");
                devEmail.append(developerDO.getDeveloperEmail()).append(";");
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
     * 保存项目依赖平铺表
     *
     * @param projectDependencyTreeDO 项目依赖信息树状
     */
    private void projectDependencyTable(ProjectDependencyTreeDO projectDependencyTreeDO) {
        // 先删除已有记录
        projectDependencyTableDao.deleteAllByNameAndVersion(projectDependencyTreeDO.getName(), projectDependencyTreeDO.getVersion());
        List<ProjectDependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(projectDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            ProjectDependencyTableDO projectDependencyTableDO = new ProjectDependencyTableDO();
            projectDependencyTableDO.setId(UUIDGenerator.getUUID());
            projectDependencyTableDO.setProjectName(projectDependencyTreeDO.getName());
            projectDependencyTableDO.setProjectVersion(projectDependencyTreeDO.getVersion());
            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
            BeanUtils.copyProperties(componentDependencyTreeDO, projectDependencyTableDO);
            result.add(projectDependencyTableDO);
            queue.addAll(componentDependencyTreeDO.getDependencies());
        }
        projectDependencyTableDao.saveAll(result);
    }
}
