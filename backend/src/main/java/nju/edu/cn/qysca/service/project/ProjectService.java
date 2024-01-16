package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.domain.project.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProjectService {

    Boolean saveProject(SaveProjectDTO saveProjectDTO);

    /**
     * 分页获取项目信息
     *
     * @param name   项目名称
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectVersionDO> 项目信息分页结果
     */
    Page<ProjectInfoDO> findProjectInfoPage(String name, int number, int size);

    /**
     * 分页获取指定项目的版本信息
     *
     * @param name   项目名称
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectVersionDO> 项目版本信息分页结果
     */
    Page<ProjectVersionDO> findProjectVersionPage(String name, int number, int size);

    /**
     * 检查指定项目扫描中组件的个数
     *
     * @param name 项目名称
     * @return Integer 扫描中组件的个数
     */
    Integer checkRunningProject(String name);

    /**
     * 获取指定项目的所有版本列表
     *
     * @param name 项目名称
     * @return List<String> 版本列表
     */
    List<String> getVersionsList(String name);

    /**
     * 获取指定项目指定版本的详细信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return ProjectVersionDO 项目版本的详细信息
     */
    ProjectVersionDO findProjectVersionInfo(ProjectSearchDTO projectSearchDTO);

    /**
     * 查询项目依赖树信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return ProjectDependencyTreeDO 项目依赖树信息
     */
    ProjectDependencyTreeDO findProjectDependencyTree(ProjectSearchDTO projectSearchDTO);

    /**
     * 分页查询项目依赖平铺信息
     *
     * @param projectSearchPageDTO 带分页项目版本搜索信息
     * @return Page<ProjectDependencyTableDO> 项目依赖平铺信息分页
     */
    Page<ProjectDependencyTableDO> findProjectDependencyTable(ProjectSearchPageDTO projectSearchPageDTO);
}
