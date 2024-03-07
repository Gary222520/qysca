package dao.component;

import domain.component.DependencyTableDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DependencyTableDao extends JpaRepository<DependencyTableDO, String> {

    /**
     * 根据gav查找组件的所有平铺依赖表
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return DependencyTreeDO 查找结果
     */
    List<DependencyTableDO> findAllByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}