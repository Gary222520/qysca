package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "应用组件信息DTO")
public class AppComponentTableDTO {

    @ApiModelProperty(value = "组件名称", example = "log4j")
    private String cName;

    @ApiModelProperty(value = "组件版本", example = "1.2.1")
    private String cVersion;

    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @ApiModelProperty(value = "类型", example = "opensource")
    private String type;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "是否直接依赖", example = "true")
    private Boolean direct;
}
