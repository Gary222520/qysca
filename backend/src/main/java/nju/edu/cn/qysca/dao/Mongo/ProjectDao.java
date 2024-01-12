package nju.edu.cn.qysca.dao.Mongo;

import nju.edu.cn.qysca.domain.project.ProjectDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProjectDao extends MongoRepository<ProjectDO, String> {

    // 查询具体项目 无需@Query注解
    ProjectDO findProjectDOByNameAndVersion(String name, String version);

    // 查询项目
    List<ProjectDO> findAllByName(String name);

    // 删除具体项目 无需@Query注解
    void deleteProjectDOByNameAndVersion(String name, String version);

    // 删除项目
    void deleteAllByName(String name);

    // 查询所有项目名称
    // 没有实现去重，且返回值为键值对格式的String
    //TODO: 需要支持分页操作
    @Query(value = "{'name': {$exists: true}}", fields = "{'name': 1, '_id': 0}")
    List<String> findAllDistinctProjectName();
}
