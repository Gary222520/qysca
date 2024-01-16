package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.domain.project.ProjectInfoDO;
import nju.edu.cn.qysca.domain.project.SaveProjectDTO;

import java.util.List;

public interface ProjectService {

    Boolean saveProject(SaveProjectDTO saveProjectDTO);

    List<String> findAllDistinctProjectName();

    /**
     *  获取所有项目信息
     * @return List<Project> 项目信息列表
     */
    List<ProjectInfoDO> getProjectList();

}
