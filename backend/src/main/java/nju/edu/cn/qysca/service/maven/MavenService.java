package nju.edu.cn.qysca.service.maven;

import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;

import java.util.List;

public interface MavenService {

    JavaComponentDO componentAnalysis(String filePath, String builder, String type);

    JavaDependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String type);

    List<JavaDependencyTableDO> dependencyTableAnalysis(JavaDependencyTreeDO javaDependencyTreeDO);

    JavaDependencyTreeDO spiderDependency(String groupId, String artifactId, String version);

    AppComponentDependencyTreeDO translateComponentDependency(JavaComponentDependencyTreeDO javaComponentDependencyTreeDO);

    List<AppDependencyTableDO> translateDependencyTable(List<JavaDependencyTableDO> javaDependencyTableDOS);
}
