package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DependencyTreeDao extends JpaRepository<DependencyTreeDO, String> {

    /**
     * 根据gav查找闭源组件依赖树
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return DependencyTreeDO 查找结果
     */
    DependencyTreeDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);


    /**
     *  根据gav删除闭源组件依赖树
     * @param groupId 组织ID
     * @param artifactId 工件ID
     * @param version 版本号
     */
    void deleteByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);


    /**
     *  根据ga删除闭源组件依赖树
     * @param groupId 组织Id
     * @param artifactId 工件Id
     */
    void deleteAllByGroupIdAndArtifactId(String groupId, String artifactId);
}
