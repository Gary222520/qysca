package nju.edu.cn.qysca.dao.components;

import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JavaComponentDao extends Neo4jRepository<JavaComponentDO, String> {

    /**
     * 非递归查询指定GAV的结点信息
     *
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return JavaComponentDO 查询结果
     */
    @Query("MATCH (n:JavaComponent) WHERE n.groupId = $groupId AND n.artifactId = $artifactId AND n.version = $version RETURN n LIMIT 1")
    JavaComponentDO findNodeByGAV(@Param("groupId") String groupId,
                                  @Param("artifactId") String artifactId,
                                  @Param("version") String version);


    /**
     * 查询指定GAV的邻接依赖结点信息
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return List<JavaComponentDO> 查询结果
     */
    @Query("MATCH (:JavaComponent{groupId:$groupId,artifactId:$artifactId,version:$version})-[:depends]->(m:JavaComponent) RETURN m")
    List<JavaComponentDO> findAdjacentDependencyByGAV(@Param("groupId") String groupId,
                                              @Param("artifactId") String artifactId,
                                              @Param("version") String version);

    /**
     * 查询指定GAV的邻接父母结点信息
     * @param groupId    组织id
     * @param artifactId 工件id
     * @param version    版本号
     * @return List<JavaComponentDO> 查询结果
     */
    @Query("MATCH (:JavaComponent{groupId:$groupId,artifactId:$artifactId,version:$version})-[:hasParent]->(m:JavaComponent) RETURN m")
    List<JavaComponentDO> findAdjacentParentByGAV(@Param("groupId") String groupId,
                                                        @Param("artifactId") String artifactId,
                                                        @Param("version") String version);


    /**
     * 通过指定GAV查找邻接的关系和节点信息
     * @param groupId 组织id
     * @param artifactId 工件id
     * @param version 版本号
     * @return JavaComponentDO 查询结果
     */
    @Query("MATCH (n:JavaComponent{groupId:$groupId,artifactId:$artifactId,version:$version})-[r:depends]->(m:JavaComponent) RETURN n,collect(r),collect(m)")
    JavaComponentDO findAdjacentRNByGAV(@Param("groupId") String groupId,
                                                  @Param("artifactId") String artifactId,
                                                  @Param("version") String version);
}
