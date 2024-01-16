package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.ProjectDependencyTreeDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectDependencyTreeDao extends MongoRepository<ProjectDependencyTreeDO, String> {
}
