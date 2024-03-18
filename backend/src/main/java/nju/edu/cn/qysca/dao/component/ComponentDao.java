package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO;
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
     * 根据应用Id查询子组件
     * @param applicationId 应用Id
     * @return List<ComponentDO> 子组件列表
     */
    @Query(value = "select * from component where id = any (select unnest(child_component) from application where id = :applicationId) order by name desc", nativeQuery = true)
    List<ComponentDO> findSubComponent(String applicationId);

    /**
     * 根据groupId、artifactId、version删除组件信息
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本
     */
    void deleteByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     *  根据id查询组件信息
     * @param id 开源组件Id
     * @return ComponentDO 组件信息
     */
    ComponentDO findComponentDOById(String id);

    /**
     *  根据组件名称模糊查询
     * @param name 组件名称
     * @return List<String> 组件名称模糊查询列表
     */
    @Query("select new nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO(c.groupId, c.artifactId, c.version, c.name) from ComponentDO c where c.name like %:name%")
    List<ComponentSearchNameDTO> searchComponentName(String name);
}
