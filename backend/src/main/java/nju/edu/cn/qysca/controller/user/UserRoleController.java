package nju.edu.cn.qysca.controller.user;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.bu.dtos.BuMemberDTO;
import nju.edu.cn.qysca.domain.user.dtos.ApplicationMemberDTO;
import nju.edu.cn.qysca.domain.bu.dtos.BuRepDTO;
import nju.edu.cn.qysca.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "用户权限管理")
@RestController
@RequestMapping("qysca/role")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @ApiOperation("向应用中增加成员")
    @PostMapping("/addAppMember")
    @PreAuthorize("@my.checkAuthAddAppMember(#applicationMemberDTO,'/qysca/role/addAppMember')")
    public ResponseMsg<Void> addAppMember(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.addMember(applicationMemberDTO);
        return new ResponseMsg<>();
    }


    @ApiOperation("在应用中删除成员")
    @PostMapping("/deleteAppMember")
    @PreAuthorize("@my.checkAuth('/qysca/role/deleteAppMember')")
    public ResponseMsg<Void> deleteAppMember(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.deleteMember(applicationMemberDTO);
        return new ResponseMsg<>();
    }


    @ApiOperation("在应用中增加Bu Rep")
    @PostMapping("/addBuRep")
    @PreAuthorize("@my.checkAuth('/qysca/role/addBuRep')")
    public ResponseMsg<Void> addBuRep(BuRepDTO buRepDTO) {
        userRoleService.addBuRep(buRepDTO);
        return new ResponseMsg<>();
    }

    @ApiOperation("在应用中删除BU Rep")
    @PostMapping("/deleteBuRep")
    @PreAuthorize("@my.checkAuth('/qysca/role/deleteBuRep')")
    public ResponseMsg<Void> deleteBuRep(BuRepDTO buRepDTO) {
        userRoleService.deleteBuRep(buRepDTO);
        return new ResponseMsg<>();
    }

    @ApiOperation("向部门中添加成员")
    @PostMapping("/addBuMember")
    @PreAuthorize("@my.checkAuth('/qysca/role/addBuMember')")
    public ResponseMsg<Void> addBuMember(BuMemberDTO buMemberDTO) {
        userRoleService.addBuMember(buMemberDTO);
        return new ResponseMsg<>();
    }

    @ApiOperation("列出部门所有成员")
    @PostMapping("/listBuMember")
    @PreAuthorize("@my.checkAuth('/qysca/role/listBuMember')")
    public ResponseMsg listBuMember(@RequestParam String name) {
        // TODO:列出部门所有成员
        return new ResponseMsg<>();
    }

    @ApiOperation("删除部门成员")
    @PostMapping("/deleteBuMember")
    @PreAuthorize("@my.checkAuth('/qysca/role/deleteBuMember')")
    public ResponseMsg deleteBuMember(@RequestParam  String name,
                                      @RequestParam  String uid){
        // TODO:删除部门成员
        return new ResponseMsg<>();
    }
}
