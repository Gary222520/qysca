package nju.edu.cn.qysca.controller.user;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import nju.edu.cn.qysca.controller.ResponseMsg;
import nju.edu.cn.qysca.domain.user.dtos.ApplicationMemberDTO;
import nju.edu.cn.qysca.service.user.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(tags = "用户权限管理")
@RestController
@RequestMapping("qysca/role")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @ApiOperation("向应用中增加成员")
    @PostMapping("/addMember")
    public ResponseMsg<Void> addMember(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.addMember(applicationMemberDTO);
        return new ResponseMsg<>();
    }


    @ApiOperation("在应用中删除成员")
    @PostMapping("/deleteMember")
    public ResponseMsg<Void> deleteMember(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.deleteMember(applicationMemberDTO);
        return new ResponseMsg<>();
    }


    @ApiOperation("在应用中增加leader")
    @PostMapping("/addLeader")
    public ResponseMsg<Void> addLeader(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.addLeader(applicationMemberDTO);
        return new ResponseMsg<>();
    }

    @ApiOperation("在应用中删除leader")
    @PostMapping("/deleteLeader")
    public ResponseMsg<Void> deleteLeader(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.deleteLeader(applicationMemberDTO);
        return new ResponseMsg<>();
    }

    @ApiOperation("在应用中增加Bu PO")
    @PostMapping("/addBuPO")
    public ResponseMsg<Void> addBuPO(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.addBuPO(applicationMemberDTO);
        return new ResponseMsg<>();
    }
    @ApiOperation("在应用中删除BU PO")
    @PostMapping("/deleteBuPO")
    public ResponseMsg<Void> deleteBuPO(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.deleteBuPO(applicationMemberDTO);
        return new ResponseMsg<>();
    }

    @ApiOperation("在应用中增加BU Rep")
    @PostMapping("/addBuRep")
    public ResponseMsg<Void> addBuRep(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.addBuRep(applicationMemberDTO);
        return new ResponseMsg<>();
    }

    @ApiOperation("在应用中删除BU Rep")
    @PostMapping("/deleteBuRep")
    public ResponseMsg<Void> deleteBuRep(ApplicationMemberDTO applicationMemberDTO) {
        userRoleService.deleteBuRep(applicationMemberDTO);
        return new ResponseMsg<>();
    }
}
