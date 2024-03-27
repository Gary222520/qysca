package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.user.PermissionDao;
import nju.edu.cn.qysca.dao.user.RoleDao;
import nju.edu.cn.qysca.dao.user.RolePermissionDao;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;
import nju.edu.cn.qysca.domain.component.dos.GoDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.GoDependencyTreeDO;
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
        // special case for spiderComponentInfo
        String name="golang.org/x/mobile";
        String version="v0.0.0-20210901025245-1fde1d6c3ca1";
        GoComponentDO goComponentDO=goService.spiderComponentInfo(name,version);
        System.out.println("ok");
    }

    @Test
    public void test2(){
        // componentAnalysis
        String name="nju.edu.cn/qysca";
        String version="v1.4.8";
        GoComponentDO goComponentDO=goService.componentAnalysis(name,version,"opensource");
        System.out.println("ok");
    }

    @Test
    public void test3(){
        // dependencyTreeAnalysis (zip)
        String name="hola";
        String version="v1.2.0";
        String type="business";
        String builder="zip";
        String filePath="D:/gos/hello.zip";
        GoDependencyTreeDO goDependencyTreeDO=goService.dependencyTreeAnalysis(name,version,type,filePath,builder);
        System.out.println("ok");
    }

    @Test
    public void test4(){
        // dependencyTreeAnalysis (go.mod)
        // dependencyTableAnalysis
        String name="ola";
        String version="v1.0.0";
        String type="close";
        String builder="go.mod";
        String filePath="D:/gos/hello/go.mod";
        GoDependencyTreeDO goDependencyTreeDO=goService.dependencyTreeAnalysis(name,version,type,filePath,builder);
        List<GoDependencyTableDO> goDependencyTableDO=goService.dependencyTableAnalysis(goDependencyTreeDO);
        System.out.println("ok");
    }

    @Test
    public void test5(){
        // spiderDependency
        String name="github.com/gin-gonic/gin";
        String version="v1.4.0";
        GoDependencyTreeDO goDependencyTreeDO=goService.spiderDependency(name,version);
        System.out.println("ok");
    }
}
