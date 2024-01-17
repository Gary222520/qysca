package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JavaOpenDependencyTreeDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaOpenDependencyTreeDao extends MongoRepository<JavaOpenDependencyTreeDO, String> {
    /**
     * 根据gav查找开源组件依赖树
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return JavaOpenDependencyTreeDO 查找结果
     */
    JavaOpenDependencyTreeDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

}
