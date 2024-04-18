package nju.edu.cn.qysca;

import nju.edu.cn.qysca.run.JsSpiderRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class MyApplication {

    @Autowired
    private JsSpiderRun jsSpiderRun;

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    /**
     * 启动后运行js爬虫
     */
    @PostConstruct
    private void run(){
        jsSpiderRun.executeSpiderTask();
    }
}
