package spider;

import domain.component.*;
import fr.dutra.tools.maven.deptree.core.Node;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import utils.ConvertUtil;
import utils.MavenUtil;
import utils.idGenerator.UUIDGenerator;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class JavaSpider implements Spider {
    /**
     * 用以记录以及爬取过的url，防止重复爬取
     */
    private Set<String> visitedUrls;
    /**
     * 记录已访问过的url地址
     */
    private final String VISITED_URLS_PATH = "spider/src/main/resources/visited_urls.txt";
    /**
     * 记录爬取失败的url地址
     */
    private final String FAILED_URLS_PATH = "spider/src/main/resources/failed_urls.txt";
    /**
     * 临时生成的pom文件地址
     */
    private final static String POM_FILE_TEMP_PATH = "spider/src/main/resources/pomFile/temp_pom.xml";
    /**
     * Maven仓库根地址
     */
    private final static String MAVEN_REPO_BASE_URL = "https://repo1.maven.org/maven2/";

    /**
     * 单例模式
     */
    private static JavaSpider instance;

    /**
     * 全局访问点，synchronized 关键字确保线程安全
     * @return
     */
    public static synchronized JavaSpider getInstance() {
        if (instance == null) {
            instance = new JavaSpider();
        }
        return instance;
    }
    private JavaSpider(){

    }

    /**
     * 带依赖的爬取指定目录url下所有组件
     */
    public void crawlManyWithDependency(String directoryUrl){
        // 程序中断时自动调用，保存数据
        Runtime.getRuntime().addShutdownHook(new Thread(this::saveAndFlush));

        loadVisitedLinks();
        crawlDirectoryWithDependency(directoryUrl);
        saveVisitedLinks();
    }

    /**
     *  递归地爬取目录url下（带依赖的）所有组件
     */
    private void crawlDirectoryWithDependency(String directoryUrl){
        // 如果该url已被访问过，跳过
        if (visitedUrls.contains(directoryUrl)) {
            return;
        }

        Document document = UrlConnector.getDocumentByUrl(directoryUrl);
        if (document == null)
            return;

        boolean isLastLevel = true;
        Elements links = document.select("a[href]");
        // 遍历目录下所有链接
        for (Element link : links) {
            String fileAbsUrl = link.absUrl("href");
            // 如果该目录下的链接为目录，说明自身不是最后一层目录，同时递归爬取该链接
            if (fileAbsUrl.endsWith("/") && !fileAbsUrl.endsWith("../")){
                isLastLevel = false;
                crawlDirectoryWithDependency(fileAbsUrl);
            }
        }
        // 如果该目录为最后一层，进行爬取
        if (isLastLevel) {
            ComponentInformationDO informationDO = crawlWithDependency(directoryUrl);
            if (informationDO == null)
                writeFailedUrl(directoryUrl);
        }

        // 记录该url已被访问过
        visitedUrls.add(directoryUrl);
    }

    /**
     * 根据GAV信息带依赖的爬取组件
     * @param groupId 组织id
     * @param artifactId 工件ig
     * @param version 版本号
     * @return componentInformationDO
     */
    private ComponentInformationDO crawlWithDependencyByGAV(String groupId, String artifactId, String version){
        String url = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        return crawlWithDependency(url);
    }

    /**
     * 带依赖的爬取url下的组件
     *  会获取该组件的组件信息、依赖树、平铺依赖表
     *  该组件的所有依赖（依赖树中出现的）仅爬取组件信息本身
     * @param url 组件的url，该url目录下应该包含pom文件、（可选）jar包
     * @return componentInformationDO
     */
    private ComponentInformationDO crawlWithDependency(String url){

        // 爬取pom文件中的组件信息
        String pomUrl = findPomUrlInDirectory(url);
        if (!pomUrl.endsWith(".pom")) {
            return null;
        }

        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        ComponentDO componentDO = ConvertUtil.convertToComponentDO(document, pomUrl, MAVEN_REPO_BASE_URL);
        if (componentDO == null)
            return null;

        // 爬取jar包，生成hash信息
        String jarUrl = findJarUrlInDirectory(url);
        if (jarUrl != null){
            Document jarDocument = UrlConnector.getDocumentByUrl(jarUrl);
//             List<HashDO> hashes = HashUtil.getHashes(document.outerHtml());
//            componentDO.setHashes(hashes);
        }

        // 生成组件依赖树
        // todo 先查数据库检查是否已存在依赖树

        // 生成一个临时pom文件
        //createPomFile(document.outerHtml());
        // 调用mvn dependency:tree命令获取依赖
        Node node = MavenUtil.mavenDependencyTreeAnalyzer(POM_FILE_TEMP_PATH);
        // 解析依赖，获得组件树
        MavenUtil mavenUtil = new MavenUtil();
        if (node == null)
            return null;
        ComponentDependencyTreeDO componentDependencyTreeDO = mavenUtil.convertNode(node, 0);

        // 封装依赖树
        DependencyTreeDO dependencyTreeDO = new DependencyTreeDO();
        dependencyTreeDO.setTree(componentDependencyTreeDO);

        dependencyTreeDO.setId(UUIDGenerator.getUUID());
        dependencyTreeDO.setGroupId(componentDO.getGroupId());
        dependencyTreeDO.setArtifactId(componentDO.getArtifactId());
        dependencyTreeDO.setVersion(componentDO.getVersion());

        // 生成依赖表
        List<DependencyTableDO> dependencyTableDOList = MavenUtil.buildDependencyTable(dependencyTreeDO);

        // 封装
        ComponentInformationDO componentInformationDO = new ComponentInformationDO();
        componentInformationDO.setComponentDO(componentDO);
        componentInformationDO.setDependencyTreeDO(dependencyTreeDO);
        componentInformationDO.setDependencyTableDO(dependencyTableDOList);

        return componentInformationDO;
    }


    /**
     * 爬取url下的组件，不爬取其依赖
     *  只会获取该组件自身的组件信息
     * @param url 组件的url，该url目录下应该包含pom文件、（可选）jar包
     * @return
     */
    private ComponentDO crawl(String url){
        String pomUrl = findPomUrlInDirectory(url);

        if (!pomUrl.endsWith(".pom")) {
            return null;
        }

        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        ComponentDO componentDO = ConvertUtil.convertToComponentDO(document, pomUrl, MAVEN_REPO_BASE_URL);
        if (componentDO == null)
            return null;

        String jarUrl = findJarUrlInDirectory(url);
        if (jarUrl != null){
            Document jarDocument = UrlConnector.getDocumentByUrl(jarUrl);
//            List<HashDO> hashes = HashUtil.getHashes(jarDocument.outerHtml());
//            componentDO.setHashes(hashes);
        }

        return componentDO;
    }

    /**
     * 在指定目录url下寻找pom文件，并返回其url
     * @param directoryUrl 目录url
     * @return pomUrl
     */
    private String findPomUrlInDirectory(String directoryUrl){
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
     * 在指定目录下寻找jar包，并返回其url
     * @param directoryUrl 目录url
     * @return jarUrl
     */
    private String findJarUrlInDirectory(String directoryUrl){
        Document document = UrlConnector.getDocumentByUrl(directoryUrl);

        if (document == null) {
            return null;
        }

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.jar结尾的文件
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            // todo 怎么精确找到jar包
            if (fileAbsUrl.endsWith(".jar") && !fileAbsUrl.contains("-javadoc")) {
                return fileAbsUrl;
            }
        }
        System.err.println("No .pom file found in \"" + directoryUrl + "\"");
        return null;
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
            System.out.println("visited links saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 记录爬取或解析依赖树失败的url
     *
     * @param failedUrl 失败的url
     */
    private void writeFailedUrl(String failedUrl) {
        System.err.println("爬取或解析pom或生成依赖树失败：" + failedUrl);
        try (OutputStream outputStream = new FileOutputStream(FAILED_URLS_PATH);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream))) {
            writer.write(failedUrl);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 在程序终止时，保存数据
     */
    private void saveAndFlush() {
        //todo

        saveVisitedLinks();
    }


}
