package nju.edu.cn.qysca.dao.application;

import nju.edu.cn.qysca.domain.application.dos.AppDependencyTreeDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppDependencyTreeDao extends JpaRepository<AppDependencyTreeDO, String> {


    /**
     * 根据名称和版本号查询应用组件依赖
     * @param name 名称
     * @param version 版本号
     * @return AppDependencyTreeDO 应用组件依赖
     */
    AppDependencyTreeDO findByNameAndVersion(String name, String version);

    /**
     * 根据名称和版本号删除应用组件依赖
     * @param name 名称
     * @param version 版本号
     */

    void deleteByNameAndVersion(String name, String version);
}
