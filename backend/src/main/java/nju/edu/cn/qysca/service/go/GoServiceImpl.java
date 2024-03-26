package nju.edu.cn.qysca.service.go;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import nju.edu.cn.qysca.domain.component.dos.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GoServiceImpl implements GoService{
    @Override
    public ComponentDO componentAnalysis(String filePath, String builder, String type) {
        return null;
    }

    @Override
    public DependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String type) {
        return null;
    }

    @Override
    public List<DependencyTableDO> dependencyTableAnalysis(DependencyTreeDO dependencyTreeDO) {
        return null;
    }

    @Override
    public DependencyTreeDO spiderDependency(String groupId, String artifactId, String version) {
        return null;
    }

    /**
     * 爬虫获取并填充Go组件信息
     *
     * @param name    组件名称
     * @param version 组件版本
     * @return Go组件信息
     */
    @Override
    public GoComponentDO spiderComponentInfo(String name, String version) {
        GoComponentDO goComponentDO=new GoComponentDO();
        // 配置基本信息
        goComponentDO.setName(name);
        goComponentDO.setVersion(version);
        goComponentDO.setLanguage("golang");
        goComponentDO.setType("opensource");
        goComponentDO.setUrl("https://pkg.go.dev/"+name+"@"+version);
        goComponentDO.setDownloadUrl("https://goproxy.cn/"+name+"/@v/"+version+".zip");
        goComponentDO.setSourceUrl("https://"+name);
        goComponentDO.setPUrl("pkg:golang/"+name+"@"+version);
        goComponentDO.setCreator(null);
        goComponentDO.setState("SUCCESS");
        // 利用github api获取组件信息（description, licenses），非github组件暂不支持
        if(!name.startsWith("github.com/")){
            goComponentDO.setDescription("-");
            goComponentDO.setLicenses(new ArrayList<>());
            return goComponentDO;
        }
        try {
            String url="https://api.github.com/repos/"+name.substring(11);
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
            if(response.getStatusLine().getStatusCode()!=200){
                throw new RuntimeException();
            }
            // 获取内容，解析成json，填充组件信息
            String content= EntityUtils.toString(response.getEntity(),"UTF-8");
            JSONObject jsonObject= JSON.parseObject(content);
            String description=jsonObject.getString("description");
            if(null!=description && description.length()!=0){
                goComponentDO.setDescription(description);
            }
            List<LicenseDO> licenses=new ArrayList<>();
            JSONObject license=jsonObject.getJSONObject("license");
            if(null !=license){
                String lname=license.getString("spdx_id");
                String lurl=license.getString("url");
                if(null!=lname && lname.length()!=0 && null!=lurl && lurl.length()!=0){
                    LicenseDO licenseDO=new LicenseDO(lname,lurl);
                    licenses.add(licenseDO);
                }
            }
            goComponentDO.setLicenses(licenses);
        }catch (Exception e){
            System.err.print("通过github api获取组件信息失败: "+name+"@"+version);
            goComponentDO.setDescription("-");
            goComponentDO.setLicenses(new ArrayList<>());
        }
        return goComponentDO;
    }
}
