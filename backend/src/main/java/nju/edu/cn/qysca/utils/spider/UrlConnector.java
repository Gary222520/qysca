package nju.edu.cn.qysca.utils.spider;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.FileOutputStream;
import java.io.IOException;

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
            //Thread.sleep(sleepTime);
            // 连接到url并获取其内容
            return Jsoup.connect(url).get();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 给定一个url地址，下载其内容至一个地址
     * @param url url地址
     * @param destinationPath 目的地址/下载地址
     */
    public static void downloadFile(String url, String destinationPath) {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(url);

        try {
            try (CloseableHttpResponse response = httpClient.execute(httpGet)) {
                if (response.getStatusLine().getStatusCode() == 200) {
                    try (FileOutputStream fileOutputStream = new FileOutputStream(destinationPath)) {
                        response.getEntity().writeTo(fileOutputStream);
                    }
                } else {
                    System.err.println("Failed to download POM file. HTTP status code: " + response.getStatusLine().getStatusCode());
                }
            } finally {
                httpClient.close();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
