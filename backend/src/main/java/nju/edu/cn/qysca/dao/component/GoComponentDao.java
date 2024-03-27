package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoComponentDao extends JpaRepository<GoComponentDO,String> {
    /**
     * 根据名称和版本查询Go组件信息
     * @param name 组件名称
     * @param version 组件版本
     * @return Go组件信息
     */
    GoComponentDO findByNameAndVersion(String name,String version);
}
