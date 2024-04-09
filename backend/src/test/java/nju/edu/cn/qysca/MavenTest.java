package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.service.spider.JavaSpiderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class MavenTest {

    @Autowired
    private MavenService mavenService;

    @Autowired
    private JavaSpiderService javaSpiderService;

    @Test
    public void test1(){
        javaSpiderService.crawlByGav("junit","junit","4.12");
    }

    @Test
    public void test2(){
        mavenService.spiderDependency("junit","junit","4.12");
    }

    @Test
    public void test3(){
        JavaDependencyTreeDO javaDependencyTreeDO = mavenService.dependencyTreeAnalysis("src/main/resources/upload/pom.xml","maven","opensource");
        mavenService.dependencyTableAnalysis(javaDependencyTreeDO);

    }

}
