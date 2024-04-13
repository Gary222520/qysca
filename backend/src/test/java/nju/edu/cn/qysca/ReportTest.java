package nju.edu.cn.qysca;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.service.report.ReportService;
import nju.edu.cn.qysca.service.report.ReportServiceImpl;
import nju.edu.cn.qysca.utils.FolderUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class ReportTest {

    @Autowired
    private ReportServiceImpl reportService;
    @Value("${tempReportFolder}")
    private String tempFolder;

    @Test
    public void test1(){
        String appName = "backend:clickhouse";
        String appVersion = "1.0.0";
        File dir = new File(tempFolder, "test");
        if (!dir.exists()){
            dir.mkdirs();
        }
        reportService.makeHtml(dir, appName, appVersion);
        //FolderUtil.deleteFolder(dir.getPath());
    }
}
