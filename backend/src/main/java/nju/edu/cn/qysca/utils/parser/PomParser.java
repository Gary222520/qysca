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
        String groupId = getGroupId(document);
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
        /**
         *  首先需要查找<parent>项目中的所有依赖，并不断递归查找<parent>
         *  其次在找<dependencies>下的依赖
         */
        List<PomFileInfo> dependencies = new ArrayList<>();

        // 查找<project>下的<parent>，获得其url
        String parentPomUrl = getParentPomUrl(document);
        // 如果存在parent，寻找parent的依赖并添加
        if (parentPomUrl != null) {
            // 找出parent的依赖，添加进自己的依赖
            PomFileInfo parentPomFileInfo = parsePomDocument(PomSpider.getPomAsDocument(parentPomUrl), parentPomUrl);
            dependencies.addAll(parentPomFileInfo.getDependencies());
        }


        // 查找标签<project>下<dependencies>下的<dependency>标签中的元素，即所有直接依赖
        Elements dependencyElements = document.select("project > dependencies > dependency");
        //遍历这些依赖，添加到依赖列表中
        for (Element dependencyElement : dependencyElements) {
            // 获取该依赖的基本信息
            String groupId = getElementValue(dependencyElement, "groupId");
            String artifactId = getElementValue(dependencyElement, "artifactId");
            String version = getDependencyVersion(document, dependencyElement);

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


    //
     private static String getGroupId(Document pomDocument){
         String groupId = getElementValue(pomDocument, "project > groupId");
         if (groupId == null){
             groupId = getElementValue(pomDocument, "project > parent > groupId");
         }
         return groupId;
     }

    /**
     * 查找某个依赖的版本号
     * @param pomDocument pom的Document
     * @param dependencyElement 依赖的Element（是pomDocument的一部分）
     * @return version 依赖的版本号
     */
    private static String getDependencyVersion(Document pomDocument, Element dependencyElement){

        // 关于版本号的一般流程：
        // 1. 未写版本号，则需要在<dependencyManagement>寻找，未找到则继续在parent的<dependencyManagement>找
        // 2. 写${xxx}形式，如<version>${spring.version}</version>,则在<properties>下寻找<spring.version>
        //  2.1 如果写的是${project.version}，则直接返回pom中的version
        //  2.2 否则如<version>${spring.version}</version>,则在<properties>下寻找<spring.version>
        // 3. 直接写明版本号

        String version = getElementValue(dependencyElement, "version");
        if (version == null){

            // 未写版本号，需要在dependencyManagement中寻找
            version = getDependencyVersionFromDependencyManagement(pomDocument, dependencyElement);
            return version;

        } else if (version.startsWith("${") && version.endsWith("}")) {

            // 写${xxx}形式，需要在<properties>下寻找相应的属性值
            String propertyName = version.substring(2, version.length() - 1);

            if (propertyName.equals("project.version")){
                //如果写的是${project.version}，则直接返回pom中的version
                version = getElementValue(pomDocument, "project > version");
            } else {
                // todo 如果propertyName含特殊符号，如${slf4j.version},竟然就找不到
                version = getElementValue(pomDocument, "project > properties > " + propertyName);
            }
            return version;

        } else {
            //直接标明了版本号，则直接返回
            return version;
        }
    }


    /**
     * 从dependencyManagement中寻找某个依赖的版本号，如果找不到，则会继续从parent的pom中的dependencyManagement中寻找
     * @param pomDocument pom的Document
     * @param dependencyElement 依赖的Element
     * @return version 依赖的版本号
     */
    private static String getDependencyVersionFromDependencyManagement(Document pomDocument, Element dependencyElement){
        String groupId = getElementValue(dependencyElement, "groupId");
        String artifactId = getElementValue(dependencyElement, "artifactId");

        // 再从中取出<dependencyManagement>标签的Element
        Element dependencyManagementElement = pomDocument.selectFirst("project > dependencyManagement");

        if (dependencyManagementElement != null) {
            // 如果存在<dependencyManagement>标签，则在其下面尝试寻找groupId和artifactId一致的<dependency>标签
            Elements dependencies = dependencyManagementElement.select("dependencies > dependency");
            for (Element dependency: dependencies) {
                String dependencyGroupId = getElementValue(dependency, "groupId");
                String dependencyArtifactId = getElementValue(dependency, "artifactId");
                // 寻找寻找groupId和artifactId一致的<dependency>标签
                if (groupId.equals(dependencyGroupId) && artifactId.equals(dependencyArtifactId)) {
                    String version = getElementValue(dependency, "version");
                    // <dependencyManagement>标签下的version还不存在，则报错
                    if (version == null) {
                        System.err.println("Err: version 在dependencyManagement中为空： ");
                        System.err.println(pomDocument);
                        return null;
                    }
                    // 写${xxx}形式，需要在<properties>下寻找相应的属性值
                    if (version.startsWith("${") && version.endsWith("}")){

                        String propertyName = version.substring(2, version.length() - 1);
                        if (propertyName.equals("project.version")){
                            //如果写的是project.version形式，则直接返回pom中的version
                            version = getElementValue(pomDocument, "project > version");
                        } else {
                            version = getElementValue(pomDocument, "project > properties > " + propertyName);
                        }
                    }
                    return version;
                }
            }
        }
        // 没有找到，递归调用，继续查找parent的dependencyManagement，直到找到为止或抛出异常
        String parentPomUrl = getParentPomUrl(pomDocument);
        Document parentDocument = PomSpider.getPomAsDocument(parentPomUrl);
        return getDependencyVersionFromDependencyManagement(parentDocument, dependencyElement);
    }

    /**
     * 从一个pom的document中找到其parent，并返回其pom的url
     * @param document pom的doucment
     * @return parent的pom的url
     */
    public static String getParentPomUrl(Document document){
        Element parentElement = document.selectFirst("project > parent");

        if (parentElement == null)
            return null;

        // 获取parent的groupId、artifactId、version
        String groupId = getElementValue(parentElement, "groupId");
        String artifactId = getElementValue(parentElement, "artifactId");
        String version = getElementValue(parentElement, "version");

        if (groupId == null || artifactId == null || version == null){
            System.err.println("Err: parent的groupId、artifactId、version不能为空, pom文件url: " + buildUrl(getElementValue(document, "groupId"), getElementValue(document, "artifactId"), getElementValue(document, "version")));
            return null;
        }
        // 构建parent的pom文件的url
        String parentUrl = buildUrl(groupId, artifactId, version);
        String parentPomUrl = PomSpider.findPomFileUrlInDirectory(parentUrl);

        return parentPomUrl;
    }


    /**
     *  通过groupId,artifactId,version(即三维坐标）构建目录url
     * @param groupId 组id
     * @param artifactId 工件id
     * @param version 版本
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
