package nju.edu.cn.qysca.dao.application;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationDao extends JpaRepository<ApplicationDO, String> {


    ApplicationDO findOneById(String id);

    /**
     * 根据name, version查找应用
     * @param name 名称
     * @param version 版本
     * @return ApplicationDO 应用信息
     */
    ApplicationDO findByNameAndVersion(String name, String version);

    /**
     * 根据name查找应用
     * @param name 名称
     * @param pageable 分页信息
     * @return Page<ApplicationDO> 应用信息列表
     */
    Page<ApplicationDO> findAllByName(String name, Pageable pageable);


    /**
     * 根据name, version删除应用
     * @param name 名称
     * @param version 版本
     */
    void deleteByNameAndVersion(String name, String version);



    /**
     * 查询指定应用的版本信息
     * @param name 应用名称
     * @return List<String> 指定应用的版本信息
     */
    @Query("select version from ApplicationDO where name = :name order by version desc")
    List<String> findVersionsByName(String name);


    /**
     * 分页获取应用
     * @param bid 部门编号
     * @param pageable 分页信息
     * @return Page<ApplicationDO> 应用分页信息
     */
    @Query(value = "select distinct on (a.name) a.* from plt_application a where a.id = any (select aid from plt_bu_app where bid = :bid) order by name desc",
            countQuery = "select count(*) from (select distinct a.name from plt_application a where a.id in (select aid from plt_bu_app where bid in :bids) order by name desc) as unique_combinations",
            nativeQuery = true)
    Page<ApplicationDO> findApplicationPage(String bid, Pageable pageable);

    /**
     * 模糊查询应用名称
     * @param bid 部门编号
     * @param name 应用名称
     * @return List<String> 模糊查询应用名称
     */
    @Query(value = "select DISTINCT a.name from plt_application a where a.id = any(select aid from plt_bu_app where bid = :bid) and a.name like %:name%", nativeQuery = true)
    List<String> searchApplicationName(String bid, String name);

    /**
     * 根据名称查询应用 并返回应用的最新版本
     * @param name 应用名称
     * @return ApplicationDO 应用信息
     */
    @Query(value = "select * from plt_application where id = any (select aid from plt_bu_app where bid = :bid) and name = :name order by version desc limit 1", nativeQuery = true)
    ApplicationDO findApplication(String bid, String name);

    /**
     * 根据应用Id查询子应用
     * @param applicationId 应用Id
     * @return List<ApplicationDO> 子应用列表
     */
    @Query(value = "select a.* from plt_application a where a.id = ANY (select unnest(child_application) from plt_application where id = :applicationId) order by name desc", nativeQuery = true)
    List<ApplicationDO> findSubApplication(String applicationId);

    /**
     * 根据Id查找应用
     * @param id 应用Id
     */
    ApplicationDO findApplicationDOById(String id);


    /**
     * 根据Id查找其父应用
     * @param id 应用Id
     * @return List<ApplicationDO> 父应用列表
     */
    @Query(value = "select * from plt_application where :id = any(child_application)", nativeQuery = true)
    List<ApplicationDO> findParentApplication(String id);
}
