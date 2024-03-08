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
     *  根据groupId、artifactId和version删除应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本号
     */
    void deleteByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     * 列出所有项目的最新版本
     * @param pageable 分页信息
     * @return Page<ApplicationDO> 所有项目的最新版本
     */
    @Query(value = "select a.* from (select *, ROW_NUMBER() OVER (PARTITION BY group_id, artifact_id ORDER BY version DESC) AS rn from application) a where a.rn = 1", countQuery = "select count(distinct group_id, artifact_id) from application", nativeQuery = true)
    Page<ApplicationDO> findLatestVersionOfAllProjects(Pageable pageable);


    /**
     *  根据groupId、artifactId和version查询应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本号
     * @return ApplicationDO 应用
     */

    ApplicationDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     * 根据groupId和artifactId查询应用的版本号
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return List<String> 版本号列表
     */
    @Query("select version from ApplicationDO where groupId = ?1 and artifactId = ?2 order by version desc")
    List<String> findVersionsByGroupIdAndArtifactId(String groupId, String artifactId);

    /**
     * 根据groupId和artifactId查询应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return List<ApplicationDO> 应用列表
     */
    List<ApplicationDO> findAllByGroupIdAndArtifactId(String groupId, String artifactId);
}
