package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.user.PermissionDao;
import nju.edu.cn.qysca.dao.user.RoleDao;
import nju.edu.cn.qysca.dao.user.RolePermissionDao;
import nju.edu.cn.qysca.domain.user.dos.PermissionDO;
import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import nju.edu.cn.qysca.domain.user.dos.RolePermissionDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

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
    public void generate_plt_permission(){
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
        auths.add("/qysca/user/listAllUser");

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
    public void generate_plt_role_permisssion(){
        String r0="Admin",r1="Bu Rep",r2="Bu PO",r3="App Leader",r4="App Member";

        String p1="/qysca/application/findApplicationPage";
        gprp_inter(r1,p1);
        gprp_inter(r2,p1);
        gprp_inter(r3,p1);
        gprp_inter(r4,p1);
        String p2="/qysca/application/searchApplicationName";
        gprp_inter(r1,p2);
        gprp_inter(r2,p2);
        gprp_inter(r3,p2);
        gprp_inter(r4,p2);
        String p3="/qysca/application/findApplication";
        gprp_inter(r1,p3);
        gprp_inter(r2,p3);
        gprp_inter(r3,p3);
        gprp_inter(r4,p3);
        String p4="/qysca/application/findSubApplication";
        gprp_inter(r1,p4);
        gprp_inter(r2,p4);
        gprp_inter(r3,p4);
        gprp_inter(r4,p4);
        String p5="/qysca/application/saveApplication";
        gprp_inter(r1,p5);
        gprp_inter(r2,p5);
        String p6="/qysca/application/saveApplicationComponent";
        gprp_inter(r1,p6);
        gprp_inter(r2,p6);
        gprp_inter(r3,p6);
        gprp_inter(r4,p6);
        String p7="/qysca/application/deleteApplicationComponent";
        gprp_inter(r1,p7);
        gprp_inter(r2,p7);
        gprp_inter(r3,p7);
        gprp_inter(r4,p7);
        String p8="/qysca/application/saveApplicationDependency";
        gprp_inter(r1,p8);
        gprp_inter(r2,p8);
        String p9="/qysca/application/upgradeApplication";
        gprp_inter(r1,p9);
        gprp_inter(r2,p9);
        String p10="/qysca/application/deleteApplicationVersion";
        gprp_inter(r1,p10);
        String p11="/qysca/application/getVersionsList";
        gprp_inter(r1,p11);
        gprp_inter(r2,p11);
        gprp_inter(r3,p11);
        gprp_inter(r4,p11);
        String p12="/qysca/application/findApplicationVersionInfo";
        gprp_inter(r1,p12);
        gprp_inter(r2,p12);
        gprp_inter(r3,p12);
        gprp_inter(r4,p12);
        String p13="/qysca/application/findApplicationDependencyTree";
        gprp_inter(r1,p13);
        gprp_inter(r2,p13);
        gprp_inter(r3,p13);
        gprp_inter(r4,p13);
        String p14="/qysca/application/findApplicationDependencyTable";
        gprp_inter(r1,p14);
        gprp_inter(r2,p14);
        gprp_inter(r3,p14);
        gprp_inter(r4,p14);
        String p15="/qysca/application/exportTableExcelBrief";
        gprp_inter(r1,p15);
        gprp_inter(r2,p15);
        gprp_inter(r3,p15);
        gprp_inter(r4,p15);
        String p16="/qysca/application/exportTableExcelDetail";
        gprp_inter(r1,p16);
        gprp_inter(r2,p16);
        gprp_inter(r3,p16);
        gprp_inter(r4,p16);
        String p17="/qysca/application/getApplicationVersionCompareTree";
        gprp_inter(r1,p17);
        gprp_inter(r2,p17);
        gprp_inter(r3,p17);
        gprp_inter(r4,p17);
        String p18="/qysca/application/changeLockState";
        gprp_inter(r1,p18);
        String p19="/qysca/application/changeReleaseState";
        gprp_inter(r2,p19);
        gprp_inter(r3,p19);
        gprp_inter(r4,p19);

        String p20="/qysca/bu/addBu";
        gprp_inter(r0,p20);
        String p21="/qysca/bu/deleteBu";
        gprp_inter(r0,p21);
        String p22="/qysca/bu/listAllBu";
        gprp_inter(r0,p22);

        String p23="/qysca/component/findComponentsPage";
        gprp_inter(r1,p23);
        gprp_inter(r2,p23);
        gprp_inter(r3,p23);
        gprp_inter(r4,p23);
        String p24="/qysca/component/searchComponentName";
        gprp_inter(r1,p24);
        gprp_inter(r2,p24);
        gprp_inter(r3,p24);
        gprp_inter(r4,p24);
        String p25="/qysca/component/saveCloseComponent";
        gprp_inter(r1,p25);
        gprp_inter(r2,p25);
        String p26="/qysca/component/updateCloseComponent";
        gprp_inter(r1,p26);
        gprp_inter(r2,p26);
        String p27="/qysca/component/deleteCloseComponent";
        gprp_inter(r1,p27);
        gprp_inter(r2,p27);
        String p28="/qysca/component/findComponentDependencyTree";
        gprp_inter(r1,p28);
        gprp_inter(r2,p28);
        gprp_inter(r3,p28);
        gprp_inter(r4,p28);
        String p29="/qysca/component/findComponentDependencyTable";
        gprp_inter(r1,p29);
        gprp_inter(r2,p29);
        gprp_inter(r3,p29);
        gprp_inter(r4,p29);
        String p30="/qysca/component/findComponentDetail";
        gprp_inter(r1,p30);
        gprp_inter(r2,p30);
        gprp_inter(r3,p30);
        gprp_inter(r4,p30);

        String p31="/qysca/file/chunk";
        gprp_inter(r1,p31);
        gprp_inter(r2,p31);
        String p32="/qysca/file/merge";
        gprp_inter(r1,p32);
        gprp_inter(r2,p32);

        String p33="/qysca/user/login";
        String p34="/qysca/user/logout";
        String p35="/qysca/user/getUserInfo";
        String p36="/qysca/user/register";
        gprp_inter(r0,p36);
        String p37="/qysca/user/deleteUser";
        gprp_inter(r0,p37);
        String p38="/qysca/user/updateUser";
        gprp_inter(r0,p38);
        String p900="/qysca/user/listAllUser";
        gprp_inter(r0,p900);
        gprp_inter(r1,p900);
        gprp_inter(r2,p900);
        gprp_inter(r3,p900);
        gprp_inter(r4,p900);

        String p39="/qysca/role/addAppMember";
        gprp_inter(r0,p39);
        gprp_inter(r1,p39);
        gprp_inter(r2,p39);
        gprp_inter(r3,p39);
        String p40="/qysca/role/deleteAppMember";
        gprp_inter(r0,p40);
        gprp_inter(r1,p40);
        String p41="/qysca/role/addBuRep";
        gprp_inter(r0,p41);
        gprp_inter(r2,p41);
        String p42="/qysca/role/deleteBuRep";
        gprp_inter(r0,p42);
        gprp_inter(r2,p42);
        String p43="/qysca/role/addBuMember";
        gprp_inter(r0,p43);
        gprp_inter(r1,p43);
        String p44="/qysca/role/listBuMember";
        gprp_inter(r0,p44);
        gprp_inter(r1,p44);
        gprp_inter(r2,p44);
        gprp_inter(r3,p44);
        gprp_inter(r4,p44);
        String p45="/qysca/role/deleteBuMember";
        gprp_inter(r0,p45);
        gprp_inter(r1,p45);
    }

    private void gprp_inter(String role, String permission){
        RoleDO roleDO=roleDao.findByName(role);
        PermissionDO permissionDO=permissionDao.findByUrl(permission);
        RolePermissionDO rolePermissionDO=new RolePermissionDO();
        rolePermissionDO.setRid(roleDO.getId());
        rolePermissionDO.setPid(permissionDO.getId());
        rolePermissionDao.save(rolePermissionDO);
    }
}
