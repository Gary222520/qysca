package nju.edu.cn.qysca.controller.component;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api(tags = "test")
@RestController
@RequestMapping("qysca/test")
public class TestController {
    @Autowired
    ComponentDao componentDao;

    @ApiOperation("测试数据库查询")
    @GetMapping("/findAll")
    public ResponseMsg<List<ComponentDO>> findAll() {
        List<ComponentDO> ans=componentDao.findAll();
        return new ResponseMsg<>(ans);
    }
}
