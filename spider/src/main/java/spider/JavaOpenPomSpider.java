package spider;

import data.BatchDataWriter;
import domain.component.*;
import fr.dutra.tools.maven.deptree.core.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.ConvertUtil;
import util.MavenUtil;
import util.idGenerator.UUIDGenerator;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaOpenPomSpider implements Spider<JavaOpenComponentDO> {

    /**
     * 连接数据库
     */
    private final BatchDataWriter<JavaOpenComponentDO> componentWriter;

    private final BatchDataWriter<JavaOpenDependencyTreeDO> dependencyTreeWriter;

    private final BatchDataWriter<JavaOpenDependencyTableDO> dependencyTableWriter;

    /**
     * 用以记录以及爬取过的url，防止重复爬取
     */
    private Set<String> visitedUrls;
    private final String VISITED_URLS_PATH = "spider/src/main/resources/visited_urls.txt";
    private final static String POM_FILE_TEMP_PATH = "spider/src/main/resources/pomFile/temp_pom.xml";
    private final static String MAVEN_REPO_BASE_URL = "https://repo1.maven.org/maven2/";
    /**
     * collection_name对象在mongodb中的collection
     */
    private final static String COMPONENT_COLLECTION_NAME = "java_component_open_detail";
    private final static String DEPENDENCY_TREE_COLLECTION_NAME = "java_component_open_dependency_tree";
    private final static String DEPENDENCY_TABLE_COLLECTION_NAME = "java_component_open_dependency_table";

    private static JavaOpenPomSpider instance;

    // 提供全局访问点，加上 synchronized 关键字确保线程安全
    public static synchronized JavaOpenPomSpider getInstance() {
        if (instance == null) {
            instance = new JavaOpenPomSpider();
        }
        return instance;
    }

    private JavaOpenPomSpider() {
        componentWriter = new BatchDataWriter<JavaOpenComponentDO>(COMPONENT_COLLECTION_NAME, JavaOpenComponentDO.class);
        dependencyTreeWriter = new BatchDataWriter<JavaOpenDependencyTreeDO>(DEPENDENCY_TREE_COLLECTION_NAME, JavaOpenDependencyTreeDO.class);
        dependencyTableWriter = new BatchDataWriter<JavaOpenDependencyTableDO>(DEPENDENCY_TABLE_COLLECTION_NAME, JavaOpenDependencyTableDO.class);
    }

    /**
     * 爬取指定目录下的所有pom文件，并将爬取到的数据存储到数据库中
     *
     * @param directoryUrl 目录url
     */
    @Override
    public void crawlManyWithDependency(String directoryUrl) {
        // 程序中断时自动调用，保存数据
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAndFlush));

        loadVisitedLinks();
        crawlDirectoryWithDependency(directoryUrl);
        saveVisitedLinks();
    }


    /**
     * 根据给定gav信息爬取pom文件，并将爬到的数据存储到数据库中
     *
     * @param groupId
     * @param artifactId
     * @param version
     * @return JavaOpenComponentDO
     */
    public JavaOpenComponentDO crawlByGav(String groupId, String artifactId, String version) {
        // 根据gav拼出pomUrl
        String downloadUrl = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        String pomUrl = findPomFileUrlInDirectory(downloadUrl);
        if (pomUrl == null) {
            return null;
        }
        // 爬取该pomUrl
        JavaOpenComponentDO javaOpenComponentDO = crawl(pomUrl);
        return javaOpenComponentDO;
    }

    /**
     * 根据给定gav信息爬取pom文件，同时获得其依赖树和平铺依赖图，并将这些数据存储到数据库中
     * @param groupId
     * @param artifactId
     * @param version
     */
    public JavaOpenComponentInformationDO crawlWithDependencyByGav(String groupId, String artifactId, String version){

        // 根据gav拼出pomUrl
        String downloadUrl = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        String pomUrl = findPomFileUrlInDirectory(downloadUrl);
        if (pomUrl == null) {
            return null;
        }

        JavaOpenComponentInformationDO javaOpenComponentInformationDO = crawlWithDependency(pomUrl);

        return javaOpenComponentInformationDO;
    }

    /**
     * 爬取指定url目录下的所有pom文件
     *
     * @param directoryUrl 目录url
     */
    private void crawlDirectory(String directoryUrl) {
        if (visitedUrls.contains(directoryUrl)) {
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
            } else if (fileAbsUrl.endsWith(".pom") && !fileAbsUrl.contains("-javadoc")) {
                // 如果为pom文件，则直接爬取
                crawl(fileAbsUrl);
            }
        }

        visitedUrls.add(directoryUrl);
    }

    /**
     * 爬取指定url目录下的所有pom文件,同时获得其依赖树和平铺依赖图，并将这些数据存储到数据库中
     *
     * @param directoryUrl 目录url
     */
    private void crawlDirectoryWithDependency(String directoryUrl){
        if (visitedUrls.contains(directoryUrl)) {
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
                crawlDirectoryWithDependency(fileAbsUrl);
            } else if (fileAbsUrl.endsWith(".pom") && !fileAbsUrl.contains("-javadoc")) {
                // 如果为pom文件，则直接爬取
                crawlWithDependency(fileAbsUrl);
            }
        }

        visitedUrls.add(directoryUrl);
    }

    /**l
     * 根据pom文件的url爬取单个pom文件，并转换为DO，存进数据库
     *
     * @param pomUrl pom url
     */
    private JavaOpenComponentDO crawl(String pomUrl) {
        if (!pomUrl.endsWith(".pom")) {
            return null;
        }

        System.out.println("  Crawling " + pomUrl);

        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        JavaOpenComponentDO javaOpenComponentDO = ConvertUtil.convertToJavaOpenComponentDO(document, pomUrl, MAVEN_REPO_BASE_URL);
        componentWriter.enqueue(javaOpenComponentDO);

        return javaOpenComponentDO;
    }


    /**
     * 根据pom文件的url爬取单个pom文件，并转换为DO，同时获得其依赖树和平铺依赖图，存进数据库
     *
     * @param pomUrl
     * @return JavaOpenComponentInformationDO
     */
    private JavaOpenComponentInformationDO crawlWithDependency(String pomUrl){
        if (!pomUrl.endsWith(".pom")) {
            return null;
        }

        System.out.println("Crawling " + pomUrl);

        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        // 获得组件信息
        JavaOpenComponentDO javaOpenComponentDO = ConvertUtil.convertToJavaOpenComponentDO(document, pomUrl, MAVEN_REPO_BASE_URL);

        // 生成一个临时pom文件
        createPomFile(document.outerHtml());
        // 调用mvn dependency:tree命令获取依赖
        Node node = MavenUtil.mavenDependencyTreeAnalyzer(POM_FILE_TEMP_PATH);
        // 解析依赖，获得组件树
        MavenUtil mavenUtil = new MavenUtil();
        ComponentDependencyTreeDO dependencyTreeDO = mavenUtil.convertNode(node, 0);

        // 封装依赖树
        JavaOpenDependencyTreeDO javaOpenDependencyTreeDO = new JavaOpenDependencyTreeDO();
        javaOpenDependencyTreeDO.setTree(dependencyTreeDO);

        javaOpenDependencyTreeDO.setId(UUIDGenerator.getUUID());
        javaOpenDependencyTreeDO.setGroupId(javaOpenComponentDO.getGroupId());
        javaOpenDependencyTreeDO.setArtifactId(javaOpenComponentDO.getArtifactId());
        javaOpenDependencyTreeDO.setVersion(javaOpenComponentDO.getVersion());

        // 封装依赖表
        List<JavaOpenDependencyTableDO> javaOpenDependencyTableDOList = MavenUtil.buildJavaOpenDependencyTable(javaOpenDependencyTreeDO);

        componentWriter.enqueue(javaOpenComponentDO);
        dependencyTreeWriter.enqueue(javaOpenDependencyTreeDO);
        for (JavaOpenDependencyTableDO javaOpenDependencyTableDO : javaOpenDependencyTableDOList)
            dependencyTableWriter.enqueue(javaOpenDependencyTableDO);

        // 封装
        JavaOpenComponentInformationDO javaOpenComponentInformationDO = new JavaOpenComponentInformationDO();
        javaOpenComponentInformationDO.setJavaOpenComponentDO(javaOpenComponentDO);
        javaOpenComponentInformationDO.setJavaOpenDependencyTreeDO(javaOpenDependencyTreeDO);
        javaOpenComponentInformationDO.setJavaOpenDependencyTableDO(javaOpenDependencyTableDOList);

        return javaOpenComponentInformationDO;
    }

    /**
     * 加载已访问过的url
     */
    private void loadVisitedLinks() {
        visitedUrls = new HashSet<>();
        try (InputStream inputStream = new FileInputStream(VISITED_URLS_PATH);
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
     *
     * @param pomString
     */
    private static void createPomFile(String pomString) {
        try (OutputStream outputStream = new FileOutputStream(POM_FILE_TEMP_PATH);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(pomString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在程序终止时，保存数据
     */
    private void saveAndFlush() {
        componentWriter.flush();
        dependencyTreeWriter.flush();
        dependencyTableWriter.flush();
        saveVisitedLinks();
    }

    /**
     * 手动刷新
     */
    public void flush(){
        componentWriter.flush();
        dependencyTreeWriter.flush();
        dependencyTableWriter.flush();
    }
}
