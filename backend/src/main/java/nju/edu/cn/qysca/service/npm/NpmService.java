package nju.edu.cn.qysca.service.npm;

import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;

import java.util.List;

public interface NpmService {

    JavaComponentDO componentAnalysis(String filePath);

    JavaDependencyTreeDO dependencyTreeAnalysis(String packagePath, String filePath);

    List<JavaDependencyTableDO> dependencyTableAnalysis(JavaDependencyTreeDO javaDependencyTreeDO);
}
