package nju.edu.cn.qysca.utils.spider;

import nju.edu.cn.qysca.utils.parser.PomParser;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class PomSpider {

    /**
     * 通过url获取pom文件，存至Document对象中
     * @param pomUrl pom文件的url
     * @return org.jsoup.nodes.Document
     */
    public static Document getPomAsDocument(String pomUrl){

        System.out.println("working on : " + pomUrl);

        // 确认url存在并且为一个pom文件
        if (pomUrl == null || !pomUrl.endsWith(".pom")) {
            System.err.println("\"" + pomUrl + "\" is not a valid url or a pom file.");
            return null;
        }

        // 以org.jsoup.nodes.Document返回其内容
        return (Document) UrlConnector.getDocumentByUrl(pomUrl);
    }

    /**
     * 通过url获取pom文件，下载到指定目录下
     * @param pomUrl pom文件的url
     * @param downloadPath 下载路径
     */
    public static void getPomAndDownload(String pomUrl, String downloadPath){
        //todo
    }

    /**
     * 在指定目录url下查找.pom文件
     * @param directoryUrl 目录url
     * @return 如果存在.pom文件则返回其url，否则返回null
     */
    public static String findPomFileUrlInDirectory(String directoryUrl) {

        Document document = UrlConnector.getDocumentByUrl(directoryUrl);

        // 确认url存在
        if (document == null) {
            System.err.println("\"" + directoryUrl + "\" is not a valid url.");
            // todo 直接返回null可能存在问题，具体如何解决需要考虑
            return null;
        }

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.pom结尾的文件
        for (Element fileElement : fileElements) {
            String fileUrl = fileElement.absUrl("href");
            if (fileUrl.endsWith(".pom")) {
                return fileUrl;
            }
        }
        // todo 直接返回null可能存在问题，具体如何解决需要考虑
        return null;
    }

}
