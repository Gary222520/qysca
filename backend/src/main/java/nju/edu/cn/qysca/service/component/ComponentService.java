package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.domain.component.*;
import org.springframework.data.domain.Page;

public interface ComponentService {
    /**
     * 分页查询开源组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaOpenComponentDO> 查询结果
     */
    Page<JavaOpenComponentDO> findOpenComponentsPage(ComponentSearchDTO searchComponentDTO);

    /**
     * 分页查询闭源组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaCloseComponentDO> 查询结果
     */
    Page<JavaCloseComponentDO> findCloseComponentsPage(ComponentSearchDTO searchComponentDTO);

    Boolean saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO);

    /**
     * 查询开源组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaOpenDependencyTreeDO 依赖树信息
     */
    JavaOpenDependencyTreeDO findOpenComponentDependencyTree(ComponentGavDTO componentGavDTO);

    /**
     * 查询闭源组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaCloseDependencyTreeDO 依赖树信息
     */
    JavaCloseDependencyTreeDO findCloseComponentDependencyTree(ComponentGavDTO componentGavDTO);


    /**
     * 分页查询开源组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<JavaOpenDependencyTableDO> 分页查询结果
     */
    Page<JavaOpenDependencyTableDO> findOpenComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO);

    /**
     * 分页查询闭源组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<JavaCloseDependencyTableDO> 分页查询结果
     */
    Page<JavaCloseDependencyTableDO> findCloseComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO);

    /**
     * 查询指定开源组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaOpenComponentDO 开源组件详细信息
     */
    JavaOpenComponentDO findOpenComponentDetail(ComponentGavDTO componentGavDTO);

    /**
     * 查询指定闭源组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaCloseComponentDO 闭源组件详细信息
     */
    JavaCloseComponentDO findCloseComponentDetail(ComponentGavDTO componentGavDTO);
}