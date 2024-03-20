package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.user.PermissionDao;
import nju.edu.cn.qysca.dao.user.RoleDao;
import nju.edu.cn.qysca.dao.user.RolePermissionDao;
import nju.edu.cn.qysca.domain.user.dos.PermissionDO;
import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import nju.edu.cn.qysca.domain.user.dos.RolePermissionDO;
import org.checkerframework.checker.units.qual.A;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.management.relation.Role;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class AuthTest {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RoleDao roleDao;

    @Autowired
    private PermissionDao permissionDao;

    @Autowired
    private RolePermissionDao rolePermissionDao;


    @Test
    public void test1(){
        System.out.println(passwordEncoder.encode("admin"));
    }

    @Test
    public void test2(){
        RoleDO role1=new RoleDO();
        role1.setName("Admin");
        RoleDO role2=new RoleDO();
        role2.setName("Bu Rep");
        RoleDO role3=new RoleDO();
        role3.setName("Bu PO");
        RoleDO role4=new RoleDO();
        role4.setName("App Leader");
        RoleDO role5=new RoleDO();
        role5.setName("App Member");
        roleDao.save(role1);
        roleDao.save(role2);
        roleDao.save(role3);
        roleDao.save(role4);
        roleDao.save(role5);
    }

    @Test
    public void test3(){
        RoleDO role1=new RoleDO();
        role1.setName("Admin");
        RoleDO role2=new RoleDO();
        role2.setName("Bu Rep");
        RoleDO role3=new RoleDO();
        role3.setName("Bu PO");
        RoleDO role4=new RoleDO();
        role4.setName("App Leader");
        RoleDO role5=new RoleDO();
        role5.setName("App Member");
        roleDao.save(role1);
        roleDao.save(role2);
        roleDao.save(role3);
        roleDao.save(role4);
        roleDao.save(role5);
    }

    @Test
    public void test4(){
        List<String> auths=new ArrayList<>();
        auths.add("/qysca/application/findApplicationPage");
        auths.add("/qysca/application/searchApplicationName");
        auths.add("/qysca/application/findApplication");
        auths.add("/qysca/application/findSubApplication");
        auths.add("/qysca/application/saveApplication");
        auths.add("/qysca/application/saveApplicationComponent");
        auths.add("/qysca/application/deleteApplicationComponent");
        auths.add("/qysca/application/saveApplicationDependency");
        auths.add("/qysca/application/upgradeApplication");
        auths.add("/qysca/application/deleteApplicationVersion");
        auths.add("/qysca/application/getVersionsList");
        auths.add("/qysca/application/findApplicationVersionInfo");
        auths.add("/qysca/application/findApplicationDependencyTree");
        auths.add("/qysca/application/findApplicationDependencyTable");
        auths.add("/qysca/application/exportTableExcelBrief");
        auths.add("/qysca/application/exportTableExcelDetail");
        auths.add("/qysca/application/getApplicationVersionCompareTree");
        auths.add("/qysca/application/changeLockState");
        auths.add("/qysca/application/changeReleaseState");

        auths.add("/qysca/component/findComponentsPage");
        auths.add("/qysca/component/searchComponentName");
        auths.add("/qysca/component/saveCloseComponent");
        auths.add("/qysca/component/updateCloseComponent");
        auths.add("/qysca/component/deleteCloseComponent");
        auths.add("/qysca/component/findComponentDependencyTree");
        auths.add("/qysca/component/findComponentDependencyTable");
        auths.add("/qysca/component/findComponentDetail");

        auths.add("/qysca/file/chunk");
        auths.add("/qysca/file/merge");

        auths.add("/qysca/user/login");
        auths.add("/qysca/user/logout");
        auths.add("/qysca/user/getUserInfo");
        auths.add("/qysca/user/register");
        auths.add("/qysca/user/deleteUser");
        auths.add("/qysca/user/updateUser");

        auths.add("/qysca/role/addAppMember");
        auths.add("/qysca/role/deleteAppMember");
        auths.add("/qysca/role/addBuRep");
        auths.add("/qysca/role/deleteBuRep");
        auths.add("/qysca/role/addBuMember");
        auths.add("/qysca/role/listBuMember");
        auths.add("/qysca/role/deleteBuMember");

        auths.add("/qysca/bu/addBu");
        auths.add("/qysca/bu/deleteBu");
        auths.add("/qysca/bu/listAllBu");

        for(String auth : auths){
            PermissionDO permissionDO=new PermissionDO();
            permissionDO.setUrl(auth);
            permissionDao.save(permissionDO);
        }
    }

    @Test
    public void test5(){
        String r0="Admin",r1="Bu Rep",r2="Bu PO",r3="App Leader",r4="App Member";

        String p1="/qysca/application/findApplicationPage";
        test5inter(r1,p1);test5inter(r2,p1);test5inter(r3,p1);test5inter(r4,p1);
        String p2="/qysca/application/searchApplicationName";
        test5inter(r1,p2);test5inter(r2,p2);test5inter(r3,p2);test5inter(r4,p2);
        String p3="/qysca/application/findApplication";
        test5inter(r1,p3);test5inter(r2,p3);test5inter(r3,p3);test5inter(r4,p3);
        String p4="/qysca/application/findSubApplication";
        test5inter(r1,p4);test5inter(r2,p4);test5inter(r3,p4);test5inter(r4,p4);
        String p5="/qysca/application/saveApplication";
        test5inter(r1,p5);test5inter(r2,p5);
        String p6="/qysca/application/saveApplicationComponent";
        test5inter(r1,p6);test5inter(r2,p6);test5inter(r3,p6);test5inter(r4,p6);
        String p7="/qysca/application/deleteApplicationComponent";
        test5inter(r1,p7);test5inter(r2,p7);test5inter(r3,p7);test5inter(r4,p7);
        String p8="/qysca/application/saveApplicationDependency";
        test5inter(r1,p8);test5inter(r2,p8);
        String p9="/qysca/application/upgradeApplication";
        test5inter(r1,p9);test5inter(r2,p9);
        String p10="/qysca/application/deleteApplicationVersion";
        test5inter(r1,p10);
        String p11="/qysca/application/getVersionsList";
        test5inter(r1,p11);test5inter(r2,p11);test5inter(r3,p11);test5inter(r4,p11);
        String p12="/qysca/application/findApplicationVersionInfo";
        test5inter(r1,p12);test5inter(r2,p12);test5inter(r3,p12);test5inter(r4,p12);
        String p13="/qysca/application/findApplicationDependencyTree";
        test5inter(r1,p13);test5inter(r2,p13);test5inter(r3,p13);test5inter(r4,p13);
        String p14="/qysca/application/findApplicationDependencyTable";
        test5inter(r1,p14);test5inter(r2,p14);test5inter(r3,p14);test5inter(r4,p14);
        String p15="/qysca/application/exportTableExcelBrief";
        test5inter(r1,p15);test5inter(r2,p15);test5inter(r3,p15);test5inter(r4,p15);
        String p16="/qysca/application/exportTableExcelDetail";
        test5inter(r1,p16);test5inter(r2,p16);test5inter(r3,p16);test5inter(r4,p16);
        String p17="/qysca/application/getApplicationVersionCompareTree";
        test5inter(r1,p17);test5inter(r2,p17);test5inter(r3,p17);test5inter(r4,p17);
        String p18="/qysca/application/changeLockState";
        test5inter(r1,p18);
        String p19="/qysca/application/changeReleaseState";
        test5inter(r2,p19);test5inter(r3,p19);test5inter(r4,p19);

        String p20="/qysca/bu/addBu";
        test5inter(r0,p20);
        String p21="/qysca/bu/deleteBu";
        test5inter(r0,p21);
        String p22="/qysca/bu/listAllBu";
        test5inter(r0,p22);

        String p23="/qysca/component/findComponentsPage";
        test5inter(r1,p23);test5inter(r2,p23);test5inter(r3,p23);test5inter(r4,p23);
        String p24="/qysca/component/searchComponentName";
        test5inter(r1,p24);test5inter(r2,p24);test5inter(r3,p24);test5inter(r4,p24);
        String p25="/qysca/component/saveCloseComponent";
        test5inter(r1,p25);test5inter(r2,p25);
        String p26="/qysca/component/updateCloseComponent";
        test5inter(r1,p26);test5inter(r2,p26);
        String p27="/qysca/component/deleteCloseComponent";
        test5inter(r1,p27);test5inter(r2,p27);
        String p28="/qysca/component/findComponentDependencyTree";
        test5inter(r1,p28);test5inter(r2,p28);test5inter(r3,p28);test5inter(r4,p28);
        String p29="/qysca/component/findComponentDependencyTable";
        test5inter(r1,p29);test5inter(r2,p29);test5inter(r3,p29);test5inter(r4,p29);
        String p30="/qysca/component/findComponentDetail";
        test5inter(r1,p30);test5inter(r2,p30);test5inter(r3,p30);test5inter(r4,p30);

        String p31="/qysca/file/chunk";
        test5inter(r1,p31);test5inter(r2,p31);
        String p32="/qysca/file/merge";
        test5inter(r1,p32);test5inter(r2,p32);

        String p33="/qysca/user/login";
        String p34="/qysca/user/logout";
        String p35="/qysca/user/getUserInfo";
        String p36="/qysca/user/register";
        test5inter(r0,p36);
        String p37="/qysca/user/deleteUser";
        test5inter(r0,p37);
        String p38="/qysca/user/updateUser";
        test5inter(r0,p38);

        String p39="/qysca/role/addAppMember";
        test5inter(r0,p39);test5inter(r1,p39);test5inter(r2,p39);test5inter(r3,p39);
        String p40="/qysca/role/deleteAppMember";
        test5inter(r0,p40);test5inter(r1,p40);
        String p41="/qysca/role/addBuRep";
        test5inter(r0,p41);test5inter(r2,p41);
        String p42="/qysca/role/deleteBuRep";
        test5inter(r0,p42);test5inter(r2,p42);
        String p43="/qysca/role/addBuMember";
        test5inter(r0,p43);test5inter(r1,p43);
        String p44="/qysca/role/listBuMember";
        test5inter(r0,p44);test5inter(r1,p44);test5inter(r2,p44);test5inter(r3,p44);test5inter(r4,p44);
        String p45="/qysca/role/deleteBuMember";
        test5inter(r0,p45);test5inter(r1,p45);
    }

    private void test5inter(String role,String permission){
        RoleDO roleDO=roleDao.findByName(role);
        PermissionDO permissionDO=permissionDao.findByUrl(permission);
        RolePermissionDO rolePermissionDO=new RolePermissionDO();
        rolePermissionDO.setRid(roleDO.getId());
        rolePermissionDO.setPid(permissionDO.getId());
        rolePermissionDao.save(rolePermissionDO);
    }
}
