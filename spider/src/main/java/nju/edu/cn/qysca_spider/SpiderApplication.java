package nju.edu.cn.qysca_spider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca_spider.domain.ComponentInformationDO;
import nju.edu.cn.qysca_spider.service.spider.JavaSpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


@SpringBootApplication
public class SpiderApplication {

    @Autowired
    private JavaSpiderService spider;

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
    public void execute() {

        printDatabaseInfo();

        List<String> targetUrls = new ArrayList<>();
        String TARGET_URLS_FILE = "spider/src/main/resources/target_urls.txt";

        // 从target_urls.txt文件中读取目标URL
        try (InputStream inputStream = new FileInputStream(TARGET_URLS_FILE);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                targetUrls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

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

    /**
     * 爬取SBOM中出现的java组件
     */
    //@PostConstruct
    public void crawlSBOM() {
        System.out.println("Start crawling component in SBOM...");
        List<String> filePathList = new ArrayList<>();
        filePathList.add("spider/src/main/resources/SBOM/Backend/sbom-osa_module_sensor.json");
        filePathList.add("spider/src/main/resources/SBOM/Filter/java/logstash-core.json");
        filePathList.add("spider/src/main/resources/SBOM/Raw Storage/mergedBom.json");
        for (String filePath : filePathList) {
            try {
                File jsonFile = new File(filePath);
                // 使用 ObjectMapper 解析 JSON
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(jsonFile);

                // 获取 components 数组
                JsonNode componentsNode = rootNode.get("components");

                // 遍历 components 数组
                for (JsonNode componentNode : componentsNode) {
                    // 获取 group、name、version 信息
                    String group = componentNode.get("group").asText();
                    String name = componentNode.get("name").asText();
                    String version = componentNode.get("version").asText();

                    // 打印结果
                    System.out.println("Group: " + group + ", Name: " + name + ", Version: " + version);
                    // 按照GAV爬取
                    ComponentInformationDO informationDO = spider.crawlWithDependencyByGAV(group, name, version);
                    if (informationDO == null)
                        System.out.println("未爬取到相关信息");
                    System.out.println("--------------------------------------");
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
