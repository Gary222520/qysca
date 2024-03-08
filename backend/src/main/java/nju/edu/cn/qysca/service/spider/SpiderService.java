package nju.edu.cn.qysca.service.spider;


import nju.edu.cn.qysca.domain.component.dos.ComponentDO;

public interface SpiderService {

    ComponentDO crawlByGav(String groupId, String artifactId, String version);

    String  getPomStrByGav(String groupId, String artifactId, String version);
}
