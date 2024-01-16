package nju.edu.cn.qysca.service.maven;

import nju.edu.cn.qysca.domain.project.SaveProjectDTO;

public interface MavenService {

    void projectDependencyAnalysis(SaveProjectDTO saveProjectDTO);
}
