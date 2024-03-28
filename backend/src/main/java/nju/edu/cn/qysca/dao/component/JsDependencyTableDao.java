package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.JsDependencyTableDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JsDependencyTableDao extends JpaRepository<JsDependencyTableDO, String> {

    /**
     * 根据名称和版本号删除
     * @param name 名称
     * @param version 版本号
     */
    void deleteAllByNameAndVersion(String name, String version);
}
