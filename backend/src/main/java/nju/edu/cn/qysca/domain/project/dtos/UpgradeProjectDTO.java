package nju.edu.cn.qysca.domain.project.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("升级项目接口信息DTO")
public class UpgradeProjectDTO {

    @ApiModelProperty(value = "父项目组织Id", example = "nju.edu.cn")
    private String parentGroupId;

    @ApiModelProperty(value = "父项目工件Id", example = "backend")
    private String parentArtifactId;

    @ApiModelProperty(value = "父项目版本号", example = "1.0.0")
    private String parentVersion;

    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "backend")
    private String artifactId;

    @ApiModelProperty(value = "旧项目版本号", example = "1.0.0")
    private String oldVersion;

    @ApiModelProperty(value = "项目版本号", example = "1.0.1")
    private String newVersion;

    @ApiModelProperty(value = "项目描述", example = "backend of sca system")
    private String description;

    @ApiModelProperty(value = "项目创建者", example = "000000000")
    private String creator;
}
