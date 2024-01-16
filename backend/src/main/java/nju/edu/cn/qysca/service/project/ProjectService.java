package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.domain.project.ProjectInfoDO;
import nju.edu.cn.qysca.domain.project.ProjectVersionDO;
import nju.edu.cn.qysca.domain.project.SaveProjectDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProjectService {

    Boolean saveProject(SaveProjectDTO saveProjectDTO);

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
