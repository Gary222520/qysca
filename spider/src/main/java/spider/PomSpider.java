package spider;

import dataAccess.DataAccessInterface;
import domain.OpensourceComponentDO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.DataHandler;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class PomSpider implements Spider<OpensourceComponentDO>{

    private final DataAccessInterface<OpensourceComponentDO> dataAccess;

    /**
     * 用以记录以及爬取过的url，防止重复爬取
     */
    private Set<String> visitedUrls;
    private final String visitedUrlsFile = "spider/src/main/resources/visited_urls.txt";

    private final static String MAVEN_REPO_BASE_URL = "https://repo1.maven.org/maven2/";


    public PomSpider(DataAccessInterface<OpensourceComponentDO> dataAccess){
        this.dataAccess = dataAccess;
    }

    public void crawlMany(String directoryUrl){
        // 程序中断时自动调用，保存数据
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAndFlush));

        loadVisitedLinks();
        crawlDirectory(directoryUrl);
        saveVisitedLinks();
    }

    public OpensourceComponentDO crawlByGAV(String groupId, String artifactId, String version){
        String downloadUrl = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        String pomUrl = findPomFileUrlInDirectory(downloadUrl);
        if (pomUrl == null){
            return null;
        }
        return crawl(pomUrl);

    }

    /**
     * 爬取指定url目录下的所有pom文件
     * @param directoryUrl 目录url
     */
    private void crawlDirectory(String directoryUrl){
        if (visitedUrls.contains(directoryUrl)){
            return;
        }

        Document document = UrlConnector.getDocumentByUrl(directoryUrl);
        if (document == null)
            return;

        Elements links = document.select("a[href]");
        for (Element link : links) {
            String fileAbsUrl = link.absUrl("href");

            if (link.ownText().endsWith("/") && !link.ownText().endsWith("../")) {
                // 如果为目录，则递归
                crawlDirectory(fileAbsUrl);
            } else if (fileAbsUrl.endsWith(".pom") && !fileAbsUrl.contains("-javadoc")){
                // 如果为pom文件，则直接爬取
                this.dataAccess.enqueue(crawl(fileAbsUrl));
            }
        }

        visitedUrls.add(directoryUrl);
    }

    /**
     *  根据pom文件的url爬取单个pom文件，并转换为DO
     * @param pomUrl pom url
     */
    private OpensourceComponentDO crawl(String pomUrl) {
        if (!pomUrl.endsWith(".pom")){
            return null;
        }

        System.out.println("Crawling " + pomUrl);

        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        OpensourceComponentDO opensourceComponentDO = DataHandler.convertToDO(document, pomUrl, "JAVA","TRUE");
        return opensourceComponentDO;
    }


    private void loadVisitedLinks() {
        visitedUrls = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(visitedUrlsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                visitedUrls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveVisitedLinks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(visitedUrlsFile))) {
            for (String link : visitedUrls) {
                writer.write(link);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在指定目录url下查找.pom文件
     *
     * @param directoryUrl 目录url
     * @return 如果存在.pom文件则返回其url，否则返回null
     */
    private static String findPomFileUrlInDirectory(String directoryUrl) {

        Document document = UrlConnector.getDocumentByUrl(directoryUrl);

        if (document == null) {
            return null;
        }

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.pom结尾的文件
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            // 一般含-javadoc的不是需要的pom文件，这样的处理可能比较简单了
            if (fileAbsUrl.endsWith(".pom") && !fileAbsUrl.contains("-javadoc")) {
                return fileAbsUrl;
            }
        }
        System.err.println("No .pom file found in \"" + directoryUrl + "\"");
        return null;
    }

    /**
     * 在程序终止时，保存数据
     */
    private void saveAndFlush(){
        dataAccess.flush();
        saveVisitedLinks();
    }

}
