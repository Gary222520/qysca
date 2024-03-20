package nju.edu.cn.qysca.controller.component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import nju.edu.cn.qysca.service.component.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "组件管理")
@RestController
@RequestMapping("qysca/component")
public class ComponentController {
    @Autowired
    ComponentService componentService;

    @ApiOperation("分页查询组件")
    @PostMapping("/findComponentsPage")
    @PreAuthorize("@my.checkAuth('/qysca/component/findComponentsPage')")
    public ResponseMsg<Page<ComponentDO>> findComponentsPage(@RequestBody ComponentSearchDTO dto) {
        return new ResponseMsg<>(componentService.findComponentsPage(dto));
    }

    @ApiOperation("模糊查询组件名称")
    @GetMapping("/searchComponentName")
    @PreAuthorize("@my.checkAuth('/qysca/component/searchComponentName')")
    public ResponseMsg<List<ComponentSearchNameDTO>>  searchComponentName(@RequestParam String name) {
        return new ResponseMsg<>(componentService.searchComponentName(name));
    }

    @ApiOperation("新增闭源组件")
    @PostMapping("/saveCloseComponent")
    @PreAuthorize("@my.checkAuth('/qysca/component/saveCloseComponent')")
    public ResponseMsg<Boolean> saveCloseComponent(@RequestBody SaveCloseComponentDTO dto) {
        componentService.saveCloseComponent(dto);
        return new ResponseMsg<>(Boolean.TRUE);
    }

    @ApiOperation("修改闭源组件")
    @PostMapping("/updateCloseComponent")
    @PreAuthorize("@my.checkAuth('/qysca/component/updateCloseComponent')")
    public ResponseMsg<Boolean> updateCloseComponent(@RequestBody UpdateCloseComponentDTO dto) {
        componentService.updateCloseComponent(dto);
        return new ResponseMsg<>(Boolean.TRUE);
    }

    @ApiOperation("删除闭源组件")
    @PostMapping("/deleteCloseComponent")
    @PreAuthorize("@my.checkAuth('/qysca/component/deleteCloseComponent')")
    public ResponseMsg<List<ApplicationDO>> deleteCloseComponent(@RequestBody DeleteCloseComponentDTO deleteCloseComponentDTO) {
        return new ResponseMsg<>(componentService.deleteCloseComponent(deleteCloseComponentDTO));
    }

    @ApiOperation("查询组件依赖树信息")
    @PostMapping("/findComponentDependencyTree")
    @PreAuthorize("@my.checkAuth('/qysca/component/findComponentDependencyTree')")
    public ResponseMsg<DependencyTreeDO> findComponentDependencyTree(@RequestBody ComponentGavDTO dto) {
        return new ResponseMsg<>(componentService.findComponentDependencyTree(dto));
    }


    @ApiOperation("分页查询组件依赖平铺信息")
    @PostMapping("/findComponentDependencyTable")
    @PreAuthorize("@my.checkAuth('/qysca/component/findComponentDependencyTable')")
    public ResponseMsg<Page<ComponentTableDTO>> findComponentDependencyTable(@RequestBody ComponentGavPageDTO dto) {
        return new ResponseMsg<>(componentService.findComponentDependencyTable(dto));
    }

    @ApiOperation("查询指定组件的详细信息")
    @PostMapping("/findComponentDetail")
    @PreAuthorize("@my.checkAuth('/qysca/component/findComponentDetail')")
    public ResponseMsg<ComponentDetailDTO> findComponentDetail(@RequestBody ComponentGavDTO dto) {
        return new ResponseMsg<>(componentService.findComponentDetail(dto));
    }
}
