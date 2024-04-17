package nju.edu.cn.qysca.run;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
    @Autowired
    GoSpiderRun goSpiderRun;

    /**
     * 批量爬取java组件
     */
    @Test
    public void executeJavaSpiderTask(){javaSpiderRun.executeSpiderTask();}

    /**
     * 批量爬取python包
     */
    @Test
    public void executePythonSpiderTask(){pythonSpiderRun.executeSpiderTask();}

    /**
     * 批量爬取js组件
     */
    @Test
    public void executeJsSpiderTask(){jsSpiderRun.executeSpiderTask();}

    /**
     * 批量爬取go组件
     */
    @Test
    public void executeGoSpiderTask(){goSpiderRun.executeSpiderTask();}
}
