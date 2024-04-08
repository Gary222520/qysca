package nju.edu.cn.qysca;

import nju.edu.cn.qysca.service.spider.JavaSpiderService;
import nju.edu.cn.qysca.service.spider.PythonSpiderService;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class SpiderRunApplication {

    @Autowired
    private JavaSpiderService javaSpiderService;
    @Autowired
    private PythonSpiderService pythonSpiderService;
    @Value("${targetUrlsPath}")
    private String TARGET_URLS_PATH;

    public static void main(String[] args) {
        SpringApplication.run(SpiderRunApplication.class);
    }

    @PostConstruct
    private void executeJavaSpiderTask(){
        List<String> targetUrls = new ArrayList<>();

        // 从target_urls.txt文件中读取目标URL
        try (InputStream inputStream = new FileInputStream(TARGET_URLS_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                targetUrls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 开始爬取
        for (String targetUrl : targetUrls) {
            if (targetUrl.isEmpty() || targetUrl.startsWith("//"))
                continue;
            System.out.println("开始按目录爬取：" + targetUrl);
            javaSpiderService.crawlDirectoryWithDependency(targetUrl);
            System.out.println("该目录爬取完毕: " + targetUrl);
        }


    }


}
