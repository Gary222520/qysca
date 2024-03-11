package nju.edu.cn.qysca_spider.dao;

import nju.edu.cn.qysca_spider.domain.ComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComponentDao extends JpaRepository<ComponentDO, String> {
    /**
     * 根据gav查找组件信息
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return ComponentDO 查找结果
     */
    ComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
