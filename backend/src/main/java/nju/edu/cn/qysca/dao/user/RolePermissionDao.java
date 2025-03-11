package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.RolePermissionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionDao extends JpaRepository<RolePermissionDO,String> {
    /**
     * 根据权限查询角色
     * @param pid 权限Id
     * @return List<String> 角色信息
     */
    @Query("select rid from RolePermissionDO where pid = :pid")
    List<String> findRidsByPid(String pid);
}
