package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;

public interface GradleService {
    DependencyTreeDO projectDependencyAnalysis(String filePath, String type, String groupId, String artifactId, String version);
}
