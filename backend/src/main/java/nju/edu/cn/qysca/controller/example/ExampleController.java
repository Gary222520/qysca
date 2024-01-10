package nju.edu.cn.qysca.controller.example;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import nju.edu.cn.qysca.service.example.ExampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = {"示例"})
@RestController
@RequestMapping("qysca/example")
public class ExampleController {

    @Autowired
    ExampleService exampleService;


    @PostMapping("/addOne")
    @ApiOperation(value = "添加一条记录")
    public ResponseMsg addOne(@RequestBody JavaComponentDO javaComponentDO) {
        exampleService.addOne(javaComponentDO);
        return new ResponseMsg<>();
    }

    @GetMapping("/findAll")
    @ApiOperation(value = "查询所有记录")
    public ResponseMsg<List<JavaComponentDO>> findAll() {
        return new ResponseMsg<>(exampleService.findAll());
    }

}
