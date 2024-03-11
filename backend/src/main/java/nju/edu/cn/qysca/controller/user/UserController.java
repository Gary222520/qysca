package nju.edu.cn.qysca.controller.user;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.auth.Authorized;
import nju.edu.cn.qysca.config.JwtConfig;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dtos.UserDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Api(tags = "用户管理")
@RestController
@RequestMapping("qysca/user")
public class UserController {
    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserService userService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public ResponseMsg<String> userLogin(@RequestBody UserDTO userDTO) {
        return new ResponseMsg<>(userService.login(userDTO));
    }

    @ApiOperation("用户鉴权")
    @GetMapping("/auth")
    public ResponseMsg<UserDO> userAuth(@RequestParam String token) {
        return new ResponseMsg<>(userService.auth(token));
    }

    @ApiOperation("用户注册")
    @PostMapping("/register")
    @Authorized(roles= {"App Leader","Bu PO","Admin"})
    public ResponseMsg userRegister(UserDO current,@RequestBody UserDO userDO) {
        // 权限控制
        if(current==null || current.getRole()==null){
            throw new PlatformException(403,"未经授权");
        }
        switch (current.getRole()) {
            case "App Leader":
                if (!userDO.getRole().equals("App Leader") && !userDO.getRole().equals("App Member")) {
                    throw new PlatformException(500, "创建权限不足");
                }
                break;
            case "Bu PO":
                if (!userDO.getRole().equals("App Leader")) {
                    throw new PlatformException(500, "创建权限不足");
                }
                break;
            case "Admin":
                if (!userDO.getRole().equals("Bu Rep") && !userDO.getRole().equals("Bu PO")) {
                    throw new PlatformException(500, "创建权限不足");
                }
                break;
        }
        userService.register(userDO);
        return new ResponseMsg();
    }
}
