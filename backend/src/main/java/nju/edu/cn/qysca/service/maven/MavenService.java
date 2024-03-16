package nju.edu.cn.qysca.service.maven;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dos.DependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;

import java.util.List;

public interface MavenService {

    ComponentDO componentAnalysis(String filePath, String builder, String type);
    DependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String type, int flag);

    List<DependencyTableDO> dependencyTableAnalysis(DependencyTreeDO dependencyTreeDO);
}
