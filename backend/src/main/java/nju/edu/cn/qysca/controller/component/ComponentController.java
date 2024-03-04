//package nju.edu.cn.qysca.controller.component;
//
//import io.swagger.annotations.Api;
//import io.swagger.annotations.ApiOperation;
//import nju.edu.cn.qysca.controller.ResponseMsg;
//import nju.edu.cn.qysca.domain.component.dos.*;
//import nju.edu.cn.qysca.domain.component.dtos.*;
//import nju.edu.cn.qysca.service.component.ComponentService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.domain.Page;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@Api(tags = "组件管理")
//@RestController
//@RequestMapping("qysca/components")
//public class ComponentController {
//    @Autowired
//    ComponentService componentService;
//
//    @ApiOperation("分页查询开源组件")
//    @PostMapping("/findOpenComponentsPage")
//    public ResponseMsg<Page<JavaOpenComponentDO>> findOpenComponentsPage(@RequestBody ComponentSearchDTO dto) {
//        return new ResponseMsg<>(componentService.findOpenComponentsPage(dto));
//    }
//
//    @ApiOperation("分页查询闭源组件")
//    @PostMapping("/findCloseComponentsPage")
//    public ResponseMsg<Page<JavaCloseComponentDO>> findCloseComponentsPage(@RequestBody ComponentSearchDTO dto) {
//        return new ResponseMsg<>(componentService.findCloseComponentsPage(dto));
//    }
//
//    @ApiOperation("新增闭源组件")
//    @PostMapping("/saveCloseComponent")
//    public ResponseMsg<Boolean> saveCloseComponent(@RequestBody SaveCloseComponentDTO dto) {
//        componentService.saveCloseComponent(dto);
//        return new ResponseMsg<>(Boolean.TRUE);
//    }
//
//    @ApiOperation("查询开源组件依赖树信息")
//    @PostMapping("/findOpenComponentDependencyTree")
//    public ResponseMsg<JavaOpenDependencyTreeDO> findOpenComponentDependencyTree(@RequestBody ComponentGavDTO dto) {
//        return new ResponseMsg<>(componentService.findOpenComponentDependencyTree(dto));
//    }
//
//    @ApiOperation("查询闭源组件依赖树信息")
//    @PostMapping("/findCloseComponentDependencyTree")
//    public ResponseMsg<JavaCloseDependencyTreeDO> findCloseComponentDependencyTree(@RequestBody ComponentGavDTO dto) {
//        return new ResponseMsg<>(componentService.findCloseComponentDependencyTree(dto));
//    }
//
//    @ApiOperation("分页查询开源组件依赖平铺信息")
//    @PostMapping("/findOpenComponentDependencyTable")
//    public ResponseMsg<Page<ComponentTableDTO>> findOpenComponentDependencyTable(@RequestBody ComponentGavPageDTO dto) {
//        return new ResponseMsg<>(componentService.findOpenComponentDependencyTable(dto));
//    }
//
//    @ApiOperation("分页查询闭源组件依赖平铺信息")
//    @PostMapping("/findCloseComponentDependencyTable")
//    public ResponseMsg<Page<ComponentTableDTO>> findCloseComponentDependencyTable(@RequestBody ComponentGavPageDTO dto) {
//        return new ResponseMsg<>(componentService.findCloseComponentDependencyTable(dto));
//    }
//
//    @ApiOperation("查询指定开源组件的详细信息")
//    @PostMapping("/findOpenComponentDetail")
//    public ResponseMsg<ComponentDetailDTO> findOpenComponentDetail(@RequestBody ComponentGavDTO dto) {
//        return new ResponseMsg<>(componentService.findOpenComponentDetail(dto));
//    }
//
//    @ApiOperation("查询指定闭源组件的详细信息")
//    @PostMapping("/findCloseComponentDetail")
//    public ResponseMsg<ComponentDetailDTO> findCloseComponentDetail(@RequestBody ComponentGavDTO dto) {
//        return new ResponseMsg<>(componentService.findCloseComponentDetail(dto));
//    }
//}