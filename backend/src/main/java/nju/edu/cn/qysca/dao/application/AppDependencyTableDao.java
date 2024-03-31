package nju.edu.cn.qysca.dao.application;

import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.application.dtos.AppComponentTableDTO;
import nju.edu.cn.qysca.domain.application.dtos.TableExcelBriefDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AppDependencyTableDao extends JpaRepository<AppDependencyTableDO, String> {

    /**
     * 根据名称和版本删除组件依赖信息表
     * @param name 名称
     * @param version 版本
     */
    void deleteAllByNameAndVersion(String name, String version);

    /**
     * 根据名称和版本查询应用依赖信息
     * @param name 名称
     * @param version 版本
     * @param pageable 分页
     * @return Page<AppComponentTableDTO> 应用依赖信息表
     */
    @Query("select new nju.edu.cn.qysca.domain.application.dtos.AppComponentTableDTO(a.cName, a.cVersion, a.depth, a.type, a.language, a.direct) from AppDependencyTableDO a where a.name = :name and a.version = :version")
    Page<AppComponentTableDTO> findByNameAndVersion(String name, String version, Pageable pageable);

    /**
     *  根据名称和版本查询应用依赖信息
     * @param name 名称
     * @param version 版本号
     * @return List<TableExcelBriefDTO> 应用依赖信息
     */
    @Query("select new nju.edu.cn.qysca.domain.application.dtos.TableExcelBriefDTO(a.cName, a.cVersion, a.language, a.direct, a.depth, a.type) from AppDependencyTableDO a where a.name = :name and a.version = :version")
    List<TableExcelBriefDTO> findTableListByNameAndVersion(String name, String version);


    /**
     * 根据名称和版本查询应用依赖信息
     * @param name 名称
     * @param version 版本
     * @return List<AppDependencyTableDO> 应用依赖信息
     */
    List<AppDependencyTableDO> findAllByNameAndVersion(String name, String version);
}
