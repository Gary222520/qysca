package nju.edu.cn.qysca.controller.example;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.example.ExampleDO;
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
    public ResponseMsg<ExampleDO> addOne(@RequestBody ExampleDO exampleDO) {
        return new ResponseMsg<>(exampleService.addOne(exampleDO));
    }


    @GetMapping("/deleteOne")
    @ApiOperation(value = "删除一条记录")
    public ResponseMsg deleteOne(@ApiParam(value = "自动编号", required = true) @RequestParam String uuid) {
        exampleService.deleteOne(uuid);
        return new ResponseMsg<>();
    }


    @PostMapping("/updateOne")
    @ApiOperation(value = "更新一条记录")
    public ResponseMsg<ExampleDO> updateOne(@RequestBody ExampleDO exampleDO) {
        return new ResponseMsg<>(exampleService.updateOne(exampleDO));
    }


    @GetMapping("/findAll")
    @ApiOperation(value = "查询所有记录")
    public ResponseMsg<List<ExampleDO>> findAll() {
        return new ResponseMsg<>(exampleService.findAll());
    }


    @GetMapping("/findByInfo")
    @ApiOperation(value = "根据info查询记录")
    public ResponseMsg<List<ExampleDO>> findByInfo(@ApiParam(value = "信息", required = true) @RequestParam String info) {
        return new ResponseMsg<>(exampleService.findByInfo(info));
    }


    @GetMapping("/findByRule")
    @ApiOperation(value = "根据规则查询记录（number<=5且info中不含有特定字符串string）")
    public ResponseMsg<List<ExampleDO>> findByRule(@ApiParam(value = "特定字符串", required = true) @RequestParam String string) {
        return new ResponseMsg<>(exampleService.findByRule(string));
    }
}
