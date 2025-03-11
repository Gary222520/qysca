package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JsDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsDependencyTableDao extends JpaRepository<JsDependencyTableDO, String> {

    /**
     * 根据名称和版本号删除
     * @param name 名称
     * @param version 版本号
     */
    void deleteAllByNameAndVersion(String name, String version);

    /**
     * 根据name, version分页查询组件依赖平铺信息
     *
     * @param name     组件name
     * @param version    版本号
     * @param pageable   分页信息
     * @return Page<ComponentTableDTO> 分页查询结果
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO(d.cName, d.cVersion, d.depth, d.type, d.language, d.direct) from JsDependencyTableDO d where d.name = :name and d.version = :version")
    Page<ComponentTableDTO> findByNameAndVersion(@Param("name") String name, @Param("version") String version, Pageable pageable);

    /**
     * 根据名称和版本号查找
     * @param name 名称
     * @param version 版本号
     */
    List<JsDependencyTableDO> findAllByNameAndVersion(String name, String version);
}
