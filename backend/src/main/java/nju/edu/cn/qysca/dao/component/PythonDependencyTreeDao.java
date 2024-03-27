package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTreeDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PythonDependencyTreeDao extends JpaRepository<PythonDependencyTreeDO, String> {
}
