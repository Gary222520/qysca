package nju.edu.cn.qysca.service.user;

import com.auth0.jwt.interfaces.Claim;
import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.auth.JwtUtil;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.dao.user.RoleDao;
import nju.edu.cn.qysca.dao.user.UserDao;
import nju.edu.cn.qysca.dao.user.UserRoleDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
import nju.edu.cn.qysca.domain.user.dtos.LoginUserDTO;
import nju.edu.cn.qysca.domain.user.dtos.UserBuAppRoleDTO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;
import nju.edu.cn.qysca.domain.user.dtos.UserDetailDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private BuDao buDao;


    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private RoleDao roleDao;

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
    public UserDetailDTO getUserInfo(){
        UserDetailDTO ans=new UserDetailDTO();
        UserDO user=ContextUtil.getUserDO();
        if(null==user){
            throw new PlatformException(500,"用户信息获取失败");
        }
        ans.setUser(user);
        ans.setUserBuAppRoles(new ArrayList<>());
        // 获取用户部门应用角色信息
        List<UserRoleDO> userRoleDOList=userRoleDao.findByUid(user.getUid());
        if(null==userRoleDOList||userRoleDOList.isEmpty()){
            return ans;
        }
        for (UserRoleDO userRoleDO:userRoleDOList){
            UserBuAppRoleDTO userBuAppRoleDTO=new UserBuAppRoleDTO();
            userBuAppRoleDTO.setBuName("-");
            userBuAppRoleDTO.setAppName("-");
            userBuAppRoleDTO.setAppVersion("-");
            userBuAppRoleDTO.setRole("-");
            if(!userRoleDO.getBid().equals("-")){
                BuDO buDO=buDao.findByBid(userRoleDO.getBid());
                userBuAppRoleDTO.setBuName(buDO.getName());
            }
            if(!userRoleDO.getAid().equals("-")){
                ApplicationDO applicationDO=applicationDao.findOneById(userRoleDO.getAid());
                userBuAppRoleDTO.setAppName(applicationDO.getName());
                userBuAppRoleDTO.setAppVersion(applicationDO.getVersion());
            }
            if(!userRoleDO.getRid().equals("-")){
                String name=roleDao.findNameById(userRoleDO.getRid());
                userBuAppRoleDTO.setRole(name);
            }
            ans.getUserBuAppRoles().add(userBuAppRoleDTO);
        }
        return ans;
    }

    /**
     * 删除用户
     * @param uid 用户编号
     */
    @Override
    @Transactional
    public void deleteUser(String uid) {
        userDao.deleteUserDOByUid(uid);
        //删除其在角色表中的所有信息
        userRoleDao.deleteAllByUid(uid);
    }

    /**
     * 更新用户信息
     * @param userDO 用户信息
     */
    @Override
    @Transactional
    public void updateUser(UserDO userDO) {
        UserDO oldUserDO=userDao.findByUid(userDO.getUid());
        BeanUtils.copyProperties(userDO, oldUserDO);
        userDao.save(oldUserDO);
    }
}
