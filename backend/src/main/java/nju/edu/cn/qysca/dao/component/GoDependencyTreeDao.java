package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.GoDependencyTreeDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoDependencyTreeDao extends JpaRepository<GoDependencyTreeDO,String> {

    /**
     * 根据名称和版本号查找
     * @param name 名称
     * @param version 版本号
     * @return GoDependencyTreeDO Go组件依赖信息
     */
    GoDependencyTreeDO findByNameAndVersion(String name, String version);

}
