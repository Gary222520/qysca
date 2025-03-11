package nju.edu.cn.qysca.service.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.vulnerability.VulnerabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class PythonSpiderServiceImpl implements PythonSpiderService{
    @Value("${PYPI_REPO_BASE_URL}")
    private String PYPI_REPO_BASE_URL;

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private VulnerabilityService vulnerabilityService;

    /**
     * 根据NV爬取python组件
     * @param name 组件名
     * @param version 组件版本，如果为"-"，则为改包的默认版本
     * @return ComponentDO
     */
    @Override
    public PythonComponentDO crawlByNV(String name, String version){
        String url = PYPI_REPO_BASE_URL + name + "/" + version + "/json";
        return crawl(url, name, version);
    }

    /**
     * 获取一个包的所有版本
     * @param name 包名
     * @return 版本列表
     */
    @Override
    public List<String> getVersions(String name){
        String url = PYPI_REPO_BASE_URL + name + "/json";
        String jsonString = getUrlContent(url);
        if (jsonString == null || jsonString.isEmpty())
            return null;
        try {
            List<String> versions = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            if (jsonNode.get("releases")!=null)
                jsonNode.get("releases").fieldNames().forEachRemaining(versions::add);
            return versions;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 爬取指定url的python组件
     * @param url 组件url
     * @param name 组件名
     * @param version 组件版本
     * @return ComponentDO
     */
    private PythonComponentDO crawl(String url, String name, String version){
        String jsonString = getUrlContent(url);
        if (jsonString == null || jsonString.isEmpty())
            return null;
        PythonComponentDO componentDO = convertToComponentDO(jsonString, name, version);
        return componentDO;
    }

    /**
     * 将json string 转换成 ComponentDO
     * @param jsonString json形式的组件信息
     * @return ComponentDO
     */
    private PythonComponentDO convertToComponentDO(String jsonString, String name, String version){
        PythonComponentDO componentDO = new PythonComponentDO();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            componentDO.setName(name);
            componentDO.setVersion(version);
            componentDO.setLanguage("python");
            componentDO.setType("opensource");
            componentDO.setDescription(jsonNode.get("info").get("description").asText());
            componentDO.setAuthor(jsonNode.get("info").get("author").asText());
            componentDO.setAuthorEmail(jsonNode.get("info").get("author_email").asText());
            componentDO.setUrl(jsonNode.get("info").get("home_page").asText());
            componentDO.setDownloadUrl(jsonNode.get("info").get("package_url").asText());
            componentDO.setSourceUrl(jsonNode.get("info").get("release_url").asText());
            componentDO.setPUrl(getPyPiPUrl(name, version));

            String license = jsonNode.get("info").get("license").asText();
            if (license.length()>=100){
                // 有时候pypi里license会写原内容，此时就先设为无许可证
                license = "";
            }
            componentDO.setLicenses(licenseService.searchLicense(license).toArray(new String[0]));
            componentDO.setVulnerabilities(vulnerabilityService.findVulnerabilities(name, version, "python").toArray(new String[0]));

            componentDO.setCreator(null);
            componentDO.setState("SUCCESS");


        } catch (JsonProcessingException e){
            e.printStackTrace();
        }
        return componentDO;
    }

    /**
     * 生成PUrl（仅对python-pypi组件）
     * 例如：pkg:pypi/django@1.11.1
     * @param name 组件名
     * @param version 版本号
     * @return PUrl
     */
    private static String getPyPiPUrl(String name, String version){
        return "pkg:pypi/" + name + "@" + version;
    }

    /**
     * 爬取指定url的内容，以字符串返回
     * @param urlString url地址
     * @return String
     */
    private static String getUrlContent(String urlString) {
        StringBuilder content = new StringBuilder();

        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
        } catch (Exception e) {
            return null;
        }

        return content.toString();
    }
}
