package nju.edu.cn.qysca.controller.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nju.edu.cn.qysca.auth.Authorized;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.application.dtos.*;
import nju.edu.cn.qysca.service.application.ApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Api(tags = "应用管理")
@RestController
@RequestMapping("qysca/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;


    @ApiOperation("分页获取根应用信息")
    @GetMapping("/findRootPage")
    public ResponseMsg<Page<ApplicationDO>> findRootPage(
            @ApiParam(value = "页码", required = true) @RequestParam int number,
            @ApiParam(value = "页大小", required = true) @RequestParam int size) {
        return new ResponseMsg<>(applicationService.findRootPage(number, size));
    }


    @ApiOperation("模糊查询应用名称")
    @GetMapping("/searchApplicationName")
    public ResponseMsg<List<String>> searchApplicationName(@ApiParam(value = "应用名称", required = true) @RequestParam String name) {
        return new ResponseMsg<>(applicationService.searchApplicationName(name));
    }


    @ApiOperation("根据名称查询应用 并返回应用的最新版本")
    @GetMapping("/findApplication")
    public ResponseMsg<ApplicationDO> findApplication(@ApiParam(value = "应用名称", required = true) @RequestParam String name) {
        return new ResponseMsg<>(applicationService.findApplication(name));
    }

    @ApiOperation("查询子应用和子组件")
    @GetMapping("/findSubApplication")
    public ResponseMsg<SubApplicationDTO> findSubApplication(@ApiParam(value = "应用组织Id", required = true) @RequestParam String groupId,
                                                         @ApiParam(value = "应用工件Id", required = true) @RequestParam String artifactId,
                                                         @ApiParam(value = "应用版本", required = true) @RequestParam String version) {
        return new ResponseMsg<>(applicationService.findSubApplication(groupId, artifactId, version));
    }


    @ApiOperation("新增/更新应用")
    @PostMapping("/saveApplication")
    @Authorized(roles = {"BU PO"})
    public ResponseMsg<Boolean> saveApplication(@RequestBody SaveApplicationDTO saveApplicationDTO) {
        Boolean result = applicationService.saveApplication(saveApplicationDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("向应用中增加组件")
    @PostMapping("/saveApplicationComponent")
    @Authorized(roles = {"APP Leader","APP Member"})
    public ResponseMsg<Boolean> saveApplicationComponent(@RequestBody ApplicationComponentDTO applicationComponentDTO) {
        Boolean result = applicationService.saveApplicationComponent(applicationComponentDTO);
        return new ResponseMsg<>(result);
    }


    @ApiOperation("删除应用中的组件")
    @PostMapping("/deleteApplicationComponent")
    @Authorized(roles = {"BU Rep"})
    public ResponseMsg<Boolean> deleteApplicationComponent(@RequestBody ApplicationComponentDTO applicationComponentDTO) {
        Boolean result = applicationService.deleteApplicationComponent(applicationComponentDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("新增/更新应用依赖信息")
    @PostMapping("/saveApplicationDependency")
    @Authorized(roles = {"App Leader", "App Member"})
    public ResponseMsg<Void> saveApplicationDependency(@RequestBody SaveApplicationDependencyDTO saveApplicationDependencyDTO) {
        applicationService.changeApplicationState(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
        applicationService.saveApplicationDependency(saveApplicationDependencyDTO);
        return new ResponseMsg<>();
    }


    @ApiOperation("升级应用")
    @PostMapping("/upgradeApplication")
    @Authorized(roles= {"BU PO"})
    public ResponseMsg<Boolean> upgradeApplication(@RequestBody UpgradeApplicationDTO upgradeApplicationDTO) {
        Boolean result = applicationService.upgradeApplication(upgradeApplicationDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("删除应用某个版本")
    @PostMapping("/deleteApplicationVersion")
    @Authorized(roles = {"BU Rep"})
    public ResponseMsg<Boolean> deleteApplicationVersion(@RequestBody DeleteApplicationDTO deleteApplicationDTO) {
        return new ResponseMsg<>(applicationService.deleteApplicationVersion(deleteApplicationDTO));
    }


    @ApiOperation("分页获取指定应用的版本信息")
    @GetMapping("/findApplicationVersionPage")
    public ResponseMsg<Page<ApplicationDO>> findApplicationVersionPage(@ApiParam(value = "组件Id", required = true) @RequestParam String groupId,
                                                                   @ApiParam(value = "工件Id", required = true) @RequestParam String artifactId,
                                                                   @ApiParam(value = "页码", required = true) @RequestParam int number,
                                                                   @ApiParam(value = "页大小", required = true) @RequestParam int size) {
        return new ResponseMsg<>(applicationService.findApplicationVersionPage(groupId, artifactId, number, size));
    }

    @ApiOperation("检查指定应用扫描中组件的个数")
    @GetMapping("/checkRunningApplication")
    public ResponseMsg<Integer> checkRunningApplication(@ApiParam(value = "应用名称", required = true) @RequestParam String groupId, @RequestParam String artifactId) {
        return new ResponseMsg<>(applicationService.checkRunningApplication(groupId, artifactId));
    }

    @ApiOperation("获取指定应用的所有版本列表")
    @GetMapping("/getVersionsList")
    public ResponseMsg<List<String>> getVersionsList(@ApiParam(value = "组织Id", required = true) @RequestParam String groupId, @ApiParam(value = "工件Id", required = true) @RequestParam String artifactId) {
        return new ResponseMsg<>(applicationService.getVersionsList(groupId, artifactId));
    }

    @ApiOperation("获取指定应用指定版本的详细信息")
    @PostMapping("/findApplicationVersionInfo")
    public ResponseMsg<ApplicationDO> findApplicationVersionInfo(@RequestBody ApplicationSearchDTO dto) {
        return new ResponseMsg<>(applicationService.findApplicationVersionInfo(dto));
    }

    @ApiOperation("查询应用依赖树信息")
    @PostMapping("/findApplicationDependencyTree")
    public ResponseMsg<DependencyTreeDO> findApplicationDependencyTree(@RequestBody ApplicationSearchDTO dto) {
        return new ResponseMsg<>(applicationService.findApplicationDependencyTree(dto));
    }

    @ApiOperation("分页查询应用依赖平铺信息")
    @PostMapping("/findApplicationDependencyTable")
    public ResponseMsg<Page<ComponentTableDTO>> findApplicationDependencyTable(@RequestBody ApplicationSearchPageDTO dto) {
        return new ResponseMsg<>(applicationService.findApplicationDependencyTable(dto));
    }

    @ApiOperation("导出应用依赖平铺信息（简明）Excel")
    @PostMapping("/exportTableExcelBrief")
    public void exportTableExcelBrief(@RequestBody ApplicationSearchDTO dto, HttpServletResponse response) {
        applicationService.exportTableExcelBrief(dto, response);
    }

    @ApiOperation("导出应用依赖平铺信息（详细）Excel")
    @PostMapping("/exportTableExcelDetail")
    public void exportTableExcelDetail(@RequestBody ApplicationSearchDTO dto, HttpServletResponse response) {
        applicationService.exportTableExcelDetail(dto, response);
    }

    @ApiOperation("生成应用版本对比树")
    @PostMapping("/getApplicationVersionCompareTree")
    public ResponseMsg<VersionCompareTreeDTO> getapplicationVersionCompareTree(@RequestBody VersionCompareReqDTO dto) {
        return new ResponseMsg<>(applicationService.getApplicationVersionCompareTree(dto));
    }

    @ApiOperation("改变应用锁定状态")
    @PostMapping("/changeLockState")
    @Authorized(roles = {"Bu Rep"})
    public ResponseMsg<Void> changeLockState(@ApiParam(value = "组织Id", required = true) @RequestParam String groupId, @ApiParam(value = "工件Id", required = true) @RequestParam String artifactId, @ApiParam(value = "版本号", required = true) @RequestParam String version) {
        applicationService.changeLockState(groupId, artifactId, version);
        return new ResponseMsg<>(null);
    }

    @ApiOperation("改变应用发布状态")
    @PostMapping("/changeReleaseState")
    @Authorized(roles = {"Bu Rep"})
    public ResponseMsg<Void> changeReleaseState(@RequestBody ChangeReleaseStateDTO changeReleaseStateDTO) {
        applicationService.changeReleaseState(changeReleaseStateDTO);
        return new ResponseMsg<>(null);
    }
}
