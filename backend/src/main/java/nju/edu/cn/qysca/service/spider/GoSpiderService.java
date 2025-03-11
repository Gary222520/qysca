package nju.edu.cn.qysca.service.spider;

import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;

import java.util.List;

public interface GoSpiderService {
    /**
     * 爬虫获取并填充Go组件信息
     *
     * @param name    组件名称
     * @param version 组件版本
     * @return Go组件信息
     */
    GoComponentDO crawlByNV(String name, String version);

    /**
     * 下载url的zip文件到指定存储目录
     *
     * @param downloadUrl 下载地址
     * @param filePath 存储目录地址
     */
    void spiderContent(String downloadUrl, String filePath);

    /**
     * 获取一个包的所有版本
     * @param name 包名
     * @return 版本列表
     */
    List<String> getVersions(String name);
}
