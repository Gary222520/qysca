package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.ComponentDO;

public interface PythonSpiderService {
    ComponentDO crawlByNV(String name, String version);
}
