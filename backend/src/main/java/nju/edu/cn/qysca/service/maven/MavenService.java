package nju.edu.cn.qysca.service.maven;

import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;

public interface MavenService {

    ComponentDependencyTreeDO projectDependencyAnalysis(String filePath) throws Exception;
}
