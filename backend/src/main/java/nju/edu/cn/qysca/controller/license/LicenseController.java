package nju.edu.cn.qysca.controller.license;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseBriefDTO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseConflictInfoDTO;
import nju.edu.cn.qysca.service.license.LicenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Api(tags= "许可证管理")
@RestController
@RequestMapping("qysca/license")
public class LicenseController {

    @Autowired
    private LicenseService licenseService;

    @ApiOperation("获取某个应用的许可证列表")
    @GetMapping("/getAppLicense")
    public ResponseMsg<Page<LicenseBriefDTO>> getAppLicense(@RequestParam String name, @RequestParam String version, @RequestParam  int page, @RequestParam int size){
        return new ResponseMsg<>(licenseService.getAppLicense(name, version, page, size));
    }

    @ApiOperation("查看组件的许可证列表")
    @GetMapping("/getComponentLicense")
    public ResponseMsg<Page<LicenseBriefDTO>> getComponentLicense(@RequestParam String name, @RequestParam String version, @RequestParam String language, @RequestParam int page, @RequestParam int size) {
        return new ResponseMsg<>(licenseService.getComponentLicense(name, version, language, page, size));
    }

    @ApiOperation("在某个应用中增加许可证")
    @PostMapping("/addAppLicense")
    public ResponseMsg<Boolean> addAppLicense(@RequestParam String name, @RequestParam String version, @RequestParam String licenseName){
        return new ResponseMsg<>(licenseService.addAppLicense(name, version, licenseName));
    }

    @ApiOperation("删除某个应用许可证")
    @PostMapping("/deleteAppLicense")
    public ResponseMsg<Boolean> deleteAppLicense(@RequestParam String name, @RequestParam String version, @RequestParam String licenseName){
        return new ResponseMsg<>(licenseService.deleteAppLicense(name, version, licenseName));
    }

    @ApiOperation("查看许可证详细内容")
    @GetMapping("/getLicenseInfo")
    public ResponseMsg<LicenseDO> getLicenseInfo(@RequestParam String licenseName){
        return new ResponseMsg<>(licenseService.getLicenseInfo(licenseName));
    }

    @ApiOperation("许可证库界面查看许可证列表")
    @GetMapping("/getLicensePage")
    public ResponseMsg<Page<LicenseBriefDTO>> getLicensePage(@RequestParam String name, @RequestParam int page, @RequestParam int size) {
        return new ResponseMsg<>(licenseService.getLicensePage(name, page, size));
    }

    @ApiOperation("查看应用的许可证冲突信息")
    @GetMapping("/getAppLicenseConflict")
    public ResponseMsg<LicenseConflictInfoDTO> getAppLicenseConflict(@RequestParam String name, @RequestParam String version) {
        return new ResponseMsg<>(licenseService.getAppLicenseConflictInformation(name, version));
    }

    @ApiOperation("查看组件的许可证冲突信息")
    @GetMapping("/getComponentLicenseConflict")
    public ResponseMsg<LicenseConflictInfoDTO> getComponentLicenseConflict(@RequestParam String name, @RequestParam String version, @RequestParam String language) {
        return new ResponseMsg<>(licenseService.getComponentLicenseConflictInformation(name, version, language));
    }

}
