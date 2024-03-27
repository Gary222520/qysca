package nju.edu.cn.qysca;

import nju.edu.cn.qysca.service.gradle.GradleService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class GradleTest {

    @Autowired
    private GradleService gradleService;

    @Test
    public void test1(){
        String filePath = "D:\\MyCourse\\2024Aping\\graduate design\\qysca\\backend\\src\\main\\resources\\upload\\gradle-spring-example.zip";
        String builder = "zip";
        String groupId = "org.example";
        String artifactId = "gradle-spring-example";
        String version = "0.0.1-SNAPSHOT";
        String type = "internal";
        gradleService.dependencyTreeAnalysis(filePath, builder, groupId, artifactId, version, type);
        System.out.println("ok");
    }
}
