package nju.edu.cn.qysca.service.user;

import com.auth0.jwt.interfaces.Claim;
import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.auth.JwtUtil;
import nju.edu.cn.qysca.dao.user.UserDao;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.LoginUserDTO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     * @param userDTO 用户信息
     * @return token
     */
    @Override
    @Transactional
    public String login(UserDTO userDTO) {
        // authenticationManager负责验证用户信息
        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userDTO.getUid(), userDTO.getPassword());
        Authentication authentication=authenticationManager.authenticate(authenticationToken);
        if(null == authentication){
            throw new PlatformException(403, "用户名或密码错误");
        }
        userDao.updateLogin(userDTO.getUid(),true); // 更新用户状态为已登录
        LoginUserDTO loginUserDTO=(LoginUserDTO) authentication.getPrincipal();
        String token = JwtUtil.createJWT(loginUserDTO.getUserDO().getUid());
        return token;
    }

    /**
     * 用户登出
     */
    @Override
    @Transactional
    public void logout() {
        Authentication authentication=SecurityContextHolder.getContext().getAuthentication();
        LoginUserDTO loginUserDTO=(LoginUserDTO) authentication.getPrincipal();
        String uid=loginUserDTO.getUserDO().getUid();
        userDao.updateLogin(uid,false); // 更新用户状态为未登录
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
        user.setLogin(false);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userDao.save(userDO);
    }

    /**
     * 获取用户信息
     * @return 用户信息
     */
    @Override
    public UserDO getUserInfo(){
        UserDO ans=ContextUtil.getUserDO();
        if(null==ans){
            throw new PlatformException(500,"用户信息获取失败");
        }
        return ans;
    }

}
