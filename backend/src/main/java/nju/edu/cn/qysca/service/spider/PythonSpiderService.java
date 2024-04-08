package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;

public interface PythonSpiderService {
    /**
     * 根据NV爬取python组件
     * @param name 组件名
     * @param version 组件版本，如果为"-"，则为改包的默认版本
     * @return ComponentDO
     */
    PythonComponentDO crawlByNV(String name, String version);
}
