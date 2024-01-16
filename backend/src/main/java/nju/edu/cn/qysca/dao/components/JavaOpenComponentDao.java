package nju.edu.cn.qysca.dao.components;

import nju.edu.cn.qysca.domain.components.JavaOpenComponentDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaOpenComponentDao extends MongoRepository<JavaOpenComponentDO, String> {

    JavaOpenComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
