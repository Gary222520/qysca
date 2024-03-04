//package nju.edu.cn.qysca.dao.component;
//
//import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
//import nju.edu.cn.qysca.domain.component.dos.JavaCloseDependencyTableDO;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.mongodb.repository.MongoRepository;
//import org.springframework.data.mongodb.repository.Query;
//
//public interface JavaCloseDependencyTableDao extends MongoRepository<JavaCloseDependencyTableDO, String> {
//    /**
//     * 根据gav分页查询闭源组件依赖平铺信息
//     *
//     * @param parentGroupId    组织id
//     * @param parentArtifactId 工件id
//     * @param parentVersion    版本号
//     * @param pageable         分页信息
//     * @return Page<JavaCloseDependencyTableDO> 分页查询结果
//     */
//    Page<JavaCloseDependencyTableDO> findByParentGroupIdAndParentArtifactIdAndParentVersion(String parentGroupId, String parentArtifactId, String parentVersion, Pageable pageable);
//
//    /**
//     * 根据gav分页查询闭源组件依赖平铺信息
//     *
//     * @param groupId    组织id
//     * @param artifactId 工件id
//     * @param version    版本号
//     * @param pageable   分页信息
//     * @return Page<ComponentTableDTO> 分页查询结果
//     */
//    @Query(value = "{parentGroupId:?0,parentArtifactId:?1,parentVersion:?2}",
//            fields = "{_id:0,parentGroupId:0,parentArtifactId:0,parentVersion:0}")
//    Page<ComponentTableDTO> findByGAV(String groupId, String artifactId, String version, Pageable pageable);
//
//}
