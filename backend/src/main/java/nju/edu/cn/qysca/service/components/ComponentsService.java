package nju.edu.cn.qysca.service.components;

import nju.edu.cn.qysca.domain.components.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.components.ComponentGavDTO;
import nju.edu.cn.qysca.domain.components.JavaComponentDO;

public interface ComponentsService {

    /**
     * 根据组件GAV获取组件依赖树
     * @param componentGavDTO 组件GAV信息DTO
     * @return ComponentDependencyTreeDO 组件依赖树DO
     */
     ComponentDependencyTreeDO getComponentTreeByGAV(ComponentGavDTO componentGavDTO);
}
