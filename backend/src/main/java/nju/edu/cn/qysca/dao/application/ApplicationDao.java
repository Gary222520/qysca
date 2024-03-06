package nju.edu.cn.qysca.dao.application;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
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
     * 根据groupId和artifactId查询应用
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return List<ApplicationDO> 应用列表
     */
    List<ApplicationDO> findAllByGroupIdAndArtifactId(String groupId, String artifactId);

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
    @Query("select version from ApplicationDO where groupId = ?1 and artifactId = ?2")
    List<String> findVersionsByGroupIdAndArtifactId(String groupId, String artifactId);


    /**
     *  根据groupId、artifactId和version查询应用带有项目信息
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本号
     * @return ApplicationDO 带有项目信息
     */
    @Query("select a from ApplicationDO a left join FETCH a.projects where a.groupId = ?1 and a.artifactId = ?2 and a.version = ?3")
    ApplicationDO  findByGroupIdAndArtifactIdAndVersionWithProject(String groupId, String artifactId, String version);
}
