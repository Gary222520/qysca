package nju.edu.cn.qysca.controller.components;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.components.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.components.ComponentGavDTO;
import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import nju.edu.cn.qysca.service.components.ComponentsService;
import nju.edu.cn.qysca.service.example.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"组件管理"})
@RestController
@RequestMapping("qysca/components")
public class ComponentsController {

    @Autowired
    ComponentsService componentsService;


    @PostMapping("/showTree")
    @ApiOperation(value = "展示树形结构")
    public ResponseMsg<ComponentDependencyTreeDO> showTree(@RequestBody ComponentGavDTO componentGavDTO) {
        return new ResponseMsg<>(componentsService.getComponentTreeByGAV(componentGavDTO));
    }
}
