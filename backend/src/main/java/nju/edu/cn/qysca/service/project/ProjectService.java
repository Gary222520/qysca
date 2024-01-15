package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.domain.project.SaveProjectDTO;

import java.util.List;

public interface ProjectService {

    Boolean saveProject(SaveProjectDTO saveProjectDTO);

    List<String> findAllDistinctProjectName();
}
