package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.ProjectVersionDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectVersionDao extends MongoRepository<ProjectVersionDO,String> {
    /**
     * 分页查询指定项目的版本信息
     *
     * @param name 项目名称
     * @param pageable 分页信息
     * @return Page<ProjectVersionDO> 分页查询结果
     */
    Page<ProjectVersionDO> findAllByName(String name,Pageable pageable);

    ProjectVersionDO findByNameAndVersion(String name, String version);

    void deleteAllByName(String name);

    void deleteByNameAndVersion(String name, String version);
}
