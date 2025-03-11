package nju.edu.cn.qysca.controller.application;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.application.dtos.*;
import nju.edu.cn.qysca.service.application.ApplicationService;
import nju.edu.cn.qysca.service.report.ReportService;
import nju.edu.cn.qysca.service.sbom.SBOMService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


@Api(tags = "应用管理")
@RestController
@RequestMapping("qysca/application")
public class ApplicationController {

    @Autowired
    private ApplicationService applicationService;

    @Autowired
    private SBOMService sbomService;
    @Autowired
    private ReportService reportService;


    @ApiOperation("分页获取应用信息")
    @GetMapping("/findApplicationPage")
    @PreAuthorize("@my.checkAuth('/qysca/application/findApplicationPage')")
    public ResponseMsg<Page<ApplicationDO>> findApplicationPage(
            @ApiParam(value = "页码", required = true) @RequestParam int number,
            @ApiParam(value = "页大小", required = true) @RequestParam int size) {
        return new ResponseMsg<>(applicationService.findApplicationPage(number, size));
    }

    @ApiOperation("模糊查询应用名称")
    @GetMapping("/searchApplicationName")
    @PreAuthorize("@my.checkAuth('/qysca/application/searchApplicationName')")
    public ResponseMsg<List<String>> searchApplicationName(@ApiParam(value = "应用名称", required = true) @RequestParam String name) {
        return new ResponseMsg<>(applicationService.searchApplicationName(name));
    }


    @ApiOperation("根据名称查询应用 并返回应用的最新版本")
    @GetMapping("/findApplication")
    @PreAuthorize("@my.checkAuth('/qysca/application/findApplication')")
    public ResponseMsg<ApplicationDO> findApplication(@ApiParam(value = "应用名称", required = true) @RequestParam String name) {
        return new ResponseMsg<>(applicationService.findApplication(name));
    }

    @ApiOperation("查询子应用和子组件")
    @GetMapping("/findSubApplication")
    @PreAuthorize("@my.checkAuth('/qysca/application/findSubApplication')")
    public ResponseMsg<SubApplicationDTO> findSubApplication(@ApiParam(value = "应用名称", required = true) @RequestParam String name,
                                                         @ApiParam(value = "应用版本", required = true) @RequestParam String version) {
        return new ResponseMsg<>(applicationService.findSubApplication(name, version));
    }


