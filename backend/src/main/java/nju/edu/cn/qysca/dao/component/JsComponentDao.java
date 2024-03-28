package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsComponentDao extends JpaRepository<JsComponentDO, String> {

    /**
     * 根据命名空间、组件Id、版本号查询组件
     * @param namespace 命名空间
     * @param artifactId 工件id
     * @param version 版本号
     * @return JsComponentDO 组件信息
     */
    JsComponentDO findByNamespaceAndArtifactIdAndVersion(String namespace, String artifactId, String version);

    /**
     *  根据组件id列表查询组件列表
     * @param ids 组件Id
     * @return List<JsComponentDO> 组件列表
     */
    List<JsComponentDO> findByIdIn(List<String> ids);
}
