package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dtos.ComponentDetailDTO;
import nju.edu.cn.qysca.domain.component.dos.JavaOpenComponentDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaOpenComponentDao extends MongoRepository<JavaOpenComponentDO, String> {
    /**
     * 根据gav查询开源组件信息
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return JavaOpenComponentDO 开源组件详细信息
     */
    JavaOpenComponentDO findByGroupIdAndArtifactIdAndVersion(String groupId, String artifactId, String version);

    /**
     * 根据gav查询开源组件详细信息
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return ComponentDetailDTO 详细信息
     */
    @Query(value = "{groupId:?0,artifactId:?1,version:?2}", fields = "{_id:0,pom:0}")
    ComponentDetailDTO findDetailByGav(String groupId, String artifactId, String version);

}
