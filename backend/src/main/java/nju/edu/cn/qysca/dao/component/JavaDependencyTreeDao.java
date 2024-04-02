package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface JavaDependencyTreeDao extends JpaRepository<JavaDependencyTreeDO, String> {

    /**
     * 根据name, version查找闭源组件依赖树
     *
     * @param name 名称
     * @param version    版本号
     * @return JavaDependencyTreeDO 查找结果
     */
    JavaDependencyTreeDO findByNameAndVersion(String name, String version);


    /**
     * 根据gav删除闭源组件依赖树
     * @param name 名称
     * @param version 版本号
     */
    void deleteByNameAndVersion(String name, String version);

}
