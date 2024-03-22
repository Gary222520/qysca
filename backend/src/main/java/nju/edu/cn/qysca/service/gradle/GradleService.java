package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;

public interface GradleService {
    ComponentDependencyTreeDO projectDependencyAnalysis(String filePath);
}
