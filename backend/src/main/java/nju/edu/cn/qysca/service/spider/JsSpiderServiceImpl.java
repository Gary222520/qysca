package nju.edu.cn.qysca.service.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.vulnerability.VulnerabilityService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class JsSpiderServiceImpl implements JsSpiderService {

    private final String FILE_SEPARATOR = "/";
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private VulnerabilityService vulnerabilityService;
    @Value("${tempNpmFolder}")
    private String tempFolder;
    @Value("${NPM_REPO_BASE_URL}")
    private String NPM_REPO_BASE_URL;


    /**
     * 利用爬虫获取组件信息
     *
     * @param name    组件名称
     * @param version 组件版本
     * @return JsComponentDO 组件信息
     */
    @Override
    public JsComponentDO crawlByNV(String name, String version) {
        //
        long sst = System.currentTimeMillis();
        //

        JsComponentDO jsComponentDO = new JsComponentDO();
        jsComponentDO.setName(name);
        jsComponentDO.setVersion(version);
        jsComponentDO.setPUrl("pkg:npm/" + name + "@" + version);
        jsComponentDO.setLanguage("javaScript");
        jsComponentDO.setType("opensource");
        jsComponentDO.setCreator("-");
        try {
            if (version.contains("npm")) {
                String[] temp = parsePackageNameAndVersion(version);
                if (temp != null) {
                    name = temp[0];
                    version = temp[1];
                }
            }
            String url = NPM_REPO_BASE_URL + name + FILE_SEPARATOR + version;

            //
            long st = System.currentTimeMillis();
            //

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");

            //
            long et = System.currentTimeMillis();
            log.info("抓取url耗时：" + (et-st));
            //


            JSONObject jsonObject = JSON.parseObject(content);
            jsComponentDO.setDescription(jsonObject.getString("description"));
            jsComponentDO.setUrl(jsonObject.getString("homepage"));
            if (jsonObject.get("repository") instanceof JSONObject) {
                jsComponentDO.setSourceUrl(jsonObject.getJSONObject("repository").getString("url"));
            } else {
                jsComponentDO.setSourceUrl(jsonObject.getString("repository"));
            }
            if (jsonObject.get("license") != null) {
                jsComponentDO.setLicenses(licenseService.searchLicense(jsonObject.getString("license")).toArray(new String[0]));
            } else {
                jsComponentDO.setLicenses(new String[0]);
            }

            //
            st = System.currentTimeMillis();
            //

            jsComponentDO.setVulnerabilities(vulnerabilityService.findVulnerabilities(name, version, "javaScript").toArray(new String[0]));

            //
            et = System.currentTimeMillis();
            log.info("解析漏洞耗时："+ (et-st));
            //

            jsComponentDO.setDownloadUrl(jsonObject.getJSONObject("dist") == null ? "" : jsonObject.getJSONObject("dist").getString("tarball"));
            List<String> copyrightStatements = new ArrayList<>();
            if (jsonObject.get("author") instanceof JSONObject) {
                if (jsonObject.getJSONObject("author") != null) {
                    copyrightStatements.add(jsonObject.getJSONObject("author").getString("name") + " ," + jsonObject.getJSONObject("author").getString("email"));
                }
            }
            if (jsonObject.get("contributors") instanceof JSONArray) {
                JSONArray jsonArray = jsonObject.getJSONArray("contributors");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject contributor = jsonArray.getJSONObject(i);
                        StringBuilder contributorStr = new StringBuilder();
                        contributorStr.append(contributor.getString("name"));
                        if (contributor.containsKey("email")) {
                            contributorStr.append(" ,").append(contributor.getString("email"));
                        }
                        if (contributor.containsKey("url")) {
                            contributorStr.append(" ,").append(contributor.getString("url"));
                        }
                        copyrightStatements.add(contributorStr.toString());
                    }
                }
            }
            jsComponentDO.setCopyrightStatements(copyrightStatements.toArray(new String[0]));
            jsComponentDO.setState("SUCCESS");
        } catch (Exception e) {
            return null;
        }

        //
        long eet =System.currentTimeMillis();
        log.info("爬取组件总耗时：" + (eet-sst));
        //

        return jsComponentDO;
    }

    /**
     * 下载npm包到指定存储目录下
     *
     * @param name    组件名称
     * @param version 组件版本
     * @param filePath 存储目录地址
     */
    @Override
    public void spiderContent(String name, String version, String filePath) {
        try {
            String url = NPM_REPO_BASE_URL + name + FILE_SEPARATOR + version;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println(response.getStatusLine().getStatusCode());
                throw new PlatformException(500, "存在未识别的组件");
            }
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");

            File file = new File(filePath, "package.json");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            throw new PlatformException(500, "存在未识别的组件");
        }
    }

    /**
     * 获取一个包的所有版本
     * @param name 包名
     * @return 版本列表
     */
    @Override
    public List<String> getVersions(String name) {
        String url = NPM_REPO_BASE_URL + name;
        String jsonString = getUrlContent(url);
        if (jsonString == null || jsonString.isEmpty())
            return null;
        try {
            List<String> versions = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonString);
            if (jsonNode.get("versions")!=null)
                jsonNode.get("versions").fieldNames().forEachRemaining(versions::add);
            return versions;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    /**
     * 解析版本号
     *
     * @param version 版本号
     * @return String[] 组件名称和版本
     */
    private String[] parsePackageNameAndVersion(String version) {
        Pattern pattern = Pattern.compile("npm:(.+)@(.+)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return null;
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
