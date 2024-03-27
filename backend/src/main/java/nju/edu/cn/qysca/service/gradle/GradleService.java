package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.domain.component.dos.JavaComponentDependencyTreeDO;

public interface GradleService {
    JavaComponentDependencyTreeDO projectDependencyAnalysis(String filePath);
}
