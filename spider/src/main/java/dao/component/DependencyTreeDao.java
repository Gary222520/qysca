package dao.component;

import domain.component.DependencyTreeDO;
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

}
