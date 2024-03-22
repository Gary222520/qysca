package nju.edu.cn.qysca.service.user;

import nju.edu.cn.qysca.dao.user.UserDao;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.LoginUserDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailServiceImpl implements UserDetailsService {
    @Autowired
    private UserDao userDao;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDO userDO=userDao.findByUid(username);        // 此处username实指uid
        if(null==userDO){
            throw new PlatformException(500, "用户名或密码错误");
        }
        return new LoginUserDTO(userDO);
    }
}
