package nju.edu.cn.qysca.utils.parser;

import io.swagger.models.auth.In;
import nju.edu.cn.qysca.utils.CsvWriter;
import nju.edu.cn.qysca.utils.spider.PomSpider;

import nju.edu.cn.qysca.utils.spider.UrlConnector;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PomParser {

    /**
     * maven仓库根地址
     */
    private static final String MAVEN_REPO_ROOT_URL = "https://repo1.maven.org/maven2/";

    /**
     * csv pom信息地址
     */
    private static final String CSV_POM_INFO_PATH = "backend/src/main/resources/csv/pomInfo.csv";
    /**
     * csv pom依赖关系地址
     */
    private static final String CSV_POM_DEPENDENCY_PATH = "backend/src/main/resources/csv/pomDependency.csv";
    /**
     * csv pom父子关系地址
     */
    private static final String CSV_POM_PARENT_PATH = "backend/src/main/resources/csv/pomParent.csv";
    /**
     * pom文件保存目录
     */
    private static final String POM_FILE_SAVE_PATH = "backend/src/main/resources/pomFiles";

    /**
     * （解析过的）pom记录文件地址
     */
    private static final String RECORD_FILE_PATH = "backend/src/main/resources/pom_record_list.txt";
    /**
     * 用以记录所有被解析过的pom文件（以GAV三维坐标为key），来源于pom记录文件中
     * 未来可能改为从数据库查
     */
    private Set<String> pomRecords;
    /**
     * 用以记录在一次parsePom中新发现的pom文件，方便重新写入pom记录文件中
     * 未来可能使用数据库后，就不需要了
     */
    private Set<String> newPomRecords;
    /**
     * 记录在一次pom解析过程中新发现的pom数量
     */
    private int new_pom_count;

    /**
     *  解析pom文件，递归解析所有依赖，被解析过的pom会写入list文件，避免重复解析
     *  还会将pom节点与依赖关系写入csv
     *  还会下载该份pom
     * @param pomUrl
     */
    public void parsePom(String pomUrl){

        System.out.println("开始解析pom: "+ pomUrl);
        new_pom_count = 0;
        // 在每次解析前，获取所有解析过的pom
        pomRecords = readPomRecords();
        newPomRecords = new HashSet<>();
        parsePomModel(PomSpider.getPomModel(pomUrl), pomUrl);
        // 解析完成后，将新发现的pom记录入文件
        writePomRecords(newPomRecords);
        System.out.println("解析结束，新发现的pom数量为: "+ new_pom_count);
        System.out.println();
    }

    /**
     * 从pom记录文件中读取所有被解析过的pom文件
     * @return pomRecords
     */
    private Set<String> readPomRecords() {
        Set<String> pomRecords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(RECORD_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                pomRecords.add(line);
            }
            System.out.println("Read Pom Records From " + RECORD_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return pomRecords;
    }

    /**
     * 向pom记录文件中写入新的pom记录
     * @param newPomRecords 新的pom记录
     */
    private void writePomRecords(Set<String> newPomRecords) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(RECORD_FILE_PATH, true))) {
            // 将新的集合写回到文件
            for (String pomKey : newPomRecords) {
                writer.write(pomKey);
                writer.newLine();
            }
            System.out.println("Write New Pom Records Back Into " + RECORD_FILE_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析PomModel，递归查找并解析所有依赖
     * 还会将pom节点与依赖关系写入csv
     * 还会下载该份pom
     *
     * @param model pom-model
     */
    public void parsePomModel(Model model, String pomUrl) {
        if (model == null) {
            System.err.println("pom-model is null");
            return;
        }

        // 首先将model的一些基本信息填充完整
        // groupId和version如果为空，则默认为parent的groupId和version
        if (model.getGroupId() == null)
            model.setGroupId(model.getParent().getGroupId());
        if (model.getVersion() == null)
            model.setVersion(model.getParent().getVersion());

        // 获取其基本信息
        String groupId = model.getGroupId();
        String artifactId = model.getArtifactId();
        String version = model.getVersion();
        String name = model.getName();

        // 检查pom是否被记录过，如果被记录过就跳过，否则记录
        if (pomRecords.contains(groupId + ":" + artifactId + ":" + version)){
            return;
        }
        pomRecords.add(groupId + ":" + artifactId + ":" + version);
        newPomRecords.add(groupId + ":" + artifactId + ":" + version);

        System.out.println("New Pom Url: " + pomUrl);
        new_pom_count++;

        // 下载pom
        UrlConnector.downLoadFromUrl(pomUrl, groupId + "." + artifactId + "-" + version + ".pom", POM_FILE_SAVE_PATH);

        // 创建pomNode，写入csv
        PomNode pomNode = new PomNode(groupId, artifactId, version, name, pomUrl);
        CsvWriter csvWriter = new CsvWriter(CSV_POM_INFO_PATH, PomNode.headers);
        csvWriter.writePomNode(pomNode);

        // 首先需要查找<parent>项目中的所有依赖，并不断递归查找<parent>
        if (model.getParent() != null) {

            String parentPomUrl = getParentPomUrl(model);
            // 将子父关系写入csv
            PomNode parentPomNode = new PomNode(model.getParent().getGroupId(), model.getParent().getArtifactId(), model.getParent().getVersion(), "", parentPomUrl);
            PomParentNode pomParentNode = new PomParentNode(pomNode, parentPomNode);
            CsvWriter csvWriter1 = new CsvWriter(CSV_POM_PARENT_PATH, PomNode.headers);
            csvWriter1.writePomParentNode(pomParentNode);

            // 解析parent的pom
            parsePomModel(PomSpider.getPomModel(parentPomUrl), parentPomUrl);
        }

        // 获取其所有依赖
        List<Dependency> dependencies = model.getDependencies();
        for (Dependency dependency : dependencies) {

            //先将model的dependency的信息填充完整
            dependency.setGroupId(getDependencyGroupId(model, dependency));
            dependency.setVersion(getDependencyVersion(model, dependency));

            // 获取依赖的groupId、artifactId、version
            String dependencyGroupId = dependency.getGroupId();
            String dependencyArtifactId = dependency.getArtifactId();
            String dependencyVersion = dependency.getVersion();
            // 获取依赖的scope，默认为compile
            String dependencyScope = dependency.getScope();
            if (dependencyScope == null)
                dependencyScope = "compile";

            // 获取依赖的pom文件url，
            String dependencyUrl = buildUrl(dependencyGroupId, dependencyArtifactId, dependencyVersion);
            String dependencyPomUrl = PomSpider.findPomFileUrlInDirectory(dependencyUrl);

            // 将依赖关系写入csv
            // 只会写直接依赖关系 (不包括继承parent的依赖）
            PomNode dependencyPomNode = new PomNode(dependencyGroupId, dependencyArtifactId, dependencyVersion, "", dependencyPomUrl);
            PomDependencyNode pomDependencyNode = new PomDependencyNode(pomNode, dependencyPomNode, dependencyScope);
            CsvWriter csvWriter2 = new CsvWriter(CSV_POM_DEPENDENCY_PATH, PomDependencyNode.headers);
            csvWriter2.writePomDependencyNode(pomDependencyNode);

            // 继续解析依赖的pom
            parsePomModel(PomSpider.getPomModel(dependencyPomUrl), dependencyPomUrl);
        }

    }

    /**
     * 给定model，获取其parent的pom的url
     *
     * @param model pom model
     * @return parent的pom的url
     */
    private static String getParentPomUrl(Model model) {
        Parent parent = model.getParent();
        if (parent == null) {
            return null;
        }
        String parentUrl = buildUrl(parent.getGroupId(), parent.getArtifactId(), parent.getVersion());
        return PomSpider.findPomFileUrlInDirectory(parentUrl);
    }

    /**
     * 获取某一依赖的groupId
     */
    private static String getDependencyGroupId(Model model, Dependency dependency) {
        String dependencyGroupId = dependency.getGroupId();
        if (dependencyGroupId.startsWith("${") && dependencyGroupId.endsWith("}")){
            // 为${xxx}形式
            if (dependencyGroupId.equals("${project.groupId}") || dependencyGroupId.equals("${pom.groupId}"))
                // 如果写的是${project.groupId}，则直接返回pom中的groupId
                dependencyGroupId = model.getGroupId();
            else {
                // 否则则在<properties>下寻找<xxx>
                String propertyName = dependencyGroupId.substring(2, dependencyGroupId.length() - 1);
                dependencyGroupId = getValueFromProperties(model, propertyName);
            }
        }
        return dependencyGroupId;
    }

    /**
     * 获取某一依赖的版本
     *
     * @param model      pom model
     * @param dependency 要查找的依赖
     * @return 依赖的版本
     */
    private static String getDependencyVersion(Model model, Dependency dependency) {

        // 首先将model的一些基本信息填充完整
        // groupId和version如果为空，则默认为parent的groupId和version
        if (model.getGroupId() == null)
            model.setGroupId(model.getParent().getGroupId());
        if (model.getVersion() == null)
            model.setVersion(model.getParent().getVersion());

        // 关于版本号的一般流程：
        // 1. 未写版本号，则需要在<dependencyManagement>寻找，未找到则继续在parent的<dependencyManagement>找
        // 2. 写${xxx}形式，如<version>${spring.version}</version>,则在<properties>下寻找<spring.version>
        //  2.1 如果写的是${project.version}，则直接返回pom中的version
        //  2.2 否则如<version>${spring.version}</version>,则在<properties>下寻找<spring.version>
        // 3. 直接写明版本号
        String version = null;
        if (dependency.getVersion() == null) {
            //未写明version，则需要在<dependencyManagement>寻找
            version = getDependencyVersionFromDependencyManagement(model, dependency);

        } else if (dependency.getVersion().startsWith("${") && dependency.getVersion().endsWith("}")) {
            // ${xxx}形式
            if (dependency.getVersion().equals("${project.version}")) {
                // 如果写的是${project.version}，则直接返回pom中的version
                version = model.getVersion();
            } else {
                //否则如${spring.version},则在<properties>下寻找<spring.version>
                String propertyName = dependency.getVersion().substring(2, dependency.getVersion().length() - 1);
                version = getValueFromProperties(model,propertyName);
            }
        } else {
            // 直接写明了版本号
            version = dependency.getVersion();
        }
        return version;
    }

    /**
     * 从<properties>中寻找值，在model中找不到则会从parent去找
     *
     * @param model pom model
     * @param propertyName 要查找的propertyName
     * @return value
     */
    private static String getValueFromProperties(Model model, String propertyName){
        String value = model.getProperties().getProperty(propertyName);
        if (value == null){
            String parentPomUrl = getParentPomUrl(model);
            Model parentModel = PomSpider.getPomModel(parentPomUrl);
            return getValueFromProperties(parentModel, propertyName);
        }
        return value;
    }

    /**
     * 在<dependencyManagement>中寻找某一依赖的version（当依赖未标明版本时）
     *
     * @param model      pom model
     * @param dependency 要查找的依赖
     * @return 依赖的版本
     */
    private static String getDependencyVersionFromDependencyManagement(Model model, Dependency dependency) {
        String version = null;
        // 先看是否有<dependencyManagement>
        if (model.getDependencyManagement() != null && model.getDependencyManagement().getDependencies() != null) {
            List<Dependency> dependenciesInDm = model.getDependencyManagement().getDependencies();
            for (Dependency dependencyInDM : dependenciesInDm) {
                // 先将model的dependencyManagement中的dependency的信息填充完整
                dependencyInDM.setGroupId(getDependencyGroupId(model, dependencyInDM));
                dependencyInDM.setVersion(getDependencyVersion(model, dependencyInDM));

                // 在dependencyManagement中寻找对应的依赖，
                if (dependencyInDM.getGroupId().equals(dependency.getGroupId()) && dependencyInDM.getArtifactId().equals(dependency.getArtifactId())) {
                    version = dependencyInDM.getVersion();
                    return version;
                }
            }
        }
        // 没找到，则继续到parent的dependencyManagement中找
        if (model.getParent() != null) {
            String parentPomUrl = getParentPomUrl(model);
            Model parentModel = PomSpider.getPomModel(parentPomUrl);
            return getDependencyVersionFromDependencyManagement(parentModel, dependency);
        }
        // 没有parent，version查不到，报错
        System.err.println("can't find version of Dependency " + dependency.getGroupId() + "/" + dependency.getArtifactId() + " in PomFile: " + buildUrl(model.getGroupId(), model.getArtifactId(), model.getVersion()));

        return null;
    }

    /**
     * 通过groupId,artifactId,version(即三维坐标）构建目录url
     *
     * @param groupId    组id
     * @param artifactId 工件id
     * @param version    版本
     * @return url 该组件该版本的目录地址
     */
    private static String buildUrl(String groupId, String artifactId, String version) {
        // 拼接出url
        // 例如：https://repo1.maven.org/maven2/org/apache/commons/commons-lang/1.3.1/

        return MAVEN_REPO_ROOT_URL + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/";
    }
}
