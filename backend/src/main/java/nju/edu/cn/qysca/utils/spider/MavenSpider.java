package nju.edu.cn.qysca.utils.spider;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;

public class MavenSpider {

    private static final String MAVEN_CENTRAL_URL = "https://repo1.maven.org/maven2/";

    /**
     *
     * @param url
     * @param targetFolder
     */
    public static void crawlAndDownload(String url, String targetFolder) {
        if (!url.startsWith(MAVEN_CENTRAL_URL)){
            System.err.println("该url并不是指定的maven仓库:" + url);
            return;
        }

        Document document = UrlConnector.getDocumentByUrl(url);
        if (document == null)
            return;

        Elements links = document.select("a[href]");

        for (Element link : links) {
            String fileAbsUrl = link.absUrl("href");

            if (link.ownText().endsWith("/") && !link.ownText().endsWith("../")) {
                // 如果为目录，则递归爬取
                crawlAndDownload(fileAbsUrl, targetFolder);
            } else if (fileAbsUrl.endsWith(".pom")) {
                // 如果以.pom结尾，则下载

                //一般含-javadoc的不是需要的pom文件，这样的处理可能比较简单了
                if (fileAbsUrl.contains("-javadoc")) {
                    continue;
                }

                // 从URL中提取groupId、artifactId、version
                String relativePath = fileAbsUrl.substring(MAVEN_CENTRAL_URL.length()); // 获取相对路径
                String[] parts = relativePath.split("/");

                String version = parts[parts.length - 2];
                String artifactId = parts[parts.length - 3];
                String groupId = String.join(".", Arrays.copyOfRange(parts, 0, parts.length - 3));

                // 下载POM文件并以groupId_artifactId_version.pom命名
                UrlConnector.downLoadFromUrl(fileAbsUrl,groupId + "_" + artifactId + "_" + version + ".pom", targetFolder);

                System.out.println("download pom : " + fileAbsUrl + " finished.");
            }
        }
    }

    public static void main(String[] args) {
        MavenSpider.crawlAndDownload("https://repo1.maven.org/maven2/org/springframework/boot/", "backend/src/main/resources/pomFiles");
    }

}
