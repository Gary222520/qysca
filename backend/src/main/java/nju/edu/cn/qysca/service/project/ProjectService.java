package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.domain.project.*;
import org.springframework.data.domain.Page;


public interface ProjectService {
    /**
     * 保存项目信息
     * @param saveProjectDTO 保存项目接口信息
     * @return Boolean 项目是否保存成功
     */

    Boolean saveProject(SaveProjectDTO saveProjectDTO);

    /**
     * 保存项目依赖信息
     * @param saveProjectDTO 保存项目接口信息
     */
    void saveProjectDependency(SaveProjectDTO saveProjectDTO);

    /**
     * 更新项目信息
     * @param updateProjectDTO 更新项目接口信息
     * @return Boolean 项目是否更新成功
     */
    Boolean updateProject(UpdateProjectDTO updateProjectDTO);

    /**
     * 更新项目依赖信息
     * @param updateProjectDTO 更新项目接口信息
     */
    void updateProjectDependency(UpdateProjectDTO updateProjectDTO);

    /**
     * 升级项目
     * @param upgradeProjectDTO 升级项目接口信息
     * @return 升级项目是否成功
     */
    Boolean upgradeProject(UpgradeProjectDTO upgradeProjectDTO);

    /**
     * 升级项目依赖信息
     * @param upgradeProjectDTO 升级项目接口信息
     */
    void upgradeProjectDependency(UpgradeProjectDTO upgradeProjectDTO);

    /**
     * 删除项目
     * @param name 项目名称
     * @return 删除项目是否成功
     */
    Boolean deleteProject(String name);

    Boolean deleteProjectVersion(String name, String version);
    /**
     * 分页获取项目信息
     *
     * @param name 项目名称
     * @param number 页码
     * @param size 页大小
     * @return Page<ProjectVersionDO> 项目信息分页结果
     */
    Page<ProjectInfoDO> findProjectInfoPage(String name, int number, int size);


    /**
     * 分页获取指定项目的版本信息
     * @param name 项目名称
     * @param number 页码
     * @param size 页大小
     * @return Page<ProjectVersionDO> 项目版本信息分页结果
     */
    Page<ProjectVersionDO> findProjectVersionPage(String name, int number, int size);
}
