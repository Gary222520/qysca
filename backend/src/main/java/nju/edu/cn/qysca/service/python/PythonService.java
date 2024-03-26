package nju.edu.cn.qysca.service.python;

import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;

public interface PythonService {
    String projectDependencyAnalysis(String filePath);

    DependencyTreeDO pythonDependencyTreeAnalyzer(String jsonString, String groupId, String artifactId, String version);
}