    @ApiOperation("新增/更新应用")
    @PostMapping("/saveApplication")
    @PreAuthorize("@my.checkAuth('/qysca/application/saveApplication')")
    public ResponseMsg<Boolean> saveApplication(@RequestBody SaveApplicationDTO saveApplicationDTO) {
        Boolean result = applicationService.saveApplication(saveApplicationDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("向应用中增加组件")
    @PostMapping("/saveApplicationComponent")
    @PreAuthorize("@my.checkAuth('/qysca/application/saveApplicationComponent')")
    public ResponseMsg<Boolean> saveApplicationComponent(@RequestBody ApplicationComponentDTO applicationComponentDTO) {
        Boolean result = applicationService.saveApplicationComponent(applicationComponentDTO);
        return new ResponseMsg<>(result);
    }


    @ApiOperation("删除应用中的组件")
    @PostMapping("/deleteApplicationComponent")
    @PreAuthorize("@my.checkAuth('/qysca/application/deleteApplicationComponent')")
    public ResponseMsg<Boolean> deleteApplicationComponent(@RequestBody ApplicationComponentDTO applicationComponentDTO) {
        Boolean result = applicationService.deleteApplicationComponent(applicationComponentDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("新增/更新应用依赖信息")
    @PostMapping("/saveApplicationDependency")
    @PreAuthorize("@my.checkAuth('/qysca/application/saveApplicationDependency')")
    public ResponseMsg<Void> saveApplicationDependency(@RequestBody SaveApplicationDependencyDTO saveApplicationDependencyDTO) {
        applicationService.changeApplicationState(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion(), "RUNNING");
        applicationService.saveApplicationDependency(saveApplicationDependencyDTO);
        return new ResponseMsg<>();
    }


    @ApiOperation("升级应用")
    @PostMapping("/upgradeApplication")
    @PreAuthorize("@my.checkAuth('/qysca/application/upgradeApplication')")
    public ResponseMsg<Boolean> upgradeApplication(@RequestBody UpgradeApplicationDTO upgradeApplicationDTO) {
        Boolean result = applicationService.upgradeApplication(upgradeApplicationDTO);
        return new ResponseMsg<>(result);
    }

    @ApiOperation("删除应用某个版本")
    @PostMapping("/deleteApplicationVersion")
    @PreAuthorize("@my.checkAuth('/qysca/application/deleteApplicationVersion')")
    public ResponseMsg<List<ApplicationDO>> deleteApplicationVersion(@RequestBody DeleteApplicationDTO deleteApplicationDTO) {
        return new ResponseMsg<>(applicationService.deleteApplicationVersion(deleteApplicationDTO));
    }


    @ApiOperation("获取指定应用的所有版本列表")
    @GetMapping("/getVersionsList")
    @PreAuthorize("@my.checkAuth('/qysca/application/getVersionsList')")
    public ResponseMsg<List<String>> getVersionsList(@ApiParam(value = "应用名称", required = true) @RequestParam String name) {
        return new ResponseMsg<>(applicationService.getVersionsList(name));
    }

    @ApiOperation("获取指定应用指定版本的详细信息")
    @PostMapping("/findApplicationVersionInfo")
    @PreAuthorize("@my.checkAuth('/qysca/application/findApplicationVersionInfo')")
    public ResponseMsg<ApplicationDetailDTO> findApplicationVersionInfo(@RequestBody ApplicationSearchDTO dto) {
        return new ResponseMsg<>(applicationService.findApplicationVersionInfo(dto));
    }

    @ApiOperation("查询应用依赖树信息")
    @PostMapping("/findApplicationDependencyTree")
    @PreAuthorize("@my.checkAuth('/qysca/application/findApplicationDependencyTree')")
    public ResponseMsg<AppDependencyTreeDO> findApplicationDependencyTree(@RequestBody ApplicationSearchDTO dto) {
        return new ResponseMsg<>(applicationService.findApplicationDependencyTree(dto));
    }

    @ApiOperation("分页查询应用依赖平铺信息")
    @PostMapping("/findApplicationDependencyTable")
    @PreAuthorize("@my.checkAuth('/qysca/application/findApplicationDependencyTable')")
    public ResponseMsg<Page<ComponentTableDTO>> findApplicationDependencyTable(@RequestBody ApplicationSearchPageDTO dto) {
        return new ResponseMsg<>(applicationService.findApplicationDependencyTable(dto));
    }

    @ApiOperation("导出应用依赖平铺信息（简明）Excel")
    @PostMapping("/exportTableExcelBrief")
    @PreAuthorize("@my.checkAuth('/qysca/application/exportTableExcelBrief')")
    public void exportTableExcelBrief(@RequestBody ApplicationSearchDTO dto, HttpServletResponse response) {
        applicationService.exportTableExcelBrief(dto, response);
    }

    @ApiOperation("导出应用依赖平铺信息（详细）Excel")
    @PostMapping("/exportTableExcelDetail")
    @PreAuthorize("@my.checkAuth('/qysca/application/exportTableExcelDetail')")
    public void exportTableExcelDetail(@RequestBody ApplicationSearchDTO dto, HttpServletResponse response) {
        applicationService.exportTableExcelDetail(dto, response);
    }

    @ApiOperation("生成应用版本对比树")
    @PostMapping("/getApplicationVersionCompareTree")
    @PreAuthorize("@my.checkAuth('/qysca/application/getApplicationVersionCompareTree')")
    public ResponseMsg<VersionCompareTreeDTO> getApplicationVersionCompareTree(@RequestBody VersionCompareReqDTO dto) {
        return new ResponseMsg<>(applicationService.getApplicationVersionCompareTree(dto));
    }

    @ApiOperation("改变应用锁定状态")
    @PostMapping("/changeLockState")
    @PreAuthorize("@my.checkAuth('/qysca/application/changeLockState')")
    public ResponseMsg<Void> changeLockState(@ApiParam(value = "应用名称", required = true) @RequestParam String name, @ApiParam(value = "版本号", required = true) @RequestParam String version) {
        applicationService.changeLockState(name, version);
        return new ResponseMsg<>(null);
    }

    @ApiOperation("改变应用发布状态")
    @PostMapping("/changeReleaseState")
    @PreAuthorize("@my.checkAuth('/qysca/application/changeReleaseState')")
    public ResponseMsg<List<ApplicationDO>> changeReleaseState(@RequestBody ChangeReleaseStateDTO changeReleaseStateDTO) {
        return new ResponseMsg<>(applicationService.changeReleaseState(changeReleaseStateDTO));
    }

    @ApiOperation("导出应用SBOM")
    @PostMapping("/exportSBOM")
    @PreAuthorize("@my.checkAuth('/qysca/application/exportSBOM')")
    public void exportSBOM(@RequestBody ApplicationSearchDTO dto, HttpServletResponse response){
        sbomService.exportSBOM(dto, response);
    }

    @ApiOperation("导出应用html报告")
    @PostMapping("/exportHtml")
    @PreAuthorize("@my.checkAuth('/qysca/application/exportHtml')")
    public void exportHtml(@RequestBody ApplicationSearchDTO dto, HttpServletResponse response){
        reportService.exportHtml(dto, response);
    }
}
