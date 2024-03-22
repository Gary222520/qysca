package nju.edu.cn.qysca.auth;

import nju.edu.cn.qysca.dao.user.UserDao;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.LoginUserDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserDao userDao;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token=request.getHeader("token");
        if(!StringUtils.hasText(token)){
            // 无token
            filterChain.doFilter(request,response);
            return;
        }
        String uid;
        try{
            // 解析token
            uid=JwtUtil.parseJwt(token);
        }catch (Exception e){
            throw new PlatformException(403,"token无效");
        }
        // 获取用户信息（此处也可使用redis加速）
        UserDO userDO=userDao.findByUid(uid);
        if(null == userDO || !userDO.getLogin()){
            throw new PlatformException(403,"token无效");
        }
        LoginUserDTO loginUserDTO=new LoginUserDTO(userDO);
        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(loginUserDTO,null,null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        filterChain.doFilter(request,response);
    }
}
