package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.RolePermissionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePermissionDao extends JpaRepository<RolePermissionDO,String> {
    @Query("select rid from RolePermissionDO where pid = ?1")
    List<String> findRidsByPid(String pid);
}
