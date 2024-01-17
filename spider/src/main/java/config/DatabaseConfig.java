package config;

import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Map;

/**
 * 读取数据库yaml配置文件
 */
public class DatabaseConfig {
    private static String DATABASE_CONFIG_PATH = "database.yml";

    private static String databaseUrl = null;
    private static String databaseName = null;

    static {
        try {
            // 使用类加载器获取InputStream
            // 使用类加载器的原因避免由于相对路径的问题导致找不到文件
            ClassLoader classLoader = DatabaseConfig.class.getClassLoader();
            InputStream inputStream = classLoader.getResourceAsStream(DATABASE_CONFIG_PATH);

            // 创建Yaml对象
            Yaml yaml = new Yaml();

            // 从InputStream中加载YAML数据
            Map<String, Object> environmentData = yaml.load(inputStream);

            // 获取环境信息
            String environment = (String) environmentData.get("active");

            // 根据环境选择加载相应的配置文件
            String databaseConfigFilePath = "database-" + environment + ".yml";
            InputStream databaseConfigInputStream = classLoader.getResourceAsStream(databaseConfigFilePath);
            Map<String, Object> databaseConfigData = yaml.load(databaseConfigInputStream);

            // 根据需要获取具体配置信息
            Map<String, Object> databaseInfo = (Map<String, Object>) databaseConfigData.get("database");
            databaseUrl = (String) databaseInfo.get("url");
            databaseName = (String) databaseInfo.get("name");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getDatabaseUrl() {
        return databaseUrl;
    }

    public static String getDatabaseName() {
        return databaseName;
    }
}