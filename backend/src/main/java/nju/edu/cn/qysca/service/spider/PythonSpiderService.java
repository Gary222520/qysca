package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;

public interface PythonSpiderService {
    PythonComponentDO crawlByNV(String name, String version);
}
