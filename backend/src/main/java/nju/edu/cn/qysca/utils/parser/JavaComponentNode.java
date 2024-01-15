package nju.edu.cn.qysca.utils.parser;

import java.util.List;

/**
 *  用以记录pom
 */
public class JavaComponentNode {
    public static final String HEADERS = "id,groupId,artifactId,version,openSource,licenseNames,licenseUrls,name,developers,description,url,pomUrl,,,,,,,,";
    private final String id;
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String openSource;
    private final String licenseNames;
    private final String licenseUrls;
    private final String name;
    private final String developers;
    private final String description;
    private final String url;
    private final String pomUrl;

    public JavaComponentNode(String id, String groupId, String artifactId, String version, String openSource, List<String> licenseNames, List<String> licenseUrls, String name, List<String> developers, String description, String url, String pomUrl) {
        // 空的"-"代替
        this.id = id;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.openSource = openSource==null||openSource.equals("")?"-":openSource;
        this.licenseNames = licenseNames==null||licenseNames.isEmpty()?"-":String.join(";", licenseNames).replaceAll("\"", "\\\\\"");
        this.licenseUrls = licenseUrls==null||licenseUrls.isEmpty()?"-":String.join(";", licenseUrls).replaceAll("\"", "\\\\\"");
        this.name = name==null||name.equals("")?"-":name.replaceAll("\"", "\\\\\"");
        this.developers = developers==null|| developers.isEmpty()?"-":String.join(";", developers).replaceAll("\"", "\\\\\"");
        this.description = description==null||description.equals("")?"-":description.replaceAll("\n","").replaceAll("\r","").replaceAll("\"", "\\\\\"");
        this.url = url==null||url.equals("")?"-":url;
        this.pomUrl = pomUrl==null||pomUrl.equals("")?"-":pomUrl;
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
                + "\"" + developers + "\"" + ","
                + "\"" + description + "\"" + ","
                + "\"" + url + "\"" + ","
                + "\"" + pomUrl + "\"" + ",,,,,,,,";
    }
}
