package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("创建应用DTO")
public class CreateApplicationDTO {

    @ApiModelProperty(value = "组织id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "sca system")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "应用名称", example = "sca system")
    private String name;

    @ApiModelProperty(value = "应用描述", example = "A system designed for software composition analysis")
    private String description;

}
