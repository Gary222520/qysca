package nju.edu.cn.qysca.service.go;

import nju.edu.cn.qysca.domain.component.dos.*;

import java.util.List;

public interface GoService {
    /**
     * 根据给定的名称、版本和类型构造GO组件
     * @param name 组件名称
     * @param version 版本号
     * @param type 组件类型
     * @return Go组件
     */
    GoComponentDO componentAnalysis(String name,String version,String type);

    /**
     * 根据给定的信息解析Go依赖树
     * @param name 根组件名称
     * @param version 根组件版本号
     * @param type 根组件类型
     * @param filePath 扫描文件路径
     * @param builder 扫描方式
     * @return Go依赖树
     */
    GoDependencyTreeDO dependencyTreeAnalysis(String name, String version, String type, String filePath, String builder);

    /**
     * 根据给定的Go依赖树解析Go依赖平铺信息表
     * @param tree Go依赖树
     * @return Go依赖平铺信息表
     */
    List<GoDependencyTableDO> dependencyTableAnalysis(GoDependencyTreeDO tree);

    /**
     * 根据给定的名称和版本爬取并解析Go组件依赖树
     * @param name 名称
     * @param version 版本
     * @return Go组件依赖树
     */
    GoDependencyTreeDO spiderDependency(String name, String version);

    /**
     * 爬虫获取并填充Go组件信息
     * @param name 组件名称
     * @param version 组件版本
     * @return Go组件信息
     */
    GoComponentDO spiderComponentInfo(String name,String version);
}
