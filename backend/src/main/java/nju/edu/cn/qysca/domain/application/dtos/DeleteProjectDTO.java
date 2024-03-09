package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "在应用中删除项目")
public class DeleteProjectDTO {

    @ApiModelProperty(value = "应用组id", example = "nju.edu.cn")
    private String appGroupId;

    @ApiModelProperty(value = "应用artifactId", example = "qysca")
    private String appArtifactId;

    @ApiModelProperty(value = "应用版本", example = "1.0.0")
    private String appVersion;

    @ApiModelProperty(value = "项目组id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "项目artifactId", example = "qysca-project")
    private String artifactId;

    @ApiModelProperty(value = "项目版本", example = "1.0.0")
    private String version;
}
