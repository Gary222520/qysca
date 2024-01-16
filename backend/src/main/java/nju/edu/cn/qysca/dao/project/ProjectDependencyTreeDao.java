package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.ProjectDependencyTreeDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectDependencyTreeDao extends MongoRepository<ProjectDependencyTreeDO, String> {
    /**
     * 查找项目依赖树信息
     *
     * @param name    项目名
     * @param version 版本号
     * @return ProjectDependencyTreeDO 项目依赖树信息
     */
    ProjectDependencyTreeDO findByNameAndVersion(String name, String version);
}
