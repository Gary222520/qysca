package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.components.JavaComponentDao;
import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JavaComponentDOTest {
    @Autowired
    private JavaComponentDao javaComponentDao;

    /**
     * 测试写入结点
     */
    @Test
    public void test1() {
        JavaComponentDO javaComponentDO= new JavaComponentDO();
        javaComponentDO.setGroupId("fw.org.ss");
        javaComponentDO.setArtifactId("redod-mm");
        javaComponentDO.setVersion("2.4.0");
        javaComponentDO.getLicenseNames().add("BSD License 3");
        javaComponentDO.getLicenseNames().add("The Apache Software License, Version 2.0");
        javaComponentDO.getLicenseUrls().add("http://opensource.org/licenses/BSD-3-Clause");
        javaComponentDO.getLicenseUrls().add("https://www.apache.org/licenses/LICENSE-2.0.txt");
        javaComponentDO.setName("REDOD MM");
        javaComponentDO.setAuthor("Hong Liu");
        javaComponentDO.setDescription("Try My Best");
        javaComponentDO.setUrl("http://mybest.com");
        javaComponentDao.save(javaComponentDO);
    }

    /**
     * 测试写入结点和关系
     */
    @Test
    public void test2() {
        JavaComponentDO javaComponentDO1= new JavaComponentDO();
        JavaComponentDO javaComponentDO2=new JavaComponentDO();
        JavaComponentDO javaComponentDO3=new JavaComponentDO();

        javaComponentDO1.setGroupId("org.hamcrest");
        javaComponentDO1.setArtifactId("hamcrest-core");
        javaComponentDO1.setVersion("2.2");
        javaComponentDO1.getLicenseNames().add("BSD License 3");
        javaComponentDO1.getLicenseNames().add("The Apache Software License, Version 2.0");
        javaComponentDO1.getLicenseUrls().add("http://opensource.org/licenses/BSD-3-Clause");
        javaComponentDO1.getLicenseUrls().add("https://www.apache.org/licenses/LICENSE-2.0.txt");
        javaComponentDO1.setName("Hamcrest Core");
        javaComponentDO1.setAuthor("Joe Walnes,Nat Pryce,Steve Freeman");
        javaComponentDO1.setDescription("Core Hamcrest API - deprecated, please use \"hamcrest\" instead");
        javaComponentDO1.setUrl("http://hamcrest.org/JavaHamcrest/");

        javaComponentDO2.setGroupId("org.example.nju");
        javaComponentDO2.setArtifactId("jackson");
        javaComponentDO2.setVersion("1.5.4");
        javaComponentDO2.getLicenseNames().add("BSD License 3");
        javaComponentDO2.getLicenseUrls().add("http://opensource.org/licenses/BSD-3-Clause");
        javaComponentDO2.setName("My Jackson");
        javaComponentDO2.setAuthor("Jia Le");
        javaComponentDO2.setDescription("a test component");
        javaComponentDO2.setUrl("http://www.example.com/");

        javaComponentDO3.setGroupId("bseoi.cn");
        javaComponentDO3.setArtifactId("dom-console");
        javaComponentDO3.setVersion("v24.24.2");
        javaComponentDO3.getLicenseNames().add("The Apache Software License, Version 2.0");
        javaComponentDO3.getLicenseUrls().add("https://www.apache.org/licenses/LICENSE-2.0.txt");
        javaComponentDO3.setName("Console of Dom");
        javaComponentDO3.setAuthor("Tian Yao");
        javaComponentDO3.setDescription("a test component");
        javaComponentDO3.setUrl("http://www.example.com/");

        javaComponentDO1.getDependencies().add(javaComponentDO2);
        javaComponentDO1.getDependencies().add(javaComponentDO3);
        javaComponentDO1.getParents().add(javaComponentDO3);
        javaComponentDao.save(javaComponentDO1);
    }


    /**
     * 测试原生查询语句
     */
    @Test
    public void test3(){
        JavaComponentDO res=javaComponentDao.myMethod("org.example.nju","jackson","1.5.4");
        System.out.println(res.getId());
    }

    /**
     * 测试删图
     */
    @Test
    public void test4(){
        javaComponentDao.deleteGraph();
    }
}
