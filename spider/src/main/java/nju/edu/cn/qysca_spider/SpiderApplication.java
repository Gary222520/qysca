package nju.edu.cn.qysca_spider;

import nju.edu.cn.qysca_spider.spider.JavaSpider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class SpiderApplication {

    @Autowired
    private JavaSpider spider;

    public static void main(String[] args) {

        SpringApplication.run(SpiderApplication.class, args);
    }

    @Autowired
    private DataSource dataSource;

    public void printDatabaseInfo() {
        try (Connection connection = dataSource.getConnection()) {
            System.out.println("Connected to database: " + connection.getCatalog());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 启动时自动运行
     */
    @PostConstruct
    public void execute(){

        printDatabaseInfo();

        List<String> targetUrls = new ArrayList<>();
//        String TARGET_URLS_FILE = "target_urls.txt";
//
//        // 从target_urls.txt文件中读取目标URL
//        try (InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(TARGET_URLS_FILE);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                targetUrls.add(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        targetUrls.add("https://repo1.maven.org/maven2/junit/");
        // 开始爬取
        for (String targetUrl : targetUrls) {
            if (targetUrl.isEmpty() || targetUrl.startsWith("//"))
                continue;
            System.out.println("开始按目录爬取：" + targetUrl);
            spider.crawlManyWithDependency(targetUrl);
            System.out.println("该目录爬取完毕: " + targetUrl);
        }
    }
}
