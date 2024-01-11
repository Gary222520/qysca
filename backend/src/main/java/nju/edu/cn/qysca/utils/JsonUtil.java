package nju.edu.cn.qysca.utils;

import org.springframework.boot.configurationprocessor.json.JSONObject;

public class JsonUtil {

    public static String extractValue(String jsonString, String key) {
        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            return jsonObject.getString(key);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
