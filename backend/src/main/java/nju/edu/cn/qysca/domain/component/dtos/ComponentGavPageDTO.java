package nju.edu.cn.qysca.domain.component.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "带分页组件GAV信息DTO")
public class ComponentGavPageDTO {

    @ApiModelProperty(value = "名称", example = "org.springframework.boot")
    private String name;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer number;

    @ApiModelProperty(value = "页大小", example = "10")
    private Integer size;
}
