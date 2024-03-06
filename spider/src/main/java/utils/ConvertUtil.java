package utils;

import domain.component.ComponentDO;
import domain.component.DeveloperDO;
import domain.component.LicenseDO;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.nodes.Document;
import utils.idGenerator.UUIDGenerator;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertUtil {

    /**
     *
     * @param document
     * @param pomUrl
     * @param MAVEN_REPO_BASE_URL
     * @return
     */
    public static ComponentDO convertToComponentDO(Document document, String pomUrl, String MAVEN_REPO_BASE_URL){
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
        componentDO.setId(UUIDGenerator.getUUID());
        componentDO.setGroupId(groupId);
        componentDO.setArtifactId(artifactId);
        componentDO.setVersion(version);

        componentDO.setName(model.getName() == null ? "-" : model.getName());
        componentDO.setLanguage("Java");
        componentDO.setOpensource(true);
        componentDO.setDescription(model.getDescription() == null ? "-" : model.getDescription());

        componentDO.setUrl(model.getUrl() == null ? "-" : model.getUrl());
        componentDO.setDownloadUrl(getDownloadUrl(pomUrl));
        componentDO.setSourceUrl(model.getScm() == null ? "-" : (model.getScm().getUrl() == null ? "-" : model.getScm().getUrl()));

        //todo
        componentDO.setPUrl("-");
        componentDO.setDevelopers(getDevelopers(model));
        componentDO.setLicenses(getLicense(model));

        //todo
        componentDO.setHashes(null);

        return componentDO;
    }

    /**
     * 从pomUrl中得到下载地址，即去掉pomUrl中最后一段字符串
     * 例如：https://repo.maven.apache.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/即为下载地址
     *
     * @param pomUrl pom url
     * @return download url
     */
    private static String getDownloadUrl(String pomUrl) {
        return pomUrl.substring(0, pomUrl.lastIndexOf('/') + 1);
    }

    /**
     * 从maven-model中获得developerDO列表
     *
     * @param model maven-model
     * @return developerDO列表
     */
    private static List<DeveloperDO> getDevelopers(Model model) {
        List<org.apache.maven.model.Developer> mavenDevelopers = model.getDevelopers();
        // 使用stream流转化
        return mavenDevelopers.stream()
                .map(mavenDeveloper -> {
                    DeveloperDO developer = new DeveloperDO();
                    developer.setId(mavenDeveloper.getId());
                    developer.setName(mavenDeveloper.getName());
                    developer.setEmail(mavenDeveloper.getEmail());
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
    private static List<LicenseDO> getLicense(Model model) {
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        return mavenLicenses.stream()
                .map(mavenLicense -> {
                    LicenseDO license = new LicenseDO();
                    license.setName(mavenLicense.getName());
                    license.setUrl(mavenLicense.getUrl());
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
    private static Model convertToModel(String pomString) {
        Model model = null;
        try {
            // 使用Maven的Xpp3Reader解析xml格式的字符串,获得maven-model
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(new StringReader(pomString));
            return model;
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将jsoup document转化为json对象
     *
     * @param document jsoup document
     * @return json对象
     */
    private static JSONObject convertToJson(Document document) {
        return XML.toJSONObject(document.outerHtml());
    }

    /**
     * 从指定地址的pom文件获取json对象
     *
     * @param pomFilePath pom文件地址
     * @return json对象
     */
    private static JSONObject convertToJson(String pomFilePath) {
        try {
            String pomString = new String(Files.readAllBytes(Paths.get(pomFilePath)));
            return XML.toJSONObject(pomString);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}