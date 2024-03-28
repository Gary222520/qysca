package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JsDependencyTreeDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsDependencyTreeDao extends JpaRepository<JsDependencyTreeDO, String> {

    /**
     * 根据名称和版本号查询
     * @param name 名称
     * @param version 版本号
     * @return JsDependencyTreeDO Js组件依赖树信息
     */
    JsDependencyTreeDO findByNameAndVersion(String name, String version);
}
