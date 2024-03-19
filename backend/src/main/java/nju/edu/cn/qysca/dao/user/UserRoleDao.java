package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface UserRoleDao extends JpaRepository<UserRoleDO, String> {

    @Query("select bid from UserRoleDO where uid = ?1")
    List<String> findBidsByUid(String uid);


    void deleteAllByAid(String aid);


    void deleteByUidAndRidAndBidAndAid(String uid, String rid, String bid, String aid);
}
