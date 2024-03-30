package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.service.license.LicenseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class LicenseTest {

    @Autowired
    private LicenseService licenseService;

    @Test
    public void test1(){
        String name = "BSD-3-Clause";
        List<LicenseDO> licenseDOList = licenseService.searchLicense(name);

        assert licenseDOList.size() == 1;
        LicenseDO licenseDO = licenseDOList.get(0);

        assert licenseDO.getFullName().equals("BSD-3-Clause");
    }

    @Test
    public void test2(){

        String name = "CC-BY-NC-SA";
        List<LicenseDO> licenseDOList = licenseService.searchLicense(name);
        assert !licenseDOList.isEmpty();
        System.out.println();
        for (LicenseDO licenseDO : licenseDOList){
            System.out.println(licenseDO.getName());
        }
    }

    @Test
    public void test3(){

        String name = "apache license";
        List<LicenseDO> licenseDOList = licenseService.searchLicense(name);
        assert !licenseDOList.isEmpty();
        System.out.println();
        for (LicenseDO licenseDO : licenseDOList){
            System.out.println(licenseDO.getName() + "      " + licenseDO.getFullName());
        }
    }
}
