package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;

public interface GoSpiderService {
    GoComponentDO crawlComponentInfoByNV(String name, String version);

    void spiderContent(String downloadUrl, String filePath);
}
