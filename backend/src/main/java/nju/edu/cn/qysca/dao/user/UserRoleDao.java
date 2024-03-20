package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleDao extends JpaRepository<UserRoleDO, String> {

    @Query("select bid from UserRoleDO where uid = ?1")
    List<String> findBidsByUid(String uid);

    @Query("select rid from UserRoleDO where uid = ?1")
    List<String> findRidsByUid(String uid);

    List<UserRoleDO> findByUid(String uid);

    void deleteAllByAid(String aid);


    void deleteByUidAndRidAndBidAndAid(String uid, String rid, String bid, String aid);
}
