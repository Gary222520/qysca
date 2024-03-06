package nju.edu.cn.qysca.controller.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.CreateApplicationDTO;
import nju.edu.cn.qysca.service.application.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "应用管理")
@RestController
@RequestMapping("qysca/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @ApiOperation("创建新应用")
    @PostMapping("/createApplication")
    public ResponseMsg<Boolean> createApplication(@RequestBody CreateApplicationDTO createApplicationDTO) {
        return new ResponseMsg<>(applicationService.createApplication(createApplicationDTO));
    }

    @ApiOperation("删除应用")
    @PostMapping("/deleteApplication")
    public ResponseMsg<Boolean> deleteApplication(@RequestParam String groupId, @RequestParam String artifactId, @RequestParam String version) {
        return new ResponseMsg<>(applicationService.deleteApplication(groupId, artifactId, version));
    }

    @ApiOperation("查询应用信息")
    @GetMapping("/getApplication")
    public ResponseMsg<List<ApplicationDO>> getApplication(@RequestParam String groupId, @RequestParam String artifactId) {
        return new ResponseMsg<>(applicationService.getApplication(groupId, artifactId));
    }

    @ApiOperation("查看应用版本信息")
    @GetMapping("/getApplicationVersionList")
    public ResponseMsg<List<String>> getApplicationVersionList(@RequestParam String groupId, @RequestParam String artifactId) {
        return new ResponseMsg<>(applicationService.getApplicationVersionList(groupId, artifactId));
    }

    @ApiOperation("查看系统具体版本信息")
    @GetMapping("/getApplicationVersion")
    public ResponseMsg<ApplicationDO> getApplicationVersion(@RequestParam String groupId, @RequestParam String artifactId, @RequestParam String version) {
        return new ResponseMsg<>(applicationService.getApplicationVersion(groupId, artifactId, version));
    }
}
