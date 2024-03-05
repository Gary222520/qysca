package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
