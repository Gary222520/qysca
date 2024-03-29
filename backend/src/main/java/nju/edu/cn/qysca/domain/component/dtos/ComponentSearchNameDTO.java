package nju.edu.cn.qysca.domain.component.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "组件名称模糊查询接口")
public class ComponentSearchNameDTO {


    @ApiModelProperty(value = "组件名称", example = "qysca-core")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;
}
