package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleDao extends JpaRepository<RoleDO, String> {


    RoleDO findByName(String name);
}
