package nju.edu.cn.qysca.dao.components;

import nju.edu.cn.qysca.domain.components.JavaCloseComponentDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseComponentDao extends MongoRepository<JavaCloseComponentDO, String> {

    JavaCloseComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
