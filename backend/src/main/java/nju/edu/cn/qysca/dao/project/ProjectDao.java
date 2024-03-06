package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectDao extends JpaRepository<ProjectDO, String> {
    /**
     *  根据groupId, artifactId, version查找项目
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 项目版本
     * @return ProjectDO 项目信息
     */
    ProjectDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     *  根据groupId, artifactId查找项目
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param pageable 分页信息
     * @return 项目信息列表
     */
    Page<ProjectDO> findAllByGroupIdAndArtifactId(String groupId, String artifactId, Pageable pageable);

    /**
     * 根据groupId, artifactId删除项目
     * @param groupId 组织Id
     * @param artifactId 工件Id
     */
    void deleteAllByGroupIdAndArtifactId(String groupId, String artifactId);

    /**
     *  根据groupId, artifactId, version删除项目
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 项目版本
     */
    void deleteByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);


    /**
     * 查询指定项目指定状态的版本数量
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param state 状态
     * @return Integer 版本数量
     */
    Integer countByGroupIdAndArtifactIdAndState(String groupId, String artifactId, String state);

    /**
     * 查询指定项目的版本信息
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return 指定项目的版本信息
     */
    @Query("select version from ProjectDO where groupId = ?1 and artifactId = ?2 order by version desc")
    List<String> findVersionsByGroupIdAndArtifactId(String groupId, String artifactId);
}
