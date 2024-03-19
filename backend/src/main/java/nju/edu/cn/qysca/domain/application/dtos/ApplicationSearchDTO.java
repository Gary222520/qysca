package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "应用版本信息搜索DTO")
public class ApplicationSearchDTO {

    @ApiModelProperty(value = "名称", example = "app")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;
}
