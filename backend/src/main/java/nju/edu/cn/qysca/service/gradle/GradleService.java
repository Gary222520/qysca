package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;

import java.util.List;

public interface GradleService {
    /**
     *  解析上传文件的依赖信息，并生成依赖树
     * @param filePath 上传文件路径
     * @param builder 构造器， 例如 zip
     * @param groupId 组织id
     * @param artifactId 工件id
     * @param version 版本号
     * @param type 类型，例如 opensource
     * @return JavaDependencyTreeDO
     */
    JavaDependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String groupId, String artifactId, String version, String type);
}
