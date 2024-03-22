package nju.edu.cn.qysca.domain.bu.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "部门代表信息")
public class BuRepDTO {

    @ApiModelProperty(value = "部门名称", example = "Software Development Department")
    private String buName;

    @ApiModelProperty(value = "部门代表编号", example = "000000000")
    private String uid;
}
