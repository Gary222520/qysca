package nju.edu.cn.qysca.domain.bu.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "部门成员信息")
public class BuMemberDTO {
    @ApiModelProperty(value = "部门名称", example = "Software Development Department")
    private String buName;

    @ApiModelProperty(value = "部门成员编号", example = "000000000")
    private String uid;
}
