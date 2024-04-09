package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;

public interface JsSpiderService {
    /**
     * 利用爬虫获取组件信息
     *
     * @param name    组件名称
     * @param version 组件版本
     * @return JsComponentDO 组件信息
     */
    JsComponentDO crawlByNV(String name, String version);

    /**
     * 下载npm包到指定存储目录下
     *
     * @param name    组件名称
     * @param version 组件版本
     * @param filePath 存储目录地址
     */
    void spiderContent(String name, String version, String filePath);
}
