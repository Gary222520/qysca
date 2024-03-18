package nju.edu.cn.qysca.service.user;

import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;

public interface UserService {
    /**
     * 用户登录
     *
     * @param userDTO 用户登录
     * @return token map
     */
    String login(UserDTO userDTO);

    /**
     * 用户登出
     */
    void logout();

    /**
     * 用户注册
     * @param userDO 用户信息
     */
    void register(UserDO userDO);

    /**
     * 获取用户信息
     * @return 用户信息
     */
    UserDO getUserInfo();


}
