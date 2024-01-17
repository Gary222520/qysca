package nju.edu.cn.qysca.utils.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.*;
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


    /**
     * 从指定Url中下载文件
     *
     * @param urlStr   目标url
     * @param fileName 文件名
     * @param savePath 保存目录
     */
    public static void downLoadFromUrl(String urlStr, String fileName, String savePath) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            //设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            //防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            //得到输入流
            InputStream inputStream = conn.getInputStream();
            //获取自己数组
            byte[] getData = readInputStream(inputStream);

            //文件保存位置
            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            File file = new File(saveDir + "/" + fileName);
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(getData);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 从输入流中获取字节数组
     *
     * @param inputStream
     * @return byte[]
     * @throws IOException
     */
    private static byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }

}
