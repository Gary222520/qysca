package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.DeveloperDO;
import nju.edu.cn.qysca.domain.component.dos.JavaOpenComponentDO;
import nju.edu.cn.qysca.domain.component.dos.LicenseDO;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpiderServiceImpl implements SpiderService {

    private final static String MAVEN_REPO_BASE_URL = "https://repo1.maven.org/maven2/";

    @Override
    public JavaOpenComponentDO crawlByGav(String groupId, String artifactId, String version) {
        String downloadUrl = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        String pomUrl = findPomFileUrlInDirectory(downloadUrl);
        if (pomUrl == null) {
            return null;
        }
        JavaOpenComponentDO javaOpenComponentDO = crawl(pomUrl);
        return javaOpenComponentDO;
    }

    /**
     * 在指定目录url下查找.pom文件
     *
     * @param directoryUrl 目录url
     * @return 如果存在.pom文件则返回其url，否则返回null
     */
    private String findPomFileUrlInDirectory(String directoryUrl) {

        Document document = getDocumentByUrl(directoryUrl);

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

    public Document getDocumentByUrl(String url) {
        try {
            // 每次爬取url时休眠一定时间，防止被ban
            //Thread.sleep(sleepTime);
            // 连接到url并获取其内容
            return Jsoup.connect(url).get();
        } catch (Exception e) {
            System.err.println("can't visit or it is invalid: " + url);
            return null;
        }
    }

    /**
     * 根据pom文件的url爬取单个pom文件，并转换为DO
     *
     * @param pomUrl pom url
     */
    private JavaOpenComponentDO crawl(String pomUrl) {
        if (!pomUrl.endsWith(".pom")) {
            return null;
        }

        System.out.println("Crawling " + pomUrl);

        Document document = getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        JavaOpenComponentDO javaOpenComponentDO = convertToJavaOpenComponentDO(document, pomUrl, MAVEN_REPO_BASE_URL);

        return javaOpenComponentDO;
    }

    public JavaOpenComponentDO convertToJavaOpenComponentDO(Document document, String pomUrl, String MAVEN_REPO_BASE_URL) {
        // 从pom url中提取groupId, artifactId, and version
        // https://repo1.maven.org/maven2/org/springframework/boot/aot/org.springframework.boot.aot.gradle.plugin/3.0.9/org.springframework.boot.aot.gradle.plugin-3.0.9.pom
        String[] parts = pomUrl.split("/");
        String version = parts[parts.length - 2];
        String artifactId = parts[parts.length - 3];
        String groupId = String.join(".", Arrays.copyOfRange(parts, MAVEN_REPO_BASE_URL.split("/").length, parts.length - 3));

        Model model = convertToModel(document);
        if (model == null) {
            return null;
        }

        JavaOpenComponentDO javaOpenComponentDO = new JavaOpenComponentDO();
        javaOpenComponentDO.setGroupId(groupId);
        javaOpenComponentDO.setArtifactId(artifactId);
        javaOpenComponentDO.setVersion(version);


        javaOpenComponentDO.setName(model.getName());
        javaOpenComponentDO.setDescription(model.getDescription());

        javaOpenComponentDO.setUrl(model.getUrl());
        javaOpenComponentDO.setDownloadUrl(getDownloadUrl(pomUrl));
        javaOpenComponentDO.setSourceUrl(model.getScm() == null ? null : model.getScm().getUrl());

        javaOpenComponentDO.setDevelopers(getDevelopers(model));
        javaOpenComponentDO.setLicenses(getLicense(model));
        javaOpenComponentDO.setPom(convertToJson(document).toString());

        return javaOpenComponentDO;

    }

    private String getDownloadUrl(String pomUrl) {
        return pomUrl.substring(0, pomUrl.lastIndexOf('/') + 1);
    }

    private List<DeveloperDO> getDevelopers(Model model) {
        List<org.apache.maven.model.Developer> mavenDevelopers = model.getDevelopers();
        return mavenDevelopers.stream()
                .map(mavenDeveloper -> {
                    DeveloperDO developer = new DeveloperDO();
                    developer.setDeveloperId(mavenDeveloper.getId());
                    developer.setDeveloperName(mavenDeveloper.getName());
                    developer.setDeveloperEmail(mavenDeveloper.getEmail());
                    return developer;
                })
                .collect(Collectors.toList());
    }

    private List<LicenseDO> getLicense(Model model) {
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        return mavenLicenses.stream()
                .map(mavenLicense -> {
                    LicenseDO license = new LicenseDO();
                    license.setLicenseName(mavenLicense.getName());
                    license.setLicenseUrl(mavenLicense.getUrl());
                    return license;
                })
                .collect(Collectors.toList());
    }

    private Model convertToModel(Document document) {
        String pomString = document.outerHtml();
        Model model = null;
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(new StringReader(pomString));
            return model;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject convertToJson(Document document) {
        return XML.toJSONObject(document.outerHtml());
    }
}