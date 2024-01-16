package nju.edu.cn.qysca.service.component;

import nju.edu.cn.qysca.domain.component.JavaCloseComponentDO;
import nju.edu.cn.qysca.domain.component.ComponentSearchDTO;
import nju.edu.cn.qysca.domain.component.JavaOpenComponentDO;
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
}
