package nju.edu.cn.qysca.utils.parser;

import java.util.List;

/**
 *  用以记录pom
 */
public class JavaComponentNode {
    public static final String[] headers = {"GroupId", "ArtifactId", "Version", "PomUrl", "Name"};
    private final String id;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String openSource;
    private final List<String> licenseNames;
    private final List<String> licenseUrls;
    private final String name;
    private final String author;
    private final String description;
    private final String url;

    public JavaComponentNode(String id, String groupId, String artifactId, String version, String openSource, List<String> licenseNames, List<String> licenseUrls, String name, String author, String description, String url) {
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.openSource = openSource==null?"-":openSource;
        this.licenseNames = licenseNames;
        this.licenseUrls = licenseUrls;
        this.name = name==null?"-":name;
        this.author = author==null?"-":author;
        this.description = description==null?"-":description;
        this.url = url==null?"-":url;
    }

    public String toCsvString(){
        // todo 有些字段前后要加引号
        return id + "," + groupId + "," + artifactId + "," + version + "," + openSource + ","
                + String.join(";", licenseNames) + "," + String.join(";", licenseUrls)
                + "," + name + "," + author + "," + description + "," + url;
    }
}
