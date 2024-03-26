package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.user.PermissionDao;
import nju.edu.cn.qysca.dao.user.RoleDao;
import nju.edu.cn.qysca.dao.user.RolePermissionDao;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;
import nju.edu.cn.qysca.domain.user.dos.PermissionDO;
import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import nju.edu.cn.qysca.domain.user.dos.RolePermissionDO;
import nju.edu.cn.qysca.service.go.GoService;
import nju.edu.cn.qysca.service.go.GoServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class GoTest {
    @Autowired
    private GoService goService;


    @Test
    public void test1(){
        String name="golang.org/x/mobile";
        String version="v0.0.0-20210901025245-1fde1d6c3ca1";
        GoComponentDO goComponentDO=goService.spiderComponentInfo(name,version);
        System.out.println("ok");
    }
}
