package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.UserDO;
import org.springframework.data.jpa.repository.JpaRepository;
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

}
