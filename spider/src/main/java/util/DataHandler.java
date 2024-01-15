package util;

import domain.Developer;
import domain.License;
import domain.OpensourceComponentDO;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DataHandler {

    public static OpensourceComponentDO convertToDO(Document document, String pomUrl, String language, String opensource){
        // 从pom url中提取groupId, artifactId, and version
        String[] parts = pomUrl.split("/");
        String version = parts[parts.length - 2];
        String artifactId = parts[parts.length - 3];
        String groupId = String.join(".", Arrays.copyOfRange(parts, 0, parts.length - 3));

        Model model = convertToModel(document);
        if (model == null){
            return null;
        }

        OpensourceComponentDO opensourceComponentDO = new OpensourceComponentDO();
        opensourceComponentDO.setGroupId(groupId);
        opensourceComponentDO.setArtifactId(artifactId);
        opensourceComponentDO.setVersion(version);


        opensourceComponentDO.setName(model.getName());
        opensourceComponentDO.setOpensource(opensource);
        opensourceComponentDO.setLanguage(language);
        opensourceComponentDO.setDescription(model.getDescription());

        opensourceComponentDO.setUrl(model.getUrl());
        opensourceComponentDO.setDownloadUrl(getDownloadUrl(pomUrl));
        opensourceComponentDO.setSourceUrl(model.getScm()==null?null:model.getScm().getUrl());

        //opensourceComponentDO.setDevelopers(getDevelopers(model));
        opensourceComponentDO.setDevelopers(null);
        //opensourceComponentDO.setLicenses(getLicense(model));
        opensourceComponentDO.setLicenses(null);
        //opensourceComponentDO.setPom(convertToJson(document));
        opensourceComponentDO.setPom(null);

        return opensourceComponentDO;

    }

    private static String getDownloadUrl(String pomUrl){
        return pomUrl.substring(0, pomUrl.lastIndexOf('/') + 1);
    }

    private static List<Developer> getDevelopers(Model model) {
        List<org.apache.maven.model.Developer> mavenDevelopers = model.getDevelopers();
        return mavenDevelopers.stream()
                .map(mavenDeveloper -> {
                    Developer developer = new Developer();
                    developer.setId(mavenDeveloper.getId());
                    developer.setName(mavenDeveloper.getName());
                    developer.setEmail(mavenDeveloper.getEmail());
                    return developer;
                })
                .collect(Collectors.toList());
    }

    private static List<License> getLicense(Model model){
        List<org.apache.maven.model.License> mavenLicenses = model.getLicenses();
        return mavenLicenses.stream()
                .map(mavenLicense ->{
                    License license = new License();
                    license.setName(license.getName());
                    license.setUrl(license.getUrl());
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
}
