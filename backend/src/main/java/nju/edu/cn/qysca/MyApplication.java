package nju.edu.cn.qysca;

import nju.edu.cn.qysca.run.GoSpiderRun;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class MyApplication {

    @Autowired
    private GoSpiderRun goSpiderRun;

    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }

    /**
     * 启动后运行go爬虫
     */
    @PostConstruct
    private void run(){
        goSpiderRun.executeSpiderTask();
    }
}
