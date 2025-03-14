package nju.edu.cn.qysca.service.python;

import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTreeDO;

import java.util.List;

public interface PythonService {

    /**
     * 构造python组件
     * @param name 组件名称
     * @param version 版本号
     * @param type 组件类型
     * @return PythonComponentDO
     */
    PythonComponentDO componentAnalysis(String name, String version, String type);

    /**
     * 分析上传项目依赖，获得组件依赖树
     * @param filePath 上传文件路径
     * @param builder 构造器 ，例如 zip
     * @param name 组件名
     * @param version 组件版本
     * @param type 组件类型，例如 opensource
     * @return PythonDependencyTreeDO
     */
    PythonDependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String name, String version, String type);

    /**
     * 根据依赖树生成平铺依赖表
     * @param pythonDependencyTreeDO PythonDependencyTreeDO
     * @return List<PythonDependencyTableDO>
     */
    List<PythonDependencyTableDO> dependencyTableAnalysis(PythonDependencyTreeDO pythonDependencyTreeDO);

    /**
     * 根据name和version爬取组件的依赖信息并生成依赖树
     *
     * @param name 组件名
     * @param version 版本号
     * @return PythonDependencyTreeDO
     */
    PythonDependencyTreeDO spiderDependency(String name, String version);


    AppComponentDependencyTreeDO translateComponentDependency(PythonComponentDependencyTreeDO pythonComponentDependencyTreeDO);

    List<AppDependencyTableDO> translateDependencyTable(List<PythonDependencyTableDO> pythonDependencyTableDOS);
}
