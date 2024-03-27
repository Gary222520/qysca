package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsComponentDao extends JpaRepository<JsComponentDO, String> {

    JsComponentDO findByNamespaceAndArtifactIdAndVersion(String namespace, String artifactId, String version);
}
