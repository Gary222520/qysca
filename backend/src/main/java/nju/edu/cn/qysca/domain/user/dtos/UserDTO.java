package nju.edu.cn.qysca.domain.user.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户登录DTO")
public class UserDTO {

    @ApiModelProperty(value = "用户编号", example = "201258794")
    private String uid;

    @ApiModelProperty(value = "用户密码", example = "2724fhfihwiwhHH")
    private String password;
}
