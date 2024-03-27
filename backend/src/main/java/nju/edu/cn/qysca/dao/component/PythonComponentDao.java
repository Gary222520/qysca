package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PythonComponentDao extends JpaRepository<PythonComponentDO, String> {
    PythonComponentDO findByNameAndVersion(String name, String version);
}
