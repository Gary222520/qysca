package nju.edu.cn.qysca.controller.bu;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.service.bu.BuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "部门管理")
@RestController
@RequestMapping("qysca/bu")
public class BuController {
    @Autowired
    private BuService buService;

    @ApiOperation("新增部门")
    @GetMapping("/addBu")
    @PreAuthorize("@my.checkAuth('/qysca/bu/addBu')")
    public ResponseMsg addBu(@RequestParam String bid, @RequestParam String name) {
        buService.createBu(bid, name);
        return new ResponseMsg<>();
    }

    @ApiOperation("删除部门")
    @GetMapping("/deleteBu")
    @PreAuthorize("@my.checkAuth('/qysca/bu/deleteBu')")
    public ResponseMsg deleteBu(@RequestParam String name) {
        buService.deleteBu(name);
        return new ResponseMsg<>();
    }


    @ApiOperation("查询部门列表")
    @GetMapping("/listAllBu")
    @PreAuthorize("@my.checkAuth('/qysca/bu/listAllBu')")
    public ResponseMsg<List<BuDO>> listAllBu() {
        return new ResponseMsg<>(buService.listAllBu());
    }
}
