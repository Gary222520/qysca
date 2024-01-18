package run;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import domain.component.JavaOpenComponentInformationDO;
import spider.JavaOpenPomSpider;

import java.io.File;
import java.io.IOException;

/**
 * 用于爬取甲方SBOM中出现的java开源组件
 */
public class RunSBOM {


    /**
     * 解析SBOM中的json，并爬取其中的java开源组件，存进数据库
     * @param filePath
     */
    private void parseSBOM(String filePath){
        File jsonFile = new File(filePath);

        JavaOpenPomSpider spider = JavaOpenPomSpider.getInstance();

        // 使用 ObjectMapper 解析 JSON
        ObjectMapper objectMapper = new ObjectMapper();

        try {
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
                JavaOpenComponentInformationDO informationDO = spider.crawlWithDependencyByGav(group,name,version);
                if (informationDO == null)
                    System.out.println("未爬取到相关信息");
                System.out.println("--------------------------------------");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        spider.flush();
    }

    /**
     * 程序入口
     */
    public static void main(String[] args) {

        String filePath1 = "spider/src/main/resources/SBOM/Backend/sbom-osa_module_sensor.json";
        String filePath2 = "spider/src/main/resources/SBOM/Filter/java/logstash-core.json";
        String filePath3 = "spider/src/main/resources/SBOM/Raw Storage/mergedBom.json";
        RunSBOM runSBOM = new RunSBOM();
        runSBOM.parseSBOM(filePath1);
        runSBOM.parseSBOM(filePath2);
        runSBOM.parseSBOM(filePath3);




    }
}
