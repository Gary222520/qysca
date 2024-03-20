package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.UserDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDao extends JpaRepository<UserDO, String> {
    /**
     * 根据编号和密码查找用户
     * @param uid 用户编号
     * @param password 用户密码
     * @return 用户信息
     */
    UserDO findByUidAndPassword(String uid, String password);

    /**
     * 根据编号查找用户
     * @param uid 用户编号
     * @return 用户信息
     */
    UserDO findByUid(String uid);

    /**
     * 更新用户登录状态
     * @param uid 用户编号
     * @param login 是否登录
     */
    @Modifying
    @Query("update UserDO set login=:login where uid=:uid")
    void updateLogin(String uid,Boolean login);
}
