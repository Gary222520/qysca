package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.ProjectDependencyTableDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectDependencyTableDao extends MongoRepository<ProjectDependencyTableDO, String> {
}
