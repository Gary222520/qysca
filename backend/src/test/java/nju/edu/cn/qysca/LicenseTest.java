package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseConflictInfoDTO;
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
        List<String> licenseNameList = licenseService.searchLicense(name);

        assert licenseNameList.size() == 1;
        String licenseName = licenseNameList.get(0);

        assert licenseName.equals("BSD-3-Clause");
    }

    @Test
    public void test2(){

        String name = "CC-BY-NC-SA";
        List<String> licenseNameList = licenseService.searchLicense(name);
        assert !licenseNameList.isEmpty();
        System.out.println(licenseNameList);
    }

    @Test
    public void test3(){

        String name = "apache license";
        List<String> licenseNameList = licenseService.searchLicense(name);
        assert !licenseNameList.isEmpty();
        System.out.println(licenseNameList);
    }

    @Test
    public void test4(){
        String appName = "bu:java-app";
        String appVersion = "1.0.0";
        LicenseConflictInfoDTO licenseConflictInfoDTO = licenseService.getLicenseConflictInformation(appName, appVersion);
        System.out.println("ok");
    }
}
