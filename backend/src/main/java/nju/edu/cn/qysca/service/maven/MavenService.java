package nju.edu.cn.qysca.service.maven;

import nju.edu.cn.qysca.domain.project.dos.ComponentDependencyTreeDO;

public interface MavenService {

    ComponentDependencyTreeDO projectDependencyAnalysis(String filePath, String builder) throws Exception;
}
