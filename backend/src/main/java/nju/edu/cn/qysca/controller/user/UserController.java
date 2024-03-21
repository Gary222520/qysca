package nju.edu.cn.qysca.controller.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nju.edu.cn.qysca.auth.JwtUtil;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;
import nju.edu.cn.qysca.domain.user.dtos.UserDetailDTO;
import nju.edu.cn.qysca.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return new ResponseMsg<>();
    }

    @ApiOperation("获取用户信息")
    @GetMapping("/getUserInfo")
    public ResponseMsg<UserDetailDTO> getUserInfo() {
        return new ResponseMsg<>(userService.getUserInfo());
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    @PreAuthorize("@my.checkAuth('/qysca/user/register')")
    public ResponseMsg register(@RequestBody UserDO userDO) {
        userService.register(userDO);
        return new ResponseMsg<>();
    }

    @ApiOperation("删除用户")
    @PostMapping("/deleteUser")
    @PreAuthorize("@my.checkAuth('/qysca/user/deleteUser')")
    public ResponseMsg deleteUser(@RequestParam String uid) {
        userService.deleteUser(uid);
        return new ResponseMsg<>();
    }

    @ApiOperation("更新用户信息")
    @PostMapping("/updateUser")
    @PreAuthorize("@my.checkAuth('/qysca/user/updateUser')")
    public ResponseMsg updateUser(@RequestBody UserDO userDO) {
        userService.updateUser(userDO);
        return new ResponseMsg<>();
    }

    @ApiOperation("查看所有用户信息")
    @GetMapping("/listAllUser")
    @PreAuthorize("@my.checkAuth('/qysca/user/listAllUser')")
    public ResponseMsg<Page<UserDO>> listAllUser(@ApiParam(value = "页码", required = true) @RequestParam int number,
                                                 @ApiParam(value = "页大小", required = true) @RequestParam int size) {
        return new ResponseMsg<>(userService.listAllUser(number,size));
    }
}
