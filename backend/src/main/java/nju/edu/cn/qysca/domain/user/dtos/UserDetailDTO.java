package nju.edu.cn.qysca.domain.user.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.user.dos.UserDO;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户详细信息DTO")
public class UserDetailDTO {
    @ApiModelProperty(value = "用户信息")
    private UserDO user;

    @ApiModelProperty(value = "用户部门应用角色信息")
    private List<UserBuAppRoleDTO> userBuAppRoles;
}
