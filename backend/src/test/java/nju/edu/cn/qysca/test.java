package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.service.bu.BuService;
import nju.edu.cn.qysca.service.user.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class test {

    @Autowired
    private BuService buService;

    @Autowired
    private UserService userService;

    @Test
    public void testBu(){
        buService.createBu("软件开发部");
    }

    @Test
    public void testUser(){
        BuDO buDO = buService.findBuByName("软件开发部");
        UserDO userDO = new UserDO();
        userDO.setBu(buDO);
        userDO.setUid("000000000");
        userDO.setName("test");
        userDO.setPassword("admin");
        userDO.setPhone("17551542358");
        userDO.setEmail("2227596010@qq.com");
        userService.register(userDO);
    }
}
