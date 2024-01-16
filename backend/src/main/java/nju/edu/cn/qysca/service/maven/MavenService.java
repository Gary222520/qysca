package nju.edu.cn.qysca.service.maven;

import nju.edu.cn.qysca.domain.project.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.project.SaveProjectDTO;

public interface MavenService {

    ComponentDependencyTreeDO projectDependencyAnalysis(String filePath) throws Exception;
}
