package nju.edu.cn.qysca.controller.statistics;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.statistics.dtos.LicenseStatisticsDTO;
import nju.edu.cn.qysca.domain.statistics.dtos.VulnerabilityStatisticsDTO;
import nju.edu.cn.qysca.service.statistics.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "统计概览")
@RestController
@RequestMapping("qysca/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @ApiOperation("查询用户所在部门的应用总数")
    @GetMapping("/getApplicationCount")
    @PreAuthorize("@my.checkAuth('/qysca/statistics/getApplicationCount')")
    public ResponseMsg<Integer> getApplicationCount(){
        return new ResponseMsg<>(statisticsService.getApplicationCount());
    }

    @ApiOperation("查询用户所在部门的应用的组件总数")
    @GetMapping("/getComponentCount")
    @PreAuthorize("@my.checkAuth('/qysca/statistics/getComponentCount')")
    public ResponseMsg<Integer> getComponentCount(){
        return new ResponseMsg<>(statisticsService.getComponentCount());
    }

    @ApiOperation("查询用户所在部门的应用的漏洞统计")
    @GetMapping("/getVulnerabilityStatistics")
    @PreAuthorize("@my.checkAuth('/qysca/statistics/getVulnerabilityStatistics')")
    public ResponseMsg<VulnerabilityStatisticsDTO> getVulnerabilityStatistics(){
        return new ResponseMsg<>(statisticsService.getVulnerabilityStatistics());
    }

    @ApiOperation("查询用户所在部门的应用的许可证统计")
    @GetMapping("/getLicenseStatistics")
    @PreAuthorize("@my.checkAuth('/qysca/statistics/getLicenseStatistics')")
    public ResponseMsg<LicenseStatisticsDTO> getLicenseStatistics(){
        return new ResponseMsg<>(statisticsService.getLicenseStatistics());
    }
}
