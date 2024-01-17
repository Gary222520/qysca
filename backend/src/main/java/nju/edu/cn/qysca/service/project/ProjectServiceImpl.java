package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.dao.project.ProjectDependencyTableDao;
import nju.edu.cn.qysca.dao.project.ProjectDependencyTreeDao;
import nju.edu.cn.qysca.dao.project.ProjectInfoDao;
import nju.edu.cn.qysca.dao.project.ProjectVersionDao;
import nju.edu.cn.qysca.domain.project.*;
import nju.edu.cn.qysca.utils.JsonUtil;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
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
    private RedisTemplate<String, Object> redisTemplate;

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
        projectVersionDO.setId(UUIDGenerator.getUUID());
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
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(saveProjectDTO.getFilePath(), saveProjectDTO.getBuilder());
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
            File file = new File(saveProjectDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(saveProjectDTO.getFilePath().substring(0, saveProjectDTO.getFilePath().lastIndexOf("/")));
        } catch (Exception e) {
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(saveProjectDTO.getName(), saveProjectDTO.getVersion());
            projectVersionDO.setState("FAILED");
            projectVersionDao.save(projectVersionDO);
            File file = new File(saveProjectDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(saveProjectDTO.getFilePath().substring(0, saveProjectDTO.getFilePath().lastIndexOf("/")));
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
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(updateProjectDTO.getFilePath(), updateProjectDTO.getBuilder());
            ProjectDependencyTreeDO projectDependencyTreeDO = projectDependencyTreeDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
            projectDependencyTreeDO.setTree(componentDependencyTreeDO);
            projectDependencyTreeDao.save(projectDependencyTreeDO);
            // 批量更新依赖平铺表
            projectDependencyTable(projectDependencyTreeDO);
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
            projectVersionDO.setState("SUCCESS");
            projectVersionDao.save(projectVersionDO);
            File file = new File(updateProjectDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(updateProjectDTO.getFilePath().substring(0, updateProjectDTO.getFilePath().lastIndexOf("/")));
        } catch (Exception e) {
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(updateProjectDTO.getName(), updateProjectDTO.getVersion());
            projectVersionDO.setState("FAILED");
            projectVersionDao.save(projectVersionDO);
            File file = new File(updateProjectDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(updateProjectDTO.getFilePath().substring(0, updateProjectDTO.getFilePath().lastIndexOf("/")));
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
        projectVersionDO.setId(UUIDGenerator.getUUID());
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
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.projectDependencyAnalysis(upgradeProjectDTO.getFilePath(), upgradeProjectDTO.getBuilder());
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
            File file = new File(upgradeProjectDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(upgradeProjectDTO.getFilePath().substring(0, upgradeProjectDTO.getFilePath().lastIndexOf("/")));
        } catch (Exception e) {
            ProjectVersionDO projectVersionDO = projectVersionDao.findByNameAndVersion(upgradeProjectDTO.getName(), upgradeProjectDTO.getVersion());
            projectVersionDO.setState("FAILED");
            projectVersionDao.save(projectVersionDO);
            File file = new File(upgradeProjectDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(upgradeProjectDTO.getFilePath().substring(0, upgradeProjectDTO.getFilePath().lastIndexOf("/")));
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
