package nju.edu.cn.qysca.utils.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class UrlConnector {
    /**
     * 休眠时间，单位毫秒
     */
    private static final int sleepTime = 100;

    /**
     * 给定一个url地址，爬取其内容至一个document
     * @param url url地址
     * @return Document org.jsoup.nodes.Document
     */
    public static Document getDocumentByUrl(String url) {
        try {
            // 每次爬取url时休眠一定时间，防止被ban
            Thread.sleep(sleepTime);
            // 连接到url并获取其内容
            return Jsoup.connect(url).get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
