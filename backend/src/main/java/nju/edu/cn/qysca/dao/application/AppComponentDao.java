package nju.edu.cn.qysca.dao.application;

import nju.edu.cn.qysca.domain.application.dos.AppComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppComponentDao extends JpaRepository<AppComponentDO, String> {

    /**
     * 根据名称和版本号删除
     * @param name 名称
     * @param version 版本号
     */
    void deleteByNameAndVersion(String name, String version);

    /**
     * 根据名称和版本号查找
     * @param name 名称
     * @param version 版本号
     * @return AppComponentDO 应用组件信息
     */
    AppComponentDO findByNameAndVersion(String name, String version);

    /**
     * 分页查询Java组件信息
     * @param name 名称
     * @param version 版本号
     * @param type 类型
     * @param pageable 分页信息
     * @return Page<AppComponentDO> App组件信息
     */
    @Query(value = "select * from plt_app_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            countQuery = "select count(*) from plt_app_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            nativeQuery = true
    )
    Page<AppComponentDO> findComponentsPage(String name, String version, String type, Pageable pageable);

    /**
     * 根据组件名称模糊查询
     * @param name 组件名称
     * @return List<String> 组件名称模糊查询列表
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO(c.version, c.name) from AppComponentDO c where c.name like %:name%")
    List<ComponentSearchNameDTO> searchComponentName(String name);
}
