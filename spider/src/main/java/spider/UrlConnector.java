package spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class UrlConnector {
    /**
     * 休眠时间，单位毫秒
     */
    private static final int sleepTime = 100;

    /**
     * 给定一个url地址，爬取其内容至一个document
     *
     * @param url url地址
     * @return Document org.jsoup.nodes.Document
     */
    public static Document getDocumentByUrl(String url) {
        try {
            // 每次爬取url时休眠一定时间，防止被ban
            //Thread.sleep(sleepTime);
            // 连接到url并获取其内容
            return Jsoup.connect(url).get();
        } catch (Exception e) {
            System.err.println("can't visit or it is invalid: " + url);
            return null;
        }
    }
}
