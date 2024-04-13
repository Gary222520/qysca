package nju.edu.cn.qysca.service.spider;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class GoSpiderServiceImpl implements GoSpiderService{
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private VulnerabilityService vulnerabilityService;
    @Value("${tempGoFolder}")
    private String tempFolder;
    @Value("${GO_REPO_BASE_URL}")
    private String GO_REPO_BASE_URL;

    /**
     * 爬虫获取并填充Go组件信息
     *
     * @param name    组件名称
     * @param version 组件版本
     * @return Go组件信息
     */
    @Override
    public GoComponentDO crawlByNV(String name, String version) {
        GoComponentDO goComponentDO = new GoComponentDO();
        // 配置基本信息
        goComponentDO.setName(name);
        goComponentDO.setVersion(version);
        goComponentDO.setLanguage("golang");
        goComponentDO.setType("opensource");
        goComponentDO.setUrl("https://pkg.go.dev/" + name + "@" + version);
        goComponentDO.setDownloadUrl("https://goproxy.cn/" + name + "/@v/" + version + ".zip");
        goComponentDO.setSourceUrl("https://" + name);
        goComponentDO.setPUrl("pkg:golang/" + name + "@" + version);
        goComponentDO.setCreator(null);
        goComponentDO.setState("SUCCESS");
        // 利用github api获取组件信息（description, licenses），非github组件暂不支持
        if (!name.startsWith("github.com/")) {
            goComponentDO.setDescription("-");
            return goComponentDO;
        }
        try {
            String url = GO_REPO_BASE_URL + name.substring(11);

            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            String timeStamp = dateFormat.format(date);
            System.out.println(timeStamp + " crawling :" + url);

            // 创建HttpClient对象
            CloseableHttpClient httpClient = HttpClients.createDefault();
            // 声明访问地址
            HttpGet httpGet = new HttpGet(url);
            // 设置请求头
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/38.0.101.76 Safari/537.36");
            httpGet.addHeader("Authorization", "Bearer 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");
            httpGet.addHeader("Accept", "application/vnd.github+json");
            httpGet.addHeader("X-GitHub-Api-Version", "2022-11-28");
            // 发起请求
            CloseableHttpResponse response = httpClient.execute(httpGet);
            // 判断响应码是否为200
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new RuntimeException();
            }
            // 获取内容，解析成json，填充组件信息
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject jsonObject = JSON.parseObject(content);
            String description = jsonObject.getString("description");
            if (null != description && description.length() != 0) {
                goComponentDO.setDescription(description);
            }
            List<String> licenses = new ArrayList<>();
            JSONObject license = jsonObject.getJSONObject("license");
            if (null != license) {
                String lname = license.getString("spdx_id");
                String lurl = license.getString("url");
                if (null != lname && lname.length() != 0 && null != lurl && lurl.length() != 0) {
                    licenses.addAll(licenseService.searchLicense(lname));
                }
            }
            goComponentDO.setLicenses(licenses.toArray(new String[0]));
            goComponentDO.setVulnerabilities(vulnerabilityService.findVulnerabilities(name, version, "golang").toArray(new String[0]));
        } catch (Exception e) {
            System.err.println("通过github api获取组件信息失败: " + name + "@" + version);
            goComponentDO.setDescription("-");
        }
        return goComponentDO;
    }

    /**
     * 下载url的zip文件到指定存储目录
     *
     * @param downloadUrl 下载地址
     * @param filePath 存储目录地址
     */
    @Override
    public void spiderContent(String downloadUrl, String filePath) {


        try {
            URL url = new URL(downloadUrl);
            URLConnection connection = url.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            httpURLConnection.setConnectTimeout(10000);
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.connect();
            BufferedInputStream bin = new BufferedInputStream(httpURLConnection.getInputStream());

            File file = new File(filePath, "gotmp.zip");
            OutputStream out = new FileOutputStream(file);
            int size = 0;
            byte[] b = new byte[2048];
            while ((size = bin.read(b)) != -1) {
                out.write(b, 0, size);
            }
            bin.close();
            out.close();
        } catch (Exception e){
            throw new PlatformException(500, "识别依赖信息失败");
        }
    }
}
