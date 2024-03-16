package nju.edu.cn.qysca.dao.application;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDao extends JpaRepository<ApplicationDO, String> {
    /**
     *  根据groupId, artifactId, version查找应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本
     * @return ApplicationDO 应用信息
     */
    ApplicationDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     *  根据groupId, artifactId查找应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param pageable 分页信息
     * @return Page<ApplicationDO> 应用信息列表
     */
    Page<ApplicationDO> findAllByGroupIdAndArtifactId(String groupId, String artifactId, Pageable pageable);

    /**
     * 根据groupId, artifactId删除应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     */
    void deleteAllByGroupIdAndArtifactId(String groupId, String artifactId);

    /**
     *  根据groupId, artifactId, version删除应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本
     */
    void deleteByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);


    /**
     * 查询指定应用指定状态的版本数量
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param state 状态
     * @return Integer 版本数量
     */
    Integer countByGroupIdAndArtifactIdAndState(String groupId, String artifactId, String state);

    /**
     * 查询指定应用的版本信息
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return List<String> 指定应用的版本信息
     */
    @Query("select version from ApplicationDO where groupId = ?1 and artifactId = ?2 order by version desc")
    List<String> findVersionsByGroupIdAndArtifactId(String groupId, String artifactId);


    /**
     * 分页获取根应用 并返回根应用的最新版本
     * @param pageable 分页信息
     * @return Page<ApplicationDO> 应用分页信息
     */
    @Query(value = "select distinct on (a.group_id, a.artifact_id, a.name) a.* from application a where a.root = true",
            countQuery = "select count(*) from (select distinct a.group_id, a.artifact_id, a.name from  application a where a.root = true) as unique_combinations",
            nativeQuery = true)
    Page<ApplicationDO> findRootPage(Pageable pageable);

    /**
     * 模糊查询应用名称
     * @param name 应用名称
     * @return List<String> 模糊查询应用名称
     */
    @Query("select name from ApplicationDO where name like %?1%")
    List<String> searchApplicationName(String name);

    /**
     * 根据名称查询应用 并返回应用的最新版本
     * @param name 应用名称
     * @return ApplicationDO 应用信息
     */
    @Query(value = "select * from application where name = :name order by version desc limit 1", nativeQuery = true)
    ApplicationDO findApplication(String name);

    /**
     * 根据应用Id查询子应用
     * @param applicationId 应用Id
     * @return List<ApplicationDO> 子应用列表
     */
    @Query(value = "select a.* from application a where a.id = ANY (select unnest(child_application) from application where id = :applicationId) order by name desc", nativeQuery = true)
    List<ApplicationDO> findSubApplication(String applicationId);

    /**
     * 根据Id查找应用
     * @param id 应用Id
     */
    ApplicationDO findApplicationDOById(String id);


    /**
     * 根据Id查找其父应用
     * @param id 应用Id
     * @return List<ApplicationDO> 父应用列表
     */
    @Query(value = "select * from application where :id = any(child_application)", nativeQuery = true)
    List<ApplicationDO> findParentApplication(String id);
}
