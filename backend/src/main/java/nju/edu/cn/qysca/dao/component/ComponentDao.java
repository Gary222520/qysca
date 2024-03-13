package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentDao extends JpaRepository<ComponentDO, String> {
    /**
     * 根据gav查询开源组件信息
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return ComponentDO 开源组件详细信息
     */
    ComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     * 根据项目Id查询子组件
     * @param projectId 项目Id
     * @return List<ComponentDO> 子组件列表
     */
    @Query(value = "select * from component where id = any (select unnest(child_component) from project where id = :projectId)", nativeQuery = true)
    List<ComponentDO> findSubComponent(String projectId);
}
