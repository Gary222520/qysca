package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GoComponentDao extends JpaRepository<GoComponentDO,String> {
    /**
     * 根据名称和版本查询Go组件信息
     * @param name 组件名称
     * @param version 组件版本
     * @return Go组件信息
     */
    GoComponentDO findByNameAndVersion(String name,String version);

    /**
     * 根据Id查询Go组件信息
     * @param ids Id
     * @return List<GoComponentDO> Go组件信息
     */
    List<GoComponentDO> findByIdIn(List<String> ids);

    /**
     *  根据Id查询Go组件信息
     * @param id 组件Id
     * @return  GoComponentDO Go组件信息
     */
    GoComponentDO findGoComponentDOById(String id);

    /**
     * 根据名称查询Go组件信息
     * @param name 名称
     * @return List<ComponentSearchNameDTO> go组件信息
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO(c.name, c.version) from GoComponentDO c where c.name like %:name%")
    List<ComponentSearchNameDTO> searchComponentName(String name);

    /**
     * 分页查询Go组件
     * @param name 名称
     * @param version 版本
     * @param type 类型
     * @param pageable 分页信息
     * @return Page<GoComponentDO> Go组件
     */
    @Query(value = "select * from plt_go_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            countQuery = "select count(*) from plt_go_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            nativeQuery = true
    )
    Page<GoComponentDO> findComponentsPage(String name, String version, String type, Pageable pageable);

    /**
     * 根据名称和版本号删除
     * @param name 名称
     * @param version 版本号
     */
    void deleteByNameAndVersion(String name, String version);
}
