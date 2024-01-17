package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDependencyTableDO;
import nju.edu.cn.qysca.domain.project.dtos.TableExcelBriefDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProjectDependencyTableDao extends MongoRepository<ProjectDependencyTableDO, String> {
    /**
     * 分页查找项目依赖平铺信息
     *
     * @param projectName    项目名
     * @param projectVersion 项目版本号
     * @return Page<ProjectDependencyTableDO> 项目依赖平铺信息分页
     */
    Page<ProjectDependencyTableDO> findByProjectNameAndProjectVersion(String projectName, String projectVersion, Pageable pageable);

    /**
     * 分页查找项目依赖平铺信息
     *
     * @param name    项目名
     * @param version 项目版本号
     * @return Page<ComponentTableDTO> 依赖平铺信息分页
     */
    @Query(value = "{projectName:?0,projectVersion:?1}",
            fields = "{_id:0,projectName:0,projectVersion:0}")
    Page<ComponentTableDTO> findByNV(String name, String version, Pageable pageable);

    /**
     * 查找项目依赖平铺信息列表
     *
     * @param projectName    项目名
     * @param projectVersion 项目版本号
     * @return List<TableExcelBriefDTO> 项目依赖平铺信息
     */
    @Query(value = "{projectName:?0,projectVersion:?1}",
            fields = "{'_id':0,'projectName':0,'projectVersion':0}",
            sort = "{depth:1,name:1,groupId:1,artifactId:1,version:-1}")
    List<TableExcelBriefDTO> findTableListByProject(String projectName, String projectVersion);


    void deleteAllByNameAndVersion(String name, String version);

    void deleteAllByName(String name);

}
