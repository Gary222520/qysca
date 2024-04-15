package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JsDependencyTreeDO;
import nju.edu.cn.qysca.service.npm.NpmServiceImpl;
import nju.edu.cn.qysca.service.spider.JsSpiderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class NpmTest {

    @Autowired
    private NpmServiceImpl npmService;
    @Autowired
    private JsSpiderService jsSpiderService;

    @Test
    public void test() {
        npmService.parsePackageLock("src/main/resources/static/temp/package-lock.json");
    }

    @Test
    public void test1() {
        JsComponentDO componentDO = npmService.componentAnalysis("src/main/resources/static/temp/test.zip", "zip", "opensource");
        System.out.println(componentDO);
    }
    @Test
    public void test2() {
        JsDependencyTreeDO dependencyTreeDO = npmService.dependencyTreeAnalysis("src/main/resources/static/temp/package.json", "package.json", "opensource");
        System.out.println(dependencyTreeDO);
    }

    @Test
    public void test3(){
        jsSpiderService.crawlByNV("colors", "0.6.2");
    }

    @Test
    public void test4(){
        npmService.spiderDependency("pinkie", "2.0.4");
    }

    @Test
    public void test5() {
        JsComponentDO jsComponentDO = jsSpiderService.crawlByNV("typescriptdeclarationfilepublication", "1.0.0");
    }
}
