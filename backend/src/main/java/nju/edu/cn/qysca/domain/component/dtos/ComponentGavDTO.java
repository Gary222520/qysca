package nju.edu.cn.qysca.domain.component.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "组件GAV信息DTO")
public class ComponentGavDTO {

    @ApiModelProperty(value = "名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;
}
