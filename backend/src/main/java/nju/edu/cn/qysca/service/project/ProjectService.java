package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import nju.edu.cn.qysca.domain.project.dtos.*;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface ProjectService {
    /**
     * 保存项目信息
     *
     * @param saveProjectDTO 保存项目接口信息
     * @return Boolean 项目是否保存成功
     */

    Boolean saveProject(SaveProjectDTO saveProjectDTO);

    /**
     * 保存项目依赖信息
     *
     * @param saveProjectDTO 保存项目接口信息
     */
    void saveProjectDependency(SaveProjectDTO saveProjectDTO);

    /**
     * 更新项目信息
     *
     * @param updateProjectDTO 更新项目接口信息
     * @return Boolean 项目是否更新成功
     */
    Boolean updateProject(UpdateProjectDTO updateProjectDTO);

    /**
     * 更新项目依赖信息
     *
     * @param updateProjectDTO 更新项目接口信息
     */
    void updateProjectDependency(UpdateProjectDTO updateProjectDTO);

    /**
     * 升级项目
     *
     * @param upgradeProjectDTO 升级项目接口信息
     * @return 升级项目是否成功
     */
    Boolean upgradeProject(UpgradeProjectDTO upgradeProjectDTO);

    /**
     * 升级项目依赖信息
     *
     * @param upgradeProjectDTO 升级项目接口信息
     */
    void upgradeProjectDependency(UpgradeProjectDTO upgradeProjectDTO);

    /**
     * 删除项目
     *
     * @param groupId 组织Id
     * @param artifactId 项目Id
     * @return 删除项目是否成功
     */
    Boolean deleteProject(String groupId,  String artifactId);

    Boolean deleteProjectVersion(String groupId,  String artifactId, String version);

    /**
     * 分页获取项目信息
     *
     * @param name 项目名称
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectVersionDO> 项目信息分页结果
     */
    Page<ProjectDO> findProjectPage(String name, int number, int size);

    /**
     * 分页获取指定项目的版本信息
     *
     * @param groupId 组织Id
     * @Param artifactId 工件Id
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectVersionDO> 项目版本信息分页结果
     */
    Page<ProjectDO> findProjectVersionPage(String groupId, String artifactId,  int number, int size);

    /**
     * 检查指定项目扫描中组件的个数
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return Integer 扫描中组件的个数
     */
    Integer checkRunningProject(String groupId, String artifactId);

    /**
     * 获取指定项目的所有版本列表
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return List<String> 版本列表
     */
    List<String> getVersionsList(String groupId, String artifactId);

    /**
     * 获取指定项目指定版本的详细信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return ProjectDO 项目版本的详细信息
     */
    ProjectDO findProjectVersionInfo(ProjectSearchDTO projectSearchDTO);

    /**
     * 查询项目依赖树信息
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @return DependencyTreeDO 项目依赖树信息
     */
    DependencyTreeDO findProjectDependencyTree(ProjectSearchDTO projectSearchDTO);

    /**
     * 分页查询项目依赖平铺信息
     *
     * @param projectSearchPageDTO 带分页项目版本搜索信息
     * @return Page<ComponentTableDTO> 依赖平铺信息分页
     */
    Page<ComponentTableDTO> findProjectDependencyTable(ProjectSearchPageDTO projectSearchPageDTO);

    /**
     * 导出项目依赖平铺信息（简明）Excel
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @param response         Http服务响应
     */
    void exportTableExcelBrief(ProjectSearchDTO projectSearchDTO, HttpServletResponse response);

    /**
     * 导出项目依赖平铺信息（详细）Excel
     *
     * @param projectSearchDTO 项目版本搜索信息
     * @param response         Http服务响应
     */
    void exportTableExcelDetail(ProjectSearchDTO projectSearchDTO, HttpServletResponse response);

    /**
     * 生成项目版本对比树
     *
     * @param versionCompareReqDTO 需对比的项目版本
     * @return VersionCompareTreeDTO 对比树
     */
    VersionCompareTreeDTO getProjectVersionCompareTree(VersionCompareReqDTO versionCompareReqDTO);
}
