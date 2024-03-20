package nju.edu.cn.qysca.auth;

import com.alibaba.fastjson.JSON;
import nju.edu.cn.qysca.controller.ResponseMsg;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AuthenticationEntryPointImpl implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        ResponseMsg responseMsg=new ResponseMsg<>(403,"登录认证失败");
        String json= JSON.toJSONString(responseMsg);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out=response.getWriter();
        out.print(json);
        out.flush();
        out.close();
    }
}
