package nju.edu.cn.qysca.controller.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.auth.JwtUtil;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;
import nju.edu.cn.qysca.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理")
@RestController
@RequestMapping("qysca/user")
public class UserController {
    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public ResponseMsg<String> login(@RequestBody UserDTO userDTO) {
        return new ResponseMsg<>(userService.login(userDTO));
    }

    @ApiOperation("用户登出")
    @GetMapping("/logout")
    public ResponseMsg logout() {
        userService.logout();
        return new ResponseMsg();
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/getUserInfo")
    public ResponseMsg<UserDO> getUserInfo() {
        return new ResponseMsg<>(userService.getUserInfo());
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public ResponseMsg register(@RequestBody UserDO userDO) {
        userService.register(userDO);
        return new ResponseMsg();
    }
}
