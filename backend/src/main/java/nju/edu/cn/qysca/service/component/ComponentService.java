package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ComponentService {

    /**
     * 分页查询组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaComponentDO> 查询结果
     */
    Page<? extends ComponentDO> findComponentsPage(ComponentSearchDTO searchComponentDTO);

    /**
     * 保存闭源组件信息
     * @param saveCloseComponentDTO 保存闭源组件接口信息
     */
    void saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO);

    /**
     * 更新闭源组件信息
     * @param updateCloseComponentDTO 更新闭源组件接口信息
     * @return 更新闭源组件是否成功
     */
    void updateCloseComponent(UpdateCloseComponentDTO updateCloseComponentDTO);

    /**
     * 删除闭源组件
     * @param deleteCloseComponentDTO 删除闭源组件信息接口
     * @return
     */
    List<ApplicationDO> deleteCloseComponent(DeleteCloseComponentDTO deleteCloseComponentDTO);


    /**
     * 查询组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaDependencyTreeDO 依赖树信息
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

    /**
     * 模糊查询组件名称
     * @param name 组件名称
     * @param language 语言
     */
    List<ComponentSearchNameDTO> searchComponentName(String name, String language);

    /**
     * 保存闭源组件依赖信息
     * @param saveCloseComponentDTO 保存闭源组件信息
     */
    void saveCloseComponentDependency(SaveCloseComponentDTO saveCloseComponentDTO);

    /**
     * 将闭源组件状态设置为RUNNING
     * @param updateCloseComponentDTO 更新闭源组件信息接口
     * @return Boolean 状态设置是否成功
     */
    Boolean changeCloseComponentState(UpdateCloseComponentDTO updateCloseComponentDTO);
}