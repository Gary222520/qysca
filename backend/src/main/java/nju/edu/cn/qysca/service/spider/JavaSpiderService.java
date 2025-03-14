package nju.edu.cn.qysca.service.spider;


import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;

public interface JavaSpiderService {

    /**
     * 通过gav爬取组件
     * @param groupId 组织id
     * @param artifactId 工件id
     * @param version 版本
     * @return JavaComponentDO
     */
    JavaComponentDO crawlByGav(String groupId, String artifactId, String version);

    /**
     * 通过gav获取其pom文件内容
     * @param groupId 组织id
     * @param artifactId 工件id
     * @param version 版本号
     * @return pom string
     */
    String getPomStrByGav(String groupId, String artifactId, String version);

    /**
     * 递归地爬取目录url下所有组件
     * @param directoryUrl 要爬取的目录
     */
    void crawlDirectory(String directoryUrl);

    /**
     * 递归地爬取目录url下（带依赖的）所有组件
     * @param directoryUrl 要爬取的目录
     */
    void crawlDirectoryWithDependency(String directoryUrl);
}
