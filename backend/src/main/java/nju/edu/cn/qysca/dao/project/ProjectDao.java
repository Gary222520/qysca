package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
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


    /**
     * 分页获取根项目 并返回根项目的最新版本
     * @param pageable 分页信息
     * @return Page<ProjectDO> 项目分页信息
     */
    @Query(value = "select distinct on (p.group_id, p.artifact_id, p.name) p.* from project p where p.root = true",
            countQuery = "select count(*) from (select distinct p.group_id, p.artifact_id, p.name from  project p where p.root = true) as unique_combinations",
            nativeQuery = true)
    Page<ProjectDO> findRootPage(Pageable pageable);

    /**
     * 模糊查询项目名称
     * @param name 项目名称
     * @return List<String> 模糊查询项目名称
     */
    @Query("select name from ProjectDO where name like %?1%")
    List<String> searchProjectName(String name);

    /**
     * 根据名称查询项目 并返回项目的最新版本
     * @param name 项目名称
     * @return ProjectDO 项目信息
     */
    @Query(value = "select * from project where name = :name order by version desc limit 1", nativeQuery = true)
    ProjectDO findProject(String name);

    /**
     * 根据项目Id查询子项目
     * @param projectId 项目Id
     * @return List<ProjectDO> 子项目列表
     */
    @Query(value = "select p.* from project p where p.id = ANY (select unnest(child_project) from project where id = :projectId)", nativeQuery = true)
    List<ProjectDO> findSubProject(String projectId);

    /**
     * 根据Id查找项目
     * @param id 项目Id
     */
    ProjectDO findProjectDOById(String id);


    /**
     * 根据Id查找其父项目
     * @param id 项目Id
     * @return List<ProjectDO> 父项目列表
     */
    @Query(value = "select * from project where :id = any(unnest(child_project))", nativeQuery = true)
    List<ProjectDO> findParentProject(String id);
}
