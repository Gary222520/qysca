package nju.edu.cn.qysca.domain.user.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "用户简要信息")
public class UserBriefDTO {

    @ApiModelProperty(value = "用户编号", example = "000000000")
    private String uid;

    @ApiModelProperty(value = "用户名称", example = "张三")
    private String name;
}
