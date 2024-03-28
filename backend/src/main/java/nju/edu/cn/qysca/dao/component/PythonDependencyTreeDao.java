package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTreeDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PythonDependencyTreeDao extends JpaRepository<PythonDependencyTreeDO, String> {

    /**
     * 根据名称和版本查询
     * @param name 名称
     * @param version 版本
     * @return PythonDependencyTreeDO python依赖树信息
     */
    PythonDependencyTreeDO findByNameAndVersion(String name, String version);
}
