package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JavaCloseDependencyTreeDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseDependencyTreeDao extends MongoRepository<JavaCloseDependencyTreeDO, String> {
    /**
     * 根据gav查找闭源组件依赖树
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return JavaCloseDependencyTreeDO 查找结果
     */
    JavaCloseDependencyTreeDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
