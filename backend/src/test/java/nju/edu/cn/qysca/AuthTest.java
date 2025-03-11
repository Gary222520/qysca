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
        auths.add("/qysca/application/exportSBOM");
        auths.add("/qysca/application/exportHtml");

        auths.add("/qysca/bu/addBu");
        auths.add("/qysca/bu/deleteBu");
        auths.add("/qysca/bu/listAllBu");

        auths.add("/qysca/component/findComponentsPage");
        auths.add("/qysca/component/searchComponentName");
        auths.add("/qysca/component/saveCloseComponent");
        auths.add("/qysca/component/updateCloseComponent");
        auths.add("/qysca/component/deleteCloseComponent");
        auths.add("/qysca/component/findComponentDependencyTree");
        auths.add("/qysca/component/findComponentDependencyTable");
        auths.add("/qysca/component/findComponentDetail");
        auths.add("/qysca/component/changeComponentDetail");

        auths.add("/qysca/file/chunk");
        auths.add("/qysca/file/merge");

        auths.add("/qysca/license/getAppLicense");
        auths.add("/qysca/license/getComponentLicense");
        auths.add("/qysca/license/addAppLicense");
        auths.add("/qysca/license/deleteAppLicense");
        auths.add("/qysca/license/getLicenseInfo");
        auths.add("/qysca/license/getLicensePage");
        auths.add("/qysca/license/getAppLicenseConflict");
        auths.add("/qysca/license/getComponentLicenseConflict");

        auths.add("/qysca/statistics/getApplicationCount");
        auths.add("/qysca/statistics/getComponentCount");
        auths.add("/qysca/statistics/getVulnerabilityStatistics");
        auths.add("/qysca/statistics/getLicenseStatistics");

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

        auths.add("/qysca/vulnerability/getComponentVulnerabilityList");
        auths.add("/qysca/vulnerability/getApplicationVulnerabilityList");
        auths.add("/qysca/vulnerability/addAppVulnerability");
        auths.add("/qysca/vulnerability/deleteAppVulnerability");
        auths.add("/qysca/vulnerability/getVulnerabilityById");
        auths.add("/qysca/vulnerability/getCweById");
        auths.add("/qysca/vulnerability/getVulnerabilityPage");

        for(String auth : auths){
            PermissionDO permissionDO=new PermissionDO();
            permissionDO.setUrl(auth);
            permissionDao.save(permissionDO);
        }
    }

    @Test
    public void generate_plt_role_permisssion(){
        String r0="Admin",r1="Bu Rep",r2="Bu PO",r3="App Leader",r4="App Member";
        String uri="";
        uri="/qysca/application/findApplicationPage";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/searchApplicationName";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/findApplication";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/findSubApplication";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/saveApplication";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/application/saveApplicationComponent";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/deleteApplicationComponent";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/saveApplicationDependency";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/application/upgradeApplication";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/application/deleteApplicationVersion";
        gprp_inter(r1,uri);
        uri="/qysca/application/getVersionsList";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/findApplicationVersionInfo";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/findApplicationDependencyTree";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/findApplicationDependencyTable";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/exportTableExcelBrief";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/exportTableExcelDetail";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/getApplicationVersionCompareTree";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/changeLockState";
        gprp_inter(r1,uri);
        uri="/qysca/application/changeReleaseState";
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/exportSBOM";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/application/exportHtml";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);

        uri="/qysca/bu/addBu";
        gprp_inter(r0,uri);
        uri="/qysca/bu/deleteBu";
        gprp_inter(r0,uri);
        uri="/qysca/bu/listAllBu";
        gprp_inter(r0,uri);

        uri="/qysca/component/findComponentsPage";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/component/searchComponentName";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/component/saveCloseComponent";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/component/updateCloseComponent";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/component/deleteCloseComponent";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/component/findComponentDependencyTree";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/component/findComponentDependencyTable";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/component/findComponentDetail";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/component/changeComponentDetail";
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);

        uri="/qysca/file/chunk";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/file/merge";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);

        uri="/qysca/license/getAppLicense";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/license/getComponentLicense";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/license/addAppLicense";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/license/deleteAppLicense";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/license/getLicenseInfo";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/license/getLicensePage";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/license/getAppLicenseConflict";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/license/getComponentLicenseConflict";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);

        uri="/qysca/statistics/getApplicationCount";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/statistics/getComponentCount";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/statistics/getVulnerabilityStatistics";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/statistics/getLicenseStatistics";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);


        uri="/qysca/user/login";
        uri="/qysca/user/logout";
        uri="/qysca/user/getUserInfo";
        uri="/qysca/user/register";
        gprp_inter(r0,uri);
        uri="/qysca/user/deleteUser";
        gprp_inter(r0,uri);
        uri="/qysca/user/updateUser";
        gprp_inter(r0,uri);
        uri="/qysca/user/listAllUser";
        gprp_inter(r0,uri);
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);

        uri="/qysca/role/addAppMember";
        gprp_inter(r0,uri);
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        uri="/qysca/role/deleteAppMember";
        gprp_inter(r0,uri);
        gprp_inter(r1,uri);
        uri="/qysca/role/addBuRep";
        gprp_inter(r0,uri);
        gprp_inter(r2,uri);
        uri="/qysca/role/deleteBuRep";
        gprp_inter(r0,uri);
        gprp_inter(r2,uri);
        uri="/qysca/role/addBuMember";
        gprp_inter(r0,uri);
        gprp_inter(r1,uri);
        uri="/qysca/role/listBuMember";
        gprp_inter(r0,uri);
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/role/deleteBuMember";
        gprp_inter(r0,uri);
        gprp_inter(r1,uri);


        uri="/qysca/vulnerability/getComponentVulnerabilityList";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/vulnerability/getApplicationVulnerabilityList";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/vulnerability/addAppVulnerability";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/vulnerability/deleteAppVulnerability";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        uri="/qysca/vulnerability/getVulnerabilityById";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/vulnerability/getCweById";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
        uri="/qysca/vulnerability/getVulnerabilityPage";
        gprp_inter(r1,uri);
        gprp_inter(r2,uri);
        gprp_inter(r3,uri);
        gprp_inter(r4,uri);
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
