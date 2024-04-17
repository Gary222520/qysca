package nju.edu.cn.qysca.run;

import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.dao.spider.NpmVisitedPackagesDao;
import nju.edu.cn.qysca.dao.spider.PythonVisitedPackagesDao;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.spider.dos.NpmVisitedPackagesDO;
import nju.edu.cn.qysca.domain.spider.dos.PythonVisitedPackagesDO;
import nju.edu.cn.qysca.service.python.PythonService;
import nju.edu.cn.qysca.service.spider.GoSpiderService;
import nju.edu.cn.qysca.service.spider.JavaSpiderService;
import nju.edu.cn.qysca.service.spider.JsSpiderService;
import nju.edu.cn.qysca.service.spider.PythonSpiderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 批量爬取入口
 */
@SpringBootTest
@Slf4j
public class SpiderRun {
    @Autowired
    JavaSpiderRun javaSpiderRun;
    @Autowired
    PythonSpiderRun pythonSpiderRun;
    @Autowired
    JsSpiderRun jsSpiderRun;


    /**
     * 批量爬取java组件
     */
    @Test
    public void executeJavaSpiderTask(){javaSpiderRun.executeJavaSpiderTask();}

    /**
     * 批量爬取python包
     */
    @Test
    public void executePythonSpiderTask(){
        pythonSpiderRun.executePythonSpiderTask();
    }

    /**
     * 批量爬取js组件
     */
    @Test
    public void executeJsSpiderTask(){
        jsSpiderRun.executeJsSpiderTask();
    }
}
