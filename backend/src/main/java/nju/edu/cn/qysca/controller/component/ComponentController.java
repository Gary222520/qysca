package nju.edu.cn.qysca.controller.component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.auth.Authorized;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import nju.edu.cn.qysca.service.component.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "组件管理")
@RestController
@RequestMapping("qysca/components")
public class ComponentController {
    @Autowired
    ComponentService componentService;

    @ApiOperation("分页查询组件")
    @PostMapping("/findComponentsPage")
    public ResponseMsg<Page<ComponentDO>> findComponentsPage(@RequestBody ComponentSearchDTO dto) {
        return new ResponseMsg<>(componentService.findComponentsPage(dto));
    }

    @ApiOperation("新增闭源组件")
    @PostMapping("/saveCloseComponent")
    @Authorized(roles = {"App Leader", "App Member"})
    public ResponseMsg<Boolean> saveCloseComponent(@RequestBody SaveCloseComponentDTO dto) {
        componentService.saveCloseComponent(dto);
        return new ResponseMsg<>(Boolean.TRUE);
    }

    @ApiOperation("查询组件依赖树信息")
    @PostMapping("/findComponentDependencyTree")
    public ResponseMsg<DependencyTreeDO> findComponentDependencyTree(@RequestBody ComponentGavDTO dto) {
        return new ResponseMsg<>(componentService.findComponentDependencyTree(dto));
    }


    @ApiOperation("分页查询组件依赖平铺信息")
    @PostMapping("/findComponentDependencyTable")
    public ResponseMsg<Page<ComponentTableDTO>> findComponentDependencyTable(@RequestBody ComponentGavPageDTO dto) {
        return new ResponseMsg<>(componentService.findComponentDependencyTable(dto));
    }

    @ApiOperation("查询指定组件的详细信息")
    @PostMapping("/findComponentDetail")
    public ResponseMsg<ComponentDetailDTO> findComponentDetail(@RequestBody ComponentGavDTO dto) {
        return new ResponseMsg<>(componentService.findComponentDetail(dto));
    }
}
