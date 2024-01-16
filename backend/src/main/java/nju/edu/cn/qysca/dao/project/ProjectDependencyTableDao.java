package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.ProjectDependencyTableDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProjectDependencyTableDao extends MongoRepository<ProjectDependencyTableDO, String> {
    /**
     * 分页查找项目依赖平铺信息
     *
     * @param projectName    项目名
     * @param projectVersion 项目版本号
     * @return Page<ProjectDependencyTableDO> 项目依赖平铺信息分页
     */
    Page<ProjectDependencyTableDO> findByProjectNameAndProjectVersion(String projectName, String projectVersion, Pageable pageable);
}
