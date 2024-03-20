package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDao extends JpaRepository<RoleDO, String> {

    @Query("select name from RoleDO where id=?1")
    String findNameById(String id);

    RoleDO findByName(String name);
}
