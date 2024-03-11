package nju.edu.cn.qysca.service.user;

import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;

import java.util.List;
import java.util.Map;

public interface UserService {
    /**
     * 用户登录
     *
     * @param userDTO 用户登录
     * @return token map
     */
    String login(UserDTO userDTO);

    /**
     * 用户注册
     * @param userDO 用户信息
     */
    void register(UserDO userDO);

    /**
     * 用户认证
     * @param token 用户身份
     * @return 用户信息
     */
    UserDO auth(String token);

}
