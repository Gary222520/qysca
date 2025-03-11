package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PythonComponentDao extends JpaRepository<PythonComponentDO, String> {
    /**
     *  根据名称和版本号查找组件
     * @param name 名称
     * @param version 版本号
     * @return PythonComponentDO python组件信息
     */
    PythonComponentDO findByNameAndVersion(String name, String version);

    /**
     * 根据Id查询组件列表
     * @param ids id
     * @return List<PythonComponentDO> python组件信息
     */
    List<PythonComponentDO> findByIdIn(List<String> ids);

    /**
     * 根据Id查询组件
     * @param id id
     * @return PythonComponentDO python组件信息
     */
    PythonComponentDO findPythonComponentDOById(String id);


    /**
     * 根据名称模糊查询
     * @param name 名称
     * @return List<ComponentSearchNameDTO> 组件名称和版本
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO(c.name, c.version) from PythonComponentDO c where c.name like %:name%")
    List<ComponentSearchNameDTO> searchComponentName(String name);

    /**
     * 分页查询Python组件
     * @param name 名称
     * @param version 版本
     * @param type 类型
     * @param pageable 分页信息
     * @return Page<PythonComponentDO> Python组件
     */
    @Query(value = "select * from plt_python_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            countQuery = "select count(*) from plt_python_component where (:name = '' or name = :name) and (:version = '' or version =:version)and (:type = '' or type = :type)",
            nativeQuery = true
    )
    Page<PythonComponentDO> findComponentsPage(String name, String version, String type, Pageable pageable);

    /**
     * 根据名称和版本号删除
     * @param name 名称
     * @param version 版本号
     */
    void deleteByNameAndVersion(String name, String version);
}
