package spider;

import dataAccess.DataAccess;
import domain.component.JavaOpenComponentDO;
import domain.component.JavaOpenDependencyTableDO;
import domain.component.JavaOpenDependencyTreeDO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.ConvertUtil;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class JavaOpenPomSpider implements Spider<JavaOpenComponentDO>{

    private DataAccess<JavaOpenComponentDO> dataAccess;

    /**
     * 用以记录以及爬取过的url，防止重复爬取
     */
    private Set<String> visitedUrls;
    private final String VISITED_URLS_PATH = "visited_urls.txt";
    private final static String MAVEN_REPO_BASE_URL = "https://repo1.maven.org/maven2/";
    private final static String POM_FILE_TEMP_PATH = "spider/src/main/resources/temp_pom.xml";
    private final static String COLLECTION_NAME = "java_component_open_detail";

    public JavaOpenPomSpider(){
        dataAccess = new DataAccess<JavaOpenComponentDO>(COLLECTION_NAME, JavaOpenComponentDO.class);
    }

    /**
     * 爬取指定目录下的所有pom文件，并将爬取到的数据存储到数据库中
     * @param directoryUrl
     */
    @Override
    public void crawlMany(String directoryUrl){
        // 程序中断时自动调用，保存数据
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAndFlush));

        loadVisitedLinks();
        crawlDirectory(directoryUrl);
        saveVisitedLinks();
    }

    /**
     * 根据给定gav信息爬取pom文件，并将爬到的数据存储到数据库中
     * @param groupId
     * @param artifactId
     * @param version
     * @return JavaOpenComponentDO
     */
    public JavaOpenComponentDO crawlByGAV(String groupId, String artifactId, String version){
        String downloadUrl = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        String pomUrl = findPomFileUrlInDirectory(downloadUrl);
        if (pomUrl == null){
            return null;
        }
        JavaOpenComponentDO  javaOpenComponentDO = crawl(pomUrl);
        //刷新保存数据
        dataAccess.flush();
        return javaOpenComponentDO;
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
    private JavaOpenComponentDO crawl(String pomUrl) {
        if (!pomUrl.endsWith(".pom")){
            return null;
        }

        System.out.println("Crawling " + pomUrl);

        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        JavaOpenComponentDO javaOpenComponentDO = ConvertUtil.convertToJavaOpenComponentDO(document, pomUrl, MAVEN_REPO_BASE_URL);

        //todo 调用MavenUtil获得node，通过node构建dependencyTableDO和dependencyTreeDO，然后返回
        //createPomFile(document.outerHtml());
        JavaOpenDependencyTableDO javaOpenDependencyTableDO = null;
        JavaOpenDependencyTreeDO javaOpenDependencyTreeDO = null;



        return javaOpenComponentDO;
    }

    /**
     * 加载已访问过的url
     */
    private void loadVisitedLinks() {
        visitedUrls = new HashSet<>();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(VISITED_URLS_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                visitedUrls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存已访问过的url
     */
    private void saveVisitedLinks() {
        try (OutputStream outputStream = new FileOutputStream(VISITED_URLS_PATH);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
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
     * 创建临时的pom文件（用以调用mvn命令）
     * @param pomString
     */
    private static void createPomFile(String pomString){
        try {
            // 创建FileWriter对象
            FileWriter writer = new FileWriter(POM_FILE_TEMP_PATH, false);
            // 写入内容
            writer.write(pomString);
            // 关闭文件写入流
            writer.close();
        } catch (IOException e) {
            System.out.println("创建临时pom文件时发生错误：" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 在程序终止时，保存数据
     */
    private void saveAndFlush(){
        dataAccess.flush();
        saveVisitedLinks();
    }

}
