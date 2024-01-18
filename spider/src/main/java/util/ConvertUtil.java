package util;

import domain.component.DeveloperDO;
import domain.component.JavaOpenComponentDO;
import domain.component.LicenseDO;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.nodes.Document;
import util.idGenerator.UUIDGenerator;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertUtil {

    /**
     * 将一个jsoup Document转化为JavaOpenComponentDO
     *
     * @param document            jsoup document 从pomUrl爬取
     * @param pomUrl              pomUrl
     * @param MAVEN_REPO_BASE_URL maven仓库基地址
     * @return JavaOpenComponentDO
     */
    public static JavaOpenComponentDO convertToJavaOpenComponentDO(Document document, String pomUrl, String MAVEN_REPO_BASE_URL) {
        // 从pom url中提取groupId, artifactId, and version
        String[] parts = pomUrl.split("/");
        String version = parts[parts.length - 2];
        String artifactId = parts[parts.length - 3];
        String groupId = String.join(".", Arrays.copyOfRange(parts, MAVEN_REPO_BASE_URL.split("/").length, parts.length - 3));

        // 将jsoup document转化为maven-model
        Model model = convertToModel(document);
        if (model == null) {
            return null;
        }

        // 创建JavaOpenComponentDO对象，填充值
        JavaOpenComponentDO javaOpenComponentDO = new JavaOpenComponentDO();
        javaOpenComponentDO.setId(UUIDGenerator.getUUID());
        javaOpenComponentDO.setGroupId(groupId);
        javaOpenComponentDO.setArtifactId(artifactId);
        javaOpenComponentDO.setVersion(version);

        javaOpenComponentDO.setLanguage("Java");
        javaOpenComponentDO.setName(model.getName()==null?"-":model.getName());
        javaOpenComponentDO.setDescription(model.getDescription()==null?"-":model.getDescription());

        javaOpenComponentDO.setUrl(model.getUrl()==null?"-":model.getUrl());
        javaOpenComponentDO.setDownloadUrl(getDownloadUrl(pomUrl));
        javaOpenComponentDO.setSourceUrl(model.getScm()==null?"-":(model.getScm().getUrl()==null?"-":model.getScm().getUrl()));

        javaOpenComponentDO.setDevelopers(getDevelopers(model));
        javaOpenComponentDO.setLicenses(getLicense(model));
        javaOpenComponentDO.setPom(convertToJson(document).toString());

        return javaOpenComponentDO;

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
                    developer.setDeveloperId(mavenDeveloper.getId());
                    developer.setDeveloperName(mavenDeveloper.getName());
                    developer.setDeveloperEmail(mavenDeveloper.getEmail());
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
                    license.setLicenseName(mavenLicense.getName());
                    license.setLicenseUrl(mavenLicense.getUrl());
                    return license;
                })
                .collect(Collectors.toList());
    }

    /**
     * 将jsoup document转化为maven-model
     *
     * @param document jsoup document
     * @return maven-model
     */
    private static Model convertToModel(Document document) {
        // 从document中获取全部内容，得到一个xml格式的字符串
        String pomString = document.outerHtml();
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