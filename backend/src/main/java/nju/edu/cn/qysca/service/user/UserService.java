package nju.edu.cn.qysca.service.user;

import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;
import nju.edu.cn.qysca.domain.user.dtos.UserDetailDTO;
import org.springframework.data.domain.Page;

import java.util.List;

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
    UserDetailDTO getUserInfo();

    /**
     * 删除用户
     * @param uid 用户编号
     */
    void deleteUser(String uid);

    /**
     * 更新用户信息
     * @param userDO 用户信息
     */
    void updateUser(UserDO userDO);

    /**
     * 分页获取所有用户信息
     * @param number 页号
     * @param size 页大小
     * @return 用户信息分页结果
     */
    Page<UserDO> listAllUser(int number, int size);
}
