package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;

public interface GradleService {
    /**
     * 构造gradle java组件
     * @param groupId 工件id
     * @param artifactId 组件id
     * @param version 版本号
     * @param type 组件类型
     * @return PythonComponentDO
     */
    JavaComponentDO componentAnalysis(String groupId, String artifactId, String version, String type);

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