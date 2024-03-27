package nju.edu.cn.qysca.service.spider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.domain.component.dos.LicenseDO;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PythonSpiderServiceImpl implements PythonSpiderService{
    /**
     * pypi仓库根地址
     */
    private final static String PYPI_REPO_BASE_URL = "https://pypi.org/pypi/";

    /**
     * 根据NV爬取python组件
     * @param name 组件名
     * @param version 组件版本
     * @return ComponentDO
     */
    @Override
    public PythonComponentDO crawlByNV(String name, String version){
        String url = PYPI_REPO_BASE_URL + name + "/" + version + "/json";
        return crawl(url, name, version);
    }

    /**
     * 爬取指定url的python组件
     * @param url 组件url
     * @param name 组件名
     * @param version 组件版本
     * @return ComponentDO
     */
    private PythonComponentDO crawl(String url, String name, String version){
        Document document = getDocumentByUrl(url);
        if (document == null)
            return null;

        String jsonString = document.outerHtml();
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
            componentDO.setSourceUrl("");
            componentDO.setPUrl(getPyPiPUrl(name, version));

            List<LicenseDO> licenseDOList = new ArrayList<>();
            licenseDOList.add(new LicenseDO(jsonNode.get("info").get("license").asText(), null));
            componentDO.setLicenses(licenseDOList);

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
     * 给定一个url地址，爬取其内容至一个document
     *
     * @param url url地址
     * @return Document org.jsoup.nodes.Document
     */
    private Document getDocumentByUrl(String url) {
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
}
