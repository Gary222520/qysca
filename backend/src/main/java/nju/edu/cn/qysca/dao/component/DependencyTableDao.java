package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.DependencyTableDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.project.dtos.TableExcelBriefDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface DependencyTableDao extends JpaRepository<DependencyTableDO, String> {

    /**
     *  根据GA信息删除平铺信息
     * @param groupId 组织Id
     * @param artifactId 工件Id
     */

    void deleteAllByGroupIdAndArtifactId(String groupId, String artifactId);

    /**
     * 根据GAV信息删除平铺信息
     * @param groupId 父组件groupId
     * @param artifactId 父组件artifactId
     * @param version 父组件version
     */
    void deleteAllByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);



    /**
     * 根据gav分页查询组件依赖平铺信息
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @param pageable   分页信息
     * @return Page<ComponentTableDTO> 分页查询结果
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO(d.cGroupId, d.cArtifactId, d.cVersion, d.scope, d.depth, d.opensource, d.language, d.opensource) from DependencyTableDO d where d.groupId = :groupId and d.artifactId = :artifactId and d.version = :version")
    Page<ComponentTableDTO> findByGroupIdAndArtifactIdAndVersion(@Param("groupId")String groupId, @Param("artifactId") String artifactId, @Param("version") String version, Pageable pageable);

    @Query("SELECT new nju.edu.cn.qysca.domain.project.dtos.TableExcelBriefDTO(d.cGroupId, d.cArtifactId, d.cVersion, d.language, d.direct, d.depth, d.scope, d.opensource) FROM DependencyTableDO d where d.groupId = :groupId and d.artifactId = :artifactId and d.version = :version")
    List<TableExcelBriefDTO>  findTableListByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
