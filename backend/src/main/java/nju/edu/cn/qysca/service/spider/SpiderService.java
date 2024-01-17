package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.JavaOpenComponentDO;

public interface SpiderService {

    JavaOpenComponentDO crawlByGav(String groupId, String artifactId, String version);
}
