package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.ProjectVersionDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectVersionDao extends MongoRepository<ProjectVersionDO, String> {

    ProjectVersionDO findByNameAndVersion(String name, String version);
}
