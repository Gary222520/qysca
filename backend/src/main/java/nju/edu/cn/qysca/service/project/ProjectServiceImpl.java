package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
import nju.edu.cn.qysca.dao.project.ProjectDependencyTableDao;
import nju.edu.cn.qysca.dao.project.ProjectDependencyTreeDao;
import nju.edu.cn.qysca.dao.project.ProjectInfoDao;
import nju.edu.cn.qysca.dao.project.ProjectVersionDao;
import nju.edu.cn.qysca.domain.project.*;
import nju.edu.cn.qysca.utils.JsonUtil;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectInfoDao projectInfoDao;

    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private ProjectVersionDao projectVersionDao;

    @Autowired
    private ProjectDependencyTreeDao projectDependencyTreeDao;

    @Autowired
    private ProjectDependencyTableDao projectDependencyTableDao;

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
     * @return Page<ProjectDependencyTableDO> 项目依赖平铺信息分页
     */
    @Override
    public Page<ProjectDependencyTableDO> findProjectDependencyTable(ProjectSearchPageDTO projectSearchPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(projectSearchPageDTO.getNumber() - 1, projectSearchPageDTO.getSize(), Sort.by(orders));
        return projectDependencyTableDao.findByProjectNameAndProjectVersion(
                projectSearchPageDTO.getName(),
                projectSearchPageDTO.getVersion(), pageable);
    }

}
