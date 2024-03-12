package nju.edu.cn.qysca.auth;

import nju.edu.cn.qysca.config.JwtConfig;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.user.UserService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Aspect
@Configuration
@Order(1)
public class AuthAspect {

    private final UserService userService;

    private final JwtConfig jwtConfig;

    @Autowired
    public AuthAspect(UserService userService, JwtConfig jwtConfig) {
        this.userService = userService;
        this.jwtConfig = jwtConfig;
    }

    @Before(value = "execution(public * nju.edu.cn.qysca.controller.*.*.*(..)) && @annotation(authorized)")
    public void authCheck(JoinPoint joinPoint, Authorized authorized) {
        try {
            HttpServletRequest httpServletRequest = ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
            String token = Optional.ofNullable(httpServletRequest.getHeader("Authorization")).
                    orElseThrow(() -> new PlatformException(403, "用户未获得第三方登录授权"));
            UserDO user = userService.auth(token);

            // 判断切面方法是否包含当前token对应的角色
            if (!Arrays.stream(authorized.roles()).collect(Collectors.toList()).contains(user.getRole())) {
                throw new PlatformException(403, "访问未授权");
            } else {
                // 将token的对象赋值给切面方法的user参数
                Object[] objects = joinPoint.getArgs();
                for (Object o : objects) {
                    if (o instanceof UserDO) {
                        BeanUtils.copyProperties(user, o);
                        break;
                    }
                }
            }
        }catch (PlatformException e) {
            throw new PlatformException(403, "认证失败");
        }
    }
}
