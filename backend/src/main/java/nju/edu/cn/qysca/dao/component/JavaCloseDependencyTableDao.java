package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.JavaCloseDependencyTableDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseDependencyTableDao extends MongoRepository<JavaCloseDependencyTableDO, String> {
    /**
     * 根据gav分页查询闭源组件依赖平铺信息
     *
     * @param parentGroupId    组织id
     * @param parentArtifactId 工件id
     * @param parentVersion    版本号
     * @param pageable         分页信息
     * @return Page<JavaCloseDependencyTableDO> 分页查询结果
     */
    Page<JavaCloseDependencyTableDO> findByParentGroupIdAndParentArtifactIdAndParentVersion(String parentGroupId, String parentArtifactId, String parentVersion, Pageable pageable);
}
