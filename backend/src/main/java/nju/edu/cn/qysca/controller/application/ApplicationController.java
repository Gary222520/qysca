package nju.edu.cn.qysca.controller.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.*;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import nju.edu.cn.qysca.service.application.ApplicationService;
import nju.edu.cn.qysca.service.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "应用管理")
@RestController
@RequestMapping("qysca/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private ProjectService projectService;


    @ApiOperation("获取所有应用")
    @GetMapping("/getApplicationList")
    public ResponseMsg<Page<ApplicationDO>> getApplicationList(@RequestParam Integer number, @RequestParam Integer size) {
        return new ResponseMsg<>(applicationService.getApplicationList(number, size));
    }

    @ApiOperation("创建新应用")
    @PostMapping("/createApplication")
    public ResponseMsg<Boolean> createApplication(@RequestBody CreateApplicationDTO createApplicationDTO) {
        return new ResponseMsg<>(applicationService.createApplication(createApplicationDTO));
    }

    @ApiOperation("删除应用")
    @PostMapping("/deleteApplication")
    public ResponseMsg<Boolean> deleteApplication(@RequestParam String groupId, @RequestParam String artifactId) {
        return new ResponseMsg<>(applicationService.deleteApplication(groupId, artifactId));
    }

    @ApiOperation("删除应用某个版本")
    @PostMapping("/deleteApplicationVersion")
    public ResponseMsg<Boolean> deleteApplicationVersion(@RequestParam String groupId, @RequestParam String artifactId, @RequestParam String version) {
        return new ResponseMsg<>(applicationService.deleteApplicationVersion(groupId, artifactId, version));
    }


    @ApiOperation("查看应用版本信息")
    @GetMapping("/getApplicationVersionList")
    public ResponseMsg<List<String>> getApplicationVersionList(@RequestParam String groupId, @RequestParam String artifactId) {
        return new ResponseMsg<>(applicationService.getApplicationVersionList(groupId, artifactId));
    }

    @ApiOperation("查看系统具体版本信息")
    @GetMapping("/getApplicationVersion")
    public ResponseMsg<List<ProjectDO>> getApplicationVersion(@RequestParam String groupId, @RequestParam String artifactId, @RequestParam String version) {
        return new ResponseMsg<>(applicationService.getApplicationVersion(groupId, artifactId, version));
    }

    @ApiOperation("向应用中增加项目")
    @PostMapping("/addProject")
    public ResponseMsg<Boolean> addProject(@RequestBody AddProjectDTO addProjectDTO) {
        return new ResponseMsg<>(applicationService.addProject(addProjectDTO));
    }

    @ApiOperation("在应用中删除项目")
    @PostMapping("/deleteProject")
    public ResponseMsg<Boolean> deleteProject(@RequestBody DeleteProjectDTO deleteProjectDTO) {
        return new ResponseMsg<>(applicationService.deleteProject(deleteProjectDTO));
    }

    @ApiOperation("在应用中创建项目")
    @PostMapping("/createProject")
    public ResponseMsg<Boolean> createProject(@RequestBody CreateAppProjectDTO createAppProjectDTO) {
        return new ResponseMsg<>(applicationService.createAppProject(createAppProjectDTO));
    }

    @ApiOperation("在应用中更新项目")
    @PostMapping("/updateProject")
    public ResponseMsg<Boolean> updateProject(@RequestBody UpdateAppProjectDTO updateAppProjectDTO) {
        return new ResponseMsg<>(applicationService.updateAppProject(updateAppProjectDTO));
    }

    @ApiOperation("在应用中升级项目")
    @PostMapping("/upgradeProject")
    public ResponseMsg<Boolean> upgradeProject(@RequestBody UpgradeAppProjectDTO upgradeAppProjectDTO) {
        return new ResponseMsg<>(applicationService.upgradeAppProject(upgradeAppProjectDTO));
    }


}
