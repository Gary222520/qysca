package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsComponentDao extends JpaRepository<JsComponentDO, String> {

    /**
     * 根据名称，版本号查询组件
     * @param name 名称
     * @param version 版本号
     * @return JsComponentDO 组件信息
     */
    JsComponentDO findByNameAndVersion(String name, String version);

    /**
     *  根据组件id列表查询组件列表
     * @param ids 组件Id
     * @return List<JsComponentDO> 组件列表
     */
    List<JsComponentDO> findByIdIn(List<String> ids);


    /**
     * 根据组件id查询组件
     * @param id 组件Id
     * @return JsComponentDO js组件
     */
    JsComponentDO findJsComponentDOById(String id);



    /**
     *  根据组件名称模糊查询
     * @param name 组件名称
     * @return List<String> 组件名称模糊查询列表
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO(c.name, c.version) from JsComponentDO c where c.name like %:name%")
    List<ComponentSearchNameDTO> searchComponentName(String name);

    /**
     * 分页查询Js组件
     * @param name 名称
     * @param version 版本号
     * @param type 类型
     * @param pageable 分页信息
     * @return Page<JsComponentDO> Js组件分页信息
     */
    @Query(value = "select * from plt_js_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            countQuery = "select count(*) from plt_js_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            nativeQuery = true
    )
    Page<JsComponentDO> findComponentsPage(String name, String version, String type, Pageable pageable);


    /**
     * 根据名称和版本删除
     * @param name 名称
     * @param version 版本
     */
    void deleteByNameAndVersion(String name, String version);
}
