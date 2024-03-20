package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleDao extends JpaRepository<RoleDO, String> {

    /**
     * 根据id查询角色名
     * @param id 角色Id
     * @return 角色名
     */
    @Query("select name from RoleDO where id=?1")
    String findNameById(String id);

    /**
     * 根据名称查找角色
     * @param name 名称
     * @return RoleDO 角色信息
     */
    RoleDO findByName(String name);
}
