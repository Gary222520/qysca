package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.JavaOpenComponentDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OpenSourceComponentDao extends MongoRepository<JavaOpenComponentDO, String> {

    JavaOpenComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
