package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.JavaCloseComponentDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseComponentDao extends MongoRepository<JavaCloseComponentDO, String> {

    JavaCloseComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);
}
