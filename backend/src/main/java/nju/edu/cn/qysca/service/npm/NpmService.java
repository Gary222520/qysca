package nju.edu.cn.qysca.service.npm;

import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JsDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.JsDependencyTableDO;

import java.util.List;

public interface NpmService {

    JsComponentDO componentAnalysis(String filePath, String type);

    JsDependencyTreeDO dependencyTreeAnalysis(String filePath, String type);

    List<JsDependencyTableDO> dependencyTableAnalysis(JsDependencyTreeDO jsDependencyTreeDO);

    JsDependencyTreeDO spiderDependencyTree(String name, String version);
}
