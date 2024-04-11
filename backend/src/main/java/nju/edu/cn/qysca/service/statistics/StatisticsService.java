package nju.edu.cn.qysca.service.statistics;

import nju.edu.cn.qysca.domain.statistics.dtos.LicenseStatisticsDTO;
import nju.edu.cn.qysca.domain.statistics.dtos.VulnerabilityStatisticsDTO;

public interface StatisticsService {

    /**
     * 获得用户所属部门的应用总数
     * @return Integer 应用总数
     */
    Integer getApplicationCount();


    /**
     * 查询用户所属部门的应用的组件总数
     * @return Integer 应用的组件总数
     */
    Integer getComponentCount();


    /**
     * 查询用户所属部门的应用的漏洞总数
     * @return Integer 应用的漏洞总数
     */
    VulnerabilityStatisticsDTO getVulnerabilityStatistics();

    /**
     * 获得用户所属部门的应用的许可证总数
     * @return Integer 应用的许可证总数
     */
    LicenseStatisticsDTO getLicenseStatistics();
}
