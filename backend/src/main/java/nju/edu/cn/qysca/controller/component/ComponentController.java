package nju.edu.cn.qysca.controller.component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.component.ComponentSearchDTO;
import nju.edu.cn.qysca.domain.component.JavaCloseComponentDO;
import nju.edu.cn.qysca.domain.component.JavaOpenComponentDO;
import nju.edu.cn.qysca.service.component.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Api(tags = "组件管理")
@RestController
@RequestMapping("qysca/components")
public class ComponentController {
    @Autowired
    ComponentService componentService;

    @ApiOperation("分页查询开源组件")
    @PostMapping("/findOpenComponentsPage")
    public ResponseMsg<Page<JavaOpenComponentDO>> findOpenComponentsPage(@RequestBody ComponentSearchDTO dto){
        return new ResponseMsg<>(componentService.findOpenComponentsPage(dto));
    }

    @ApiOperation("分页查询闭源组件")
    @PostMapping("/findCloseComponentsPage")
    public ResponseMsg<Page<JavaCloseComponentDO>> findCloseComponentsPage(@RequestBody ComponentSearchDTO dto){
        return new ResponseMsg<>(componentService.findCloseComponentsPage(dto));
    }

}
