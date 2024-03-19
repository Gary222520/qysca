package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "带分页应用版本信息搜索DTO")
public class ApplicationSearchPageDTO {
    @ApiModelProperty(value = "应用名称", example = "nju.edu.cn")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer number;

    @ApiModelProperty(value = "页大小", example = "10")
    private Integer size;
}
