package nju.edu.cn.qysca.controller.component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.components.SaveCloseComponentDTO;
import nju.edu.cn.qysca.service.component.ComponentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "组件管理")
@RestController
@RequestMapping("qysca/component")
public class ComponentController {

    @Autowired
    private ComponentService componentService;

    @ApiOperation("闭源组件保存")
    @PostMapping("/save")
    public ResponseMsg<Boolean> saveCloseComponent(@RequestBody SaveCloseComponentDTO saveCloseComponentDTO) {
        componentService.saveCloseComponent(saveCloseComponentDTO);
        return new ResponseMsg<>(Boolean.TRUE);
    }
}
