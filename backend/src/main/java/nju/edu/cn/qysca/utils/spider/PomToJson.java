package nju.edu.cn.qysca.utils.spider;

import org.json.JSONObject;
import org.json.XML;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class PomToJson {
    /**
     * 将pom File转成json对象
     * @param file pom File
     * @return json对象
     */
    public static JSONObject convertPomFileToJson(File file){
        String fileName = file.getName();
        String fileContent = readFile(file);

        String groupId = fileName.substring(0, fileName.lastIndexOf("-"));
        String artifactId = fileName.substring(fileName.lastIndexOf("-") + 1, fileName.lastIndexOf("."));;
        String version = fileName.substring(fileName.lastIndexOf(".") + 1);

        // 包装成json对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", groupId + ":" + artifactId + ":" + version);
        jsonObject.put("groupId", groupId);
        jsonObject.put("artifactId", artifactId);
        jsonObject.put("version", version);
        jsonObject.put("pom",convertXMLToJson(fileContent));

        return jsonObject;
    }

    /**
     * 将xml字符串转化为json对象
     * @param xmlString xml字符串
     * @return json对象
     */
    private static JSONObject convertXMLToJson(String xmlString){
        return XML.toJSONObject(xmlString);
    }

    /**
     * 从文件中读取内容
     * @param file File
     * @return String
     */
    private static String readFile(File file) {
        // 使用StringBuilder存储文件内容
        StringBuilder content = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;

            // 逐行读取文件内容并追加到StringBuilder
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  content.toString();
    }
}
