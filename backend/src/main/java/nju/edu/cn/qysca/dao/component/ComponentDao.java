package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComponentDao extends JpaRepository<ComponentDO,String> {
    List<ComponentDO> findAll();
}
