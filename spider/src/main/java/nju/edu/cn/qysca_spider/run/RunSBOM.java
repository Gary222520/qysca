//package run;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import data.BatchDataWriter;
//import domain.component.*;
//import spider.JavaOpenSpider;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 用于爬取甲方SBOM中出现的java开源组件
// */
//public class RunSBOM {
//
//
//    /**
//     * 连接数据库
//     */
//    private final BatchDataWriter<JavaOpenComponentDO> componentWriter;
//    private final BatchDataWriter<JavaOpenDependencyTreeDO> dependencyTreeWriter;
//    private final BatchDataWriter<JavaOpenDependencyTableDO> dependencyTableWriter;
//    private final static String COMPONENT_COLLECTION_NAME = "java_component_open_detail";
//    private final static String DEPENDENCY_TREE_COLLECTION_NAME = "java_component_open_dependency_tree";
//    private final static String DEPENDENCY_TABLE_COLLECTION_NAME = "java_component_open_dependency_table";
//
//    public RunSBOM(){
//        componentWriter = new BatchDataWriter<JavaOpenComponentDO>(COMPONENT_COLLECTION_NAME, JavaOpenComponentDO.class);
//        dependencyTreeWriter = new BatchDataWriter<JavaOpenDependencyTreeDO>(DEPENDENCY_TREE_COLLECTION_NAME, JavaOpenDependencyTreeDO.class);
//        dependencyTableWriter = new BatchDataWriter<JavaOpenDependencyTableDO>(DEPENDENCY_TABLE_COLLECTION_NAME, JavaOpenDependencyTableDO.class);
//    }
//
//
//    /**
//     * 解析SBOM中的json，并爬取其中的java开源组件，存进数据库
//     *
//     * @param filePath
//     */
//    private void parseSBOM(String filePath) {
//        File jsonFile = new File(filePath);
//
//        JavaOpenSpider spider = JavaOpenSpider.getInstance();
//
//        // 使用 ObjectMapper 解析 JSON
//        ObjectMapper objectMapper = new ObjectMapper();
//
//        try {
//            JsonNode rootNode = objectMapper.readTree(jsonFile);
//
//            // 获取 components 数组
//            JsonNode componentsNode = rootNode.get("components");
//
//            // 遍历 components 数组
//            for (JsonNode componentNode : componentsNode) {
//                // 获取 group、name、version 信息
//                String group = componentNode.get("group").asText();
//                String name = componentNode.get("name").asText();
//                String version = componentNode.get("version").asText();
//
//                // 打印结果
//                System.out.println("Group: " + group + ", Name: " + name + ", Version: " + version);
//                // 按照GAV爬取
//                JavaOpenComponentInformationDO informationDO = spider.crawlWithDependencyByGav(group, name, version);
//                if (informationDO == null)
//                    System.out.println("未爬取到相关信息");
//                else {
//                    // 查找SBOM中的hashes信息
//                    List<HashDO> hashes = new ArrayList<>();
//
//                    JsonNode hashesNode = componentNode.get("hashes");
//                    for (JsonNode hashNode : hashesNode){
//                        HashDO hashDO = new HashDO();
//                        hashDO.setAlgorithm(hashNode.get("alg").asText());
//                        hashDO.setContent(hashNode.get("content").asText());
//                        hashes.add(hashDO);
//                    }
//                    informationDO.getJavaOpenComponentDO().setHashes(hashes);
//
//                    // 写入数据库
//                    componentWriter.enqueue(informationDO.getJavaOpenComponentDO());
//
//                    if (informationDO.getJavaOpenDependencyTreeDO() != null)
//                        dependencyTreeWriter.enqueue(informationDO.getJavaOpenDependencyTreeDO());
//
//                    if (informationDO.getJavaOpenDependencyTableDO() != null)
//                        for (JavaOpenDependencyTableDO javaOpenDependencyTableDO : informationDO.getJavaOpenDependencyTableDO())
//                            dependencyTableWriter.enqueue(javaOpenDependencyTableDO);
//
//                }
//                System.out.println("--------------------------------------");
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        spider.flush();
//    }
//
//    /**
//     * 程序入口
//     */
//    public static void main(String[] args) {
//
//        String filePath1 = "spider/src/main/resources/SBOM/Backend/sbom-osa_module_sensor.json";
//        String filePath2 = "spider/src/main/resources/SBOM/Filter/java/logstash-core.json";
//        String filePath3 = "spider/src/main/resources/SBOM/Raw Storage/mergedBom.json";
//        RunSBOM runSBOM = new RunSBOM();
//        runSBOM.parseSBOM(filePath1);
//        runSBOM.parseSBOM(filePath2);
//        runSBOM.parseSBOM(filePath3);
//
//
//    }
//}
