package nju.edu.cn.qysca.auth;

import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.LoginUserDTO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class ContextUtil {

    private ContextUtil(){}

    public static UserDO getUserDO(){
        Authentication authentication= SecurityContextHolder.getContext().getAuthentication();
        LoginUserDTO loginUserDTO=(LoginUserDTO) authentication.getPrincipal();
        return loginUserDTO.getUserDO();
    }
}
