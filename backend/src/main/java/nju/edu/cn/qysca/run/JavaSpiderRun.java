package nju.edu.cn.qysca.run;

import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;
import nju.edu.cn.qysca.domain.spider.dos.MavenVisitedUrlsDO;
import nju.edu.cn.qysca.service.spider.JavaSpiderService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class JavaSpiderRun {

    @Autowired
    private JavaSpiderService javaSpiderService;
    @Autowired
    private JavaComponentDao javaComponentDao;
    @Value("${targetUrlsPath}")
    private String TARGET_URLS_PATH;

    public void executeJavaSpiderTask(){
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
            javaSpiderService.crawlDirectory(targetUrl);
            System.out.println("该目录爬取完毕: " + targetUrl);
        }
    }


    /**
     * 批量爬取java组件（还会构造并存储该组件的依赖树、依赖表，并爬取该组件的所以依赖）
     * （暂时废弃, 无法使用）
     */
    public void executeJavaSpiderWithDependencyTask(){
//        List<String> targetUrls = new ArrayList<>();
//
//        // 从target_urls.txt文件中读取目标URL
//        try (InputStream inputStream = new FileInputStream(TARGET_URLS_PATH);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                targetUrls.add(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // 开始爬取
//        for (String targetUrl : targetUrls) {
//            if (targetUrl.isEmpty() || targetUrl.startsWith("//"))
//                continue;
//            System.out.println("开始按目录爬取：" + targetUrl);
//            javaSpiderService.crawlDirectoryWithDependency(targetUrl);
//            System.out.println("该目录爬取完毕: " + targetUrl);
//        }
    }
}
