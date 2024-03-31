package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.service.report.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ReportTest {

    @Autowired
    private ReportService reportService;

    @Test
    public void test1(){
        ApplicationDO applicationDO = new ApplicationDO();
        applicationDO.setId("00010001000100010001000100010001");
        applicationDO.setName("test-app");
        applicationDO.setVersion("0.0.1");
        applicationDO.setDescription("hello world.");
        applicationDO.setCreator("me");
        applicationDO.setState("SUCCESS");
        applicationDO.setRelease(false);
        applicationDO.setLock(false);
        applicationDO.setBuilder("maven");
        applicationDO.setType("internal");

        JavaComponentDO componentDO1 = new JavaComponentDO();
        componentDO1.setId("00010001000100010001000100020001");
        componentDO1.setName("test-com-1");
        componentDO1.setVersion("0.0.1");
        componentDO1.setLanguage("java");
        componentDO1.setType("opensource");
        componentDO1.setDescription("fake news!");
        componentDO1.setUrl("http");
        componentDO1.setDownloadUrl("http://ssee.com");


        JavaComponentDO componentDO2 = new JavaComponentDO();
        componentDO2.setId("00010001000100010001000100020002");
        componentDO2.setName("test-com-2");
        componentDO2.setVersion("0.0.2");
        componentDO2.setLanguage("java");
        componentDO2.setType("internal");
        componentDO2.setDescription("watch out");
        componentDO2.setUrl("seqqqq");
        componentDO2.setDownloadUrl("http://ssweqqqe.com");

        List<ComponentDO> componentDOList = new ArrayList<>();
        componentDOList.add(componentDO1);
        componentDOList.add(componentDO2);

        reportService.exportHtml(applicationDO, componentDOList);
        System.out.println("ok");

    }
}
