package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JavaComponentDao extends JpaRepository<JavaComponentDO, String> {
    /**
     * 根据名称和版本号查询开源组件信息
     *
     * @param name 名称
     * @param version    版本号
     * @return JavaComponentDO 开源组件详细信息
     */
    JavaComponentDO findByNameAndVersion(String name, String version);


    /**
     * 根据name, version删除组件信息
     * @param name 名称
     * @param version 版本
     */
    void deleteByNameAndVersion(String name, String version);

    /**
     * 根据id查询组件信息
     * @param id 开源组件Id
     * @return JavaComponentDO 组件信息
     */
    JavaComponentDO findComponentDOById(String id);

    /**
     * 根据组件名称模糊查询
     * @param name 组件名称
     * @return List<String> 组件名称模糊查询列表
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO(c.name, c.version) from JavaComponentDO c where c.name like %:name%")
    List<ComponentSearchNameDTO> searchComponentName(String name);


    /**
     *  根据id列表查询组件信息
     * @param ids Id列表
     * @return List<JavaComponentDO> 组件信息列表
     */
    List<JavaComponentDO> findByIdIn(List<String> ids);

    /**
     * 分页查询Java组件信息
     * @param name 名称
     * @param version 版本号
     * @param type 类型
     * @param pageable 分页信息
     * @return Page<JavaComponentDO> Java组件信息
     */
    @Query(value = "select * from plt_java_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            countQuery = "select count(*) from plt_java_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            nativeQuery = true
    )
    Page<JavaComponentDO> findComponentsPage(String name, String version, String type, Pageable pageable);
}
