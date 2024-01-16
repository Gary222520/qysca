package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.ProjectVersionDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectVersionDao extends MongoRepository<ProjectVersionDO, String> {
    /**
     * 分页查询指定项目的版本信息
     *
     * @param name     项目名称
     * @param pageable 分页信息
     * @return Page<ProjectVersionDO> 分页查询结果
     */
    Page<ProjectVersionDO> findAllByName(String name, Pageable pageable);

    /**
     * 查询指定项目指定版本的详细信息
     *
     * @param name    项目名称
     * @param version 版本号
     * @return ProjectVersionDO 版本详细信息
     */
    ProjectVersionDO findByNameAndVersion(String name, String version);

    /**
     * 查询指定项目指定状态的版本数量
     *
     * @param name  项目名称
     * @param state 状态
     * @return Integer 版本数量
     */
    Integer countByNameAndState(String name, String state);

    /**
     * 查询指定项目的版本号列表
     *
     * @param name 项目名称
     * @param sort 排序规则
     * @return List<String> 版本号列表
     */
    @Query(value = "{name:?0}", fields = "{'_id':0,'version':1}")
    List<String> findVersionsByName(String name, Sort sort);
}
