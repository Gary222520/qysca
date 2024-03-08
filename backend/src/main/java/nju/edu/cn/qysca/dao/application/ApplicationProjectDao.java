package nju.edu.cn.qysca.dao.application;

import nju.edu.cn.qysca.domain.application.dos.ApplicationProjectDO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationProjectDao extends JpaRepository<ApplicationProjectDO, String> {

    /**
     * 根据应用Gav查询项目信息
     * @param groupId 应用组织Id
     * @param artifactId 应用工件Id
     * @param version 应用版本
     * @return List<ProjectDO> 项目列表
     */
    @Query("select ap.projectDO from ApplicationProjectDO ap where ap.applicationDO.groupId = :groupId and ap.applicationDO.artifactId = :artifactId and ap.applicationDO.version = :version")
    List<ProjectDO> findProjectByApplicationGAV(String groupId, String artifactId, String version);

    /**
     *  根据应用Id和项目Id删除应用项目关联
     * @param applicationId 应用Id
     * @param projectId 项目Id
     * @return
     */
    void deleteByApplicationDO_IdAndProjectDO_Id(String applicationId, String projectId);

    /**
     *  根据应用Id删除应用项目关联
     * @param applicationId 应用Id
     */
    void deleteAllByApplicationDO_Id(String applicationId);
}
