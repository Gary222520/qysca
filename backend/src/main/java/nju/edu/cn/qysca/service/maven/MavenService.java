package nju.edu.cn.qysca.service.maven;

import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;

public interface MavenService {

    ComponentDependencyTreeDO applicationDependencyAnalysis(String filePath, String builder, int flag) throws Exception;
}
