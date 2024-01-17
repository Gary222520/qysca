package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.JavaCloseComponentDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseComponentDao extends MongoRepository<JavaCloseComponentDO, String> {
    /**
     * 根据gav查询闭源组件信息
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return JavaCloseComponentDO 闭源组件详细信息
     */
    JavaCloseComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     * 根据gav删除闭源组件信息
     *
     * @param groupId    组件id
     * @param artifactId 工件id
     * @param version    版本号
     */
    void deleteByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
