package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import org.springframework.data.domain.Page;

public interface ComponentService {

    /**
     * 分页查询组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<ComponentDO> 查询结果
     */
    Page<ComponentDO> findComponentsPage(ComponentSearchDTO searchComponentDTO);

    /**
     * 保存闭源组件信息
     *
     * @param saveCloseComponentDTO 保存闭源组件接口信息
     * @return 返回保存闭源组件是否成功
     */
    Boolean saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO);


    /**
     * 查询组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return DependencyTreeDO 依赖树信息
     */
    DependencyTreeDO findComponentDependencyTree(ComponentGavDTO componentGavDTO);



    /**
     * 分页查询组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<ComponentTableDTO> 分页查询结果
     */
    Page<ComponentTableDTO> findComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO);

    /**
     * 查询指定组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return ComponentDetailDTO 组件详细信息
     */
    ComponentDetailDTO findComponentDetail(ComponentGavDTO componentGavDTO);
}