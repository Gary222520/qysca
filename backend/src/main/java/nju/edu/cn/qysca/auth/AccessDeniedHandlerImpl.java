package nju.edu.cn.qysca.auth;

import com.alibaba.fastjson.JSON;
import io.lettuce.core.dynamic.annotation.CommandNaming;
import nju.edu.cn.qysca.controller.ResponseMsg;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
public class AccessDeniedHandlerImpl implements AccessDeniedHandler {
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        ResponseMsg responseMsg=new ResponseMsg<>(403,"权限不足");
        String json= JSON.toJSONString(responseMsg);
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out=response.getWriter();
        out.print(json);
        out.flush();
        out.close();
    }
}
