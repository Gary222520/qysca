package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.project.dtos.TableExcelDetailDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
