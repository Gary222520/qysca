package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.dao.component.DependencyTableDao;
import nju.edu.cn.qysca.dao.component.DependencyTreeDao;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dos.DeveloperDO;
import nju.edu.cn.qysca.domain.component.dos.LicenseDO;
import nju.edu.cn.qysca.utils.HashUtil;
import nju.edu.cn.qysca.utils.spider.UrlConnector;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SpiderServiceImpl implements SpiderService {


    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private DependencyTreeDao dependencyTreeDao;

    @Autowired
    private DependencyTableDao dependencyTableDao;

    private final static String MAVEN_REPO_BASE_URL = "https://repo1.maven.org/maven2/";

    /**
     * 通过gav爬取组件
     * @param groupId 组织id
     * @param artifactId 工件id
     * @param version 版本
     * @return ComponentDO
     */
    public ComponentDO crawlByGav(String groupId, String artifactId, String version) {
        String url = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        return crawl(url);
    }


    /**
     * 爬取url下的组件，不爬取其依赖
     * 只会获取该组件自身的组件信息
     *
     * @param url 组件的url，该url目录下应该包含pom文件、（可选）jar包
     * @return ComponentDO
     */
    private ComponentDO crawl(String url) {
        // 爬取pom文件中的组件信息
        String pomUrl = findPomUrlInDirectory(url);
        if (pomUrl == null) {
            return null;
        }

        Document document = getDocumentByUrl(pomUrl);
        if (document == null)
            return null;

        ComponentDO componentDO = convertToComponentDO(document, pomUrl, MAVEN_REPO_BASE_URL);
        if (componentDO == null)
            return null;

        ComponentDO searchResult = componentDao.findByGroupIdAndArtifactIdAndVersion(componentDO.getGroupId(), componentDO.getArtifactId(), componentDO.getVersion());
        if (searchResult != null) {
            // 表示该组件的组件信息已被爬取过
            return searchResult;
        }

        // 爬取jar包，生成hash信息
        String jarUrl = findJarUrlInDirectory(url);
        if (jarUrl != null) {
            componentDO.setHashes(HashUtil.getHashes(jarUrl));
        }

        return componentDO;
    }

    /**
     * 给定一个url地址，爬取其内容至一个document
     *
     * @param url url地址
     * @return Document org.jsoup.nodes.Document
     */
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
     * 在指定目录url下寻找pom文件，并返回其url
     *
     * @param directoryUrl 目录url
     * @return pomUrl
     */
    private String findPomUrlInDirectory(String directoryUrl) {
        Document document = getDocumentByUrl(directoryUrl);

        if (document == null) {
            return null;
        }

        // 从目录中提取出工件号和版本号
        String[] parts = directoryUrl.split("/");
        String artifactId = parts[parts.length - 2];
        String version = parts[parts.length - 1];

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.pom结尾的文件
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            // 一般只有形如“artifactId-version.pom"的才是需要的pom文件
            if (fileAbsUrl.endsWith(artifactId + "-" + version + ".pom")) {
                return fileAbsUrl;
            }
        }
        System.err.println("No .pom file found in \"" + directoryUrl + "\"");
        return null;
    }

    /**
     * 在指定目录下寻找jar包，并返回其url
     *
     * @param directoryUrl 目录url
     * @return jarUrl
     */
    private String findJarUrlInDirectory(String directoryUrl) {
        Document document = getDocumentByUrl(directoryUrl);

        if (document == null) {
            return null;
        }

        // 从目录中提取出工件号和版本号
        String[] parts = directoryUrl.split("/");
        String artifactId = parts[parts.length - 2];
        String version = parts[parts.length - 1];

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.jar结尾的文件
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            // 一般只有形如“artifactId-version.jar"的才是需要的jar包
            if (fileAbsUrl.endsWith(artifactId + "-" + version + ".jar")) {
                return fileAbsUrl;
            }
        }
        return null;
    }

    /**
     * 将爬取的pom Document转换为java开源ComponentDO
     * @param document pom Document
     * @param pomUrl pom文件url
     * @param MAVEN_REPO_BASE_URL maven仓库根地址
     * @return ComponentDO
     */
    public ComponentDO convertToComponentDO(Document document, String pomUrl, String MAVEN_REPO_BASE_URL) {
        // 从pom url中提取groupId, artifactId, and version
        String[] parts = pomUrl.split("/");
        String version = parts[parts.length - 2];
        String artifactId = parts[parts.length - 3];
        String groupId = String.join(".", Arrays.copyOfRange(parts, MAVEN_REPO_BASE_URL.split("/").length, parts.length - 3));

        // 将jsoup document转化为maven-model
        Model model = convertToModel(document.outerHtml());
        if (model == null) {
            return null;
        }

        ComponentDO componentDO = new ComponentDO();
        componentDO.setGroupId(groupId);
        componentDO.setArtifactId(artifactId);
        componentDO.setVersion(version);

        componentDO.setName(model.getName() == null ? "-" : model.getName());
        componentDO.setLanguage("java");
        componentDO.setType("opensource");
        componentDO.setDescription(model.getDescription() == null ? "-" : model.getDescription());

        componentDO.setUrl(model.getUrl() == null ? "-" : model.getUrl());
        componentDO.setDownloadUrl(getDownloadUrl(pomUrl));
        componentDO.setSourceUrl(model.getScm() == null ? "-" : (model.getScm().getUrl() == null ? "-" : model.getScm().getUrl()));

        componentDO.setPUrl(getMavenPUrl(groupId, artifactId, version, model.getPackaging()));
        componentDO.setDevelopers(getDevelopers(model));
        componentDO.setLicenses(getLicense(model));

        componentDO.setState("SUCCESS");
        return componentDO;
    }


    /**
     * 生成PUrl（仅对maven组件）
     * 例如：pkg:maven/commons-codec/commons-codec@1.15?type=jar
     * @param groupId 组织Id
     * @param artifactId 工件id
     * @param version 版本号
     * @param packaging 打包方式，如pom、jar
     * @return PUrl
     */
    private static String getMavenPUrl(String groupId, String artifactId, String version, String packaging){
        String pUrl = "pkg:maven/" + groupId + "/" + artifactId + "@" + version;
        if (packaging.equals("jar"))
            pUrl += "?type=jar";
        return pUrl;
    }

    /**
     * 从pomUrl中得到下载地址，即去掉pomUrl中最后一段字符串
     * 例如：https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/即为下载地址
     *
     * @param pomUrl pom url
     * @return download url
     */
    private String getDownloadUrl(String pomUrl) {
        return pomUrl.substring(0, pomUrl.lastIndexOf('/') + 1);
    }

    /**
     * 从maven-model中获得developerDO列表
     *
     * @param model maven-model
     * @return developerDO列表
     */
    private List<DeveloperDO> getDevelopers(Model model) {
        List<org.apache.maven.model.Developer> mavenDevelopers = model.getDevelopers();
        return mavenDevelopers.stream()
                .map(mavenDeveloper -> {
                    DeveloperDO developer = new DeveloperDO();
                    developer.setId(mavenDeveloper.getId() == null ? "-" : mavenDeveloper.getId());
                    developer.setName(mavenDeveloper.getName() == null ? "-" : mavenDeveloper.getName());
                    developer.setEmail(mavenDeveloper.getEmail() == null ? "-" : mavenDeveloper.getEmail());
                    return developer;
                })
                .collect(Collectors.toList());
    }

    /**
     * 从maven-model中获得licenseDO列表
     *
     * @param model maven-model
     * @return licenseDO列表
     */
    private List<LicenseDO> getLicense(Model model) {
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        return mavenLicenses.stream()
                .map(mavenLicense -> {
                    LicenseDO license = new LicenseDO();
                    license.setName(mavenLicense.getName() == null ? "-" : mavenLicense.getName());
                    license.setUrl(mavenLicense.getUrl() == null ? "-" : mavenLicense.getUrl());
                    return license;
                })
                .collect(Collectors.toList());
    }

    /**
     * 将pom string转化为maven-model
     *
     * @param pomString pom.xml转成字符串
     * @return maven-model
     */
    private Model convertToModel(String pomString) {
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

    /**
     * 通过gav获取其pom文件内容
     * @param groupId 组织id
     * @param artifactId 工件id
     * @param version 版本号
     * @return pom string
     */
    public String getPomStrByGav(String groupId, String artifactId, String version) {
        String downloadUrl = MAVEN_REPO_BASE_URL + groupId.replace(".", "/") + "/" + artifactId + "/" + version + "/";
        String pomUrl = findPomUrlInDirectory(downloadUrl);
        if (pomUrl == null) {
            return null;
        }
        Document document = UrlConnector.getDocumentByUrl(pomUrl);
        if (document == null)
            return null;
        return document.outerHtml();
    }
}