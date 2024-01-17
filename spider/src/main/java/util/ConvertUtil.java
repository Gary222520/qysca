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

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConvertUtil {

    public static JavaOpenComponentDO convertToJavaOpenComponentDO(Document document, String pomUrl, String MAVEN_REPO_BASE_URL){
        // 从pom url中提取groupId, artifactId, and version
        // https://repo1.maven.org/maven2/org/springframework/boot/aot/org.springframework.boot.aot.gradle.plugin/3.0.9/org.springframework.boot.aot.gradle.plugin-3.0.9.pom
        String[] parts = pomUrl.split("/");
        String version = parts[parts.length - 2];
        String artifactId = parts[parts.length - 3];
        String groupId = String.join(".", Arrays.copyOfRange(parts, MAVEN_REPO_BASE_URL.split("/").length, parts.length - 3));

        Model model = convertToModel(document);
        if (model == null){
            return null;
        }

        JavaOpenComponentDO javaOpenComponentDO = new JavaOpenComponentDO();
        javaOpenComponentDO.setGroupId(groupId);
        javaOpenComponentDO.setArtifactId(artifactId);
        javaOpenComponentDO.setVersion(version);

        javaOpenComponentDO.setLanguage("Java");
        javaOpenComponentDO.setName(model.getName());
        javaOpenComponentDO.setDescription(model.getDescription());

        javaOpenComponentDO.setUrl(model.getUrl());
        javaOpenComponentDO.setDownloadUrl(getDownloadUrl(pomUrl));
        javaOpenComponentDO.setSourceUrl(model.getScm()==null?null:model.getScm().getUrl());

        javaOpenComponentDO.setDevelopers(getDevelopers(model));
        javaOpenComponentDO.setLicenses(getLicense(model));
        javaOpenComponentDO.setPom(convertToJson(document).toString());

        return javaOpenComponentDO;

    }

    private static String getDownloadUrl(String pomUrl){
        return pomUrl.substring(0, pomUrl.lastIndexOf('/') + 1);
    }

    private static List<DeveloperDO> getDevelopers(Model model) {
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

    private static List<LicenseDO> getLicense(Model model){
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        return mavenLicenses.stream()
                .map(mavenLicense ->{
                    LicenseDO license = new LicenseDO();
                    license.setLicenseName(mavenLicense.getName());
                    license.setLicenseUrl(mavenLicense.getUrl());
                    return license;
                })
                .collect(Collectors.toList());
    }

    private static Model convertToModel(Document document) {
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

    private static JSONObject convertToJson(Document document) {
        return XML.toJSONObject(document.outerHtml());
    }

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
