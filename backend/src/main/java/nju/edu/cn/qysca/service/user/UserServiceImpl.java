package nju.edu.cn.qysca.service.user;

import com.auth0.jwt.interfaces.Claim;
import nju.edu.cn.qysca.config.JwtConfig;
import nju.edu.cn.qysca.dao.user.UserDao;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.Id;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class UserServiceImpl implements UserService {

    // 用户身份认证管理Service层实现

    private final UserDao userDao;

    private final JwtConfig jwtConfig;


    @Autowired
    public UserServiceImpl(UserDao userDao, JwtConfig jwtConfig) {
        this.userDao = userDao;
        this.jwtConfig = jwtConfig;
    }


    /**
     * 用户登录
     * @param userDTO 用户信息
     * @return token
     */
    @Override
    public String login(UserDTO userDTO) {
        UserDO userDO = userDao.findByUidAndPassword(userDTO.getUid(), userDTO.getPassword());
        if (null == userDO ) {
            throw new PlatformException(500, "用户名或密码错误");
        }
        String token = jwtConfig.createJWT(userDO);
        return token;
    }

    /**
     * 用户注册
     * @param userDO 用户信息
     */
    @Override
    public void register(UserDO userDO) {
        UserDO user=userDao.findByUid(userDO.getUid());
        if (user != null) {
            throw new PlatformException(500, "用户编号已存在");
        }
        userDao.save(userDO);
    }

    /**
     * 用户认证
     * @param token 用户身份
     * @return 用户信息
     */
    @Override
    public UserDO auth(String token) {

        Map<String, Claim> claims = jwtConfig.parseJwt(token);
        UserDO userDO = UserDO.builder()
                .uid(claims.get("uid").as(String.class))
                .name(claims.get("name").as(String.class))
/*                .role(claims.get("role").as(String.class))*/
                .email(claims.get("email").as(String.class))
                .phone(claims.get("phone").as(String.class))
                .build();
        return userDO;
    }

}
