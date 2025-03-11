package nju.edu.cn.qysca.domain.user.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "应用成员信息接口")
public class ApplicationMemberDTO {

    @ApiModelProperty(value = "应用名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "应用版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "用户id", example = "000000")
    private String uid;

    @ApiModelProperty(value = "角色", example = "APP Member")
    private String role;
}
