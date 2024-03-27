package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import nju.edu.cn.qysca.service.npm.NpmServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NpmTest {

    @Autowired
    private NpmServiceImpl npmService;

    @Test
    public void test() {
        npmService.parsePackageLock("src/main/resources/static/temp/package-lock.json");
    }

    @Test
    public void test1() {
        JsComponentDO componentDO = npmService.componentAnalysis("src/main/resources/static/temp/package.json", "opensource");
        System.out.println(componentDO);
    }
}
