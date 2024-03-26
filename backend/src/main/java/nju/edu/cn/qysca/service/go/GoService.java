package nju.edu.cn.qysca.service.go;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dos.DependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;

import java.util.List;

public interface GoService {
    ComponentDO componentAnalysis(String filePath, String builder, String type);
    DependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String type);

    List<DependencyTableDO> dependencyTableAnalysis(DependencyTreeDO dependencyTreeDO);

    DependencyTreeDO spiderDependency(String groupId, String artifactId, String version);

    /**
     * 爬虫获取并填充Go组件信息
     * @param name 组件名称
     * @param version 组件版本
     * @return Go组件信息
     */
    GoComponentDO spiderComponentInfo(String name,String version);
}
