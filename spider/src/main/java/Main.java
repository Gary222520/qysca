import domain.component.JavaOpenComponentDO;
import spider.JavaOpenPomSpider;
import spider.Spider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        Spider<JavaOpenComponentDO> spider = new JavaOpenPomSpider();

        List<String> targetUrls = new ArrayList<>();
        String TARGET_URLS_FILE = "target_urls.txt";

        // 从target_urls.txt文件中读取目标URL
        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(TARGET_URLS_FILE);
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
            System.out.println("开始按目录爬取：" + targetUrl);
            spider.crawlManyWithDependency(targetUrl);
        }
    }
}
