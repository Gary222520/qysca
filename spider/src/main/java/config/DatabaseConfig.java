package config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;

public class DatabaseConfig {
    private static String RESOURCES_PATH = "spider/src/main/resources/";
    private static String DATABASE_CONFIG_PATH = "spider/src/main/resources/database.yml";

    private static String databaseUrl = null;
    private static String databaseName = null;

    static {
        try {
            // 创建Yaml对象
            Yaml yaml = new Yaml();

            // 从文件中加载YAML数据
            FileInputStream environmentInputStream = new FileInputStream(DATABASE_CONFIG_PATH);
            Map<String, Object> environmentData = yaml.load(environmentInputStream);

            // 获取环境信息
            String environment = (String) environmentData.get("active");

            // 根据环境选择加载相应的配置文件
            String databaseConfigFilePath = RESOURCES_PATH + "database-" + environment + ".yml";
            FileInputStream databaseConfigInputStream = new FileInputStream(databaseConfigFilePath);
            Map<String, Object> databaseConfigData = yaml.load(databaseConfigInputStream);

            // 根据需要获取具体配置信息
            Map<String, Object> databaseInfo = (Map<String, Object>) databaseConfigData.get("database");
            databaseUrl = (String) databaseInfo.get("url");
            databaseName = (String) databaseInfo.get("name");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static String getDatabaseUrl(){
        return databaseUrl;
    }
    public static String getDatabaseName(){
        return databaseName;
    }
}
