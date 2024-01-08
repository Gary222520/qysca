package nju.edu.cn.qysca.utils.parser;

import nju.edu.cn.qysca.utils.spider.PomSpider;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class PomParser {

    /**
     * maven仓库根地址
     */
    private static final String MAVEN_REPO_ROOT_URL = "https://repo1.maven.org/maven2/";

    /**
     * 解析一个pom文件，记录其基本信息，并递归地记录依赖
     * @param document org.jsoup.nodes.Document, pom文件
     * @param pomUrl pom文件的url地址
     * @return  PomFileInfo
     */
    public static PomFileInfo parsePomDocument(Document document, String pomUrl) {
        // 获取其基本信息
        String groupId = getElementValue(document, "project > groupId");
        String artifactId = getElementValue(document, "project > artifactId");
        String version = getElementValue(document, "project > version");

        PomFileInfo pomFileInfo = new PomFileInfo(groupId, artifactId, version, pomUrl);

        // 发现其依赖并添加
        pomFileInfo.setDependencies(parseDependencies(document));

        return pomFileInfo;
    }

    /**
     * 递归地获取依赖信息
     * @param document org.jsoup.nodes.Document, pom文件
     * @return 该pom的依赖
     */
    private static List<PomFileInfo> parseDependencies(Document document) {
        List<PomFileInfo> dependencies = new ArrayList<>();

        // 查找标签<project>下<dependencies>下的<dependency>标签中的元素，即所有直接依赖
        Elements dependencyElements = document.select("project > dependencies > dependency");

        //遍历这些依赖，添加到依赖列表中
        for (Element dependencyElement : dependencyElements) {
            //todo 一些pom文件中dependency不会写版本号，一些版本号则用${xxx}形式，需要解决

            // 获取该依赖的基本信息
            String groupId = getElementValue(dependencyElement, "groupId");
            String artifactId = getElementValue(dependencyElement, "artifactId");
            String version = getElementValue(dependencyElement, "version");

            // 构建该依赖的pom文件URL
            String dependencyUrl = buildUrl(groupId, artifactId, version);
            String dependencyPomFileUrl = PomSpider.findPomFileUrlInDirectory(dependencyUrl);

            // 解析该依赖的pom文件
            // 这里需要递归调用，因为一个依赖的pom文件中可能还依赖新的子依赖
            PomFileInfo dependencyFileInfo = parsePomDocument(PomSpider.getPomAsDocument(dependencyPomFileUrl), dependencyPomFileUrl);

            dependencies.add(dependencyFileInfo);
        }

        return dependencies;
    }

    /**
     *  通过groupId,artifactId,version构建目录url
     * @param groupId
     * @param artifactId
     * @param version
     * @return url 该组件该版本的目录地址
     */
    private static String buildUrl(String groupId, String artifactId, String version) {
        // 拼接出url
        // 例如：https://repo1.maven.org/maven2/org/apache/commons/commons-lang/1.3.1/

        return MAVEN_REPO_ROOT_URL + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/";
    }

    private static String getElementValue(Document document, String selector) {
        Element element = document.selectFirst(selector);
        return (element != null) ? element.text() : null;
    }

    private static String getElementValue(Element document, String selector) {
        Element element = document.selectFirst(selector);
        return (element != null) ? element.text() : null;
    }


}
