package nju.edu.cn.qysca.utils.parser;

import java.util.List;

/**
 *  用以记录pom
 */
public class JavaComponentNode {
    public static final String[] HEADERS = {"id","groupId","artifactId","version","openSource","licenseNames","licenseUrls","name","author","description","url"};
    private final String id;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String openSource;
    private final String licenseNames;
    private final String licenseUrls;
    private final String name;
    private final String author;
    private final String description;
    private final String url;

    public JavaComponentNode(String id, String groupId, String artifactId, String version, String openSource, List<String> licenseNames, List<String> licenseUrls, String name, String author, String description, String url) {
        // 空的"-"代替
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.openSource = openSource==null||openSource.equals("")?"-":openSource;
        this.licenseNames = licenseNames==null||licenseNames.isEmpty()?"-":String.join(";", licenseNames);
        this.licenseUrls = licenseUrls==null||licenseUrls.isEmpty()?"-":String.join(";", licenseUrls);
        this.name = name==null||name.equals("")?"-":name;
        this.author = author==null|| author.equals("") ?"-":author;
        this.description = description==null||description.equals("")?"-":description.replaceAll("\n","").replaceAll("\r","");
        this.url = url==null||url.equals("")?"-":url;
    }

    public String toCsvString(){
        // 有些字段前后要加引号
        return id + ","
                + "\"" + groupId + "\"" + ","
                + "\"" + artifactId + "\"" + ","
                + "\"" + version + "\"" + ","
                +  openSource + ","
                + "\"" + licenseNames + "\"" +  ","
                + "\"" + licenseUrls + "\"" + ","
                + "\"" + name + "\"" + ","
                + "\"" + author + "\"" + ","
                + "\"" + description + "\"" + ","
                + "\"" + url + "\"";
    }
}
