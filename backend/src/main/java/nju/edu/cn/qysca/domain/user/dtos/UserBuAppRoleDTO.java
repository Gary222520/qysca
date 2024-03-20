package nju.edu.cn.qysca.domain.user.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户部门应用角色信息DTO")
public class UserBuAppRoleDTO {
    @ApiModelProperty(value = "部门名称",example = "business")
    private String buName;

    @ApiModelProperty(value = "应用名称",example = "qysca")
    private String appName;

    @ApiModelProperty(value = "应用版本",example = "1.0.0")
    private String appVersion;

    @ApiModelProperty(value = "角色",example = "App Leader")
    private String role;
}
