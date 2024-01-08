package nju.edu.cn.qysca.exception;

import nju.edu.cn.qysca.controller.ResponseMsg;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = {"nju.edu.cn.qysca.controller"})
public class PlatformExceptionHandler {
    @ExceptionHandler(PlatformException.class)
    @ResponseBody
    private ResponseMsg handlePlatformException(PlatformException e) {
        return new ResponseMsg(e.getCode(), e.getMessage());
    }
}
