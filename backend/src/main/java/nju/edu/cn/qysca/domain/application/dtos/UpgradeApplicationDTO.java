package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("升级应用接口信息DTO")
public class UpgradeApplicationDTO {

    @ApiModelProperty(value = "父应用组织Id", example = "nju.edu.cn")
    private String parentGroupId;

    @ApiModelProperty(value = "父应用工件Id", example = "backend")
    private String parentArtifactId;

    @ApiModelProperty(value = "父应用版本号", example = "1.0.0")
    private String parentVersion;

    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "backend")
    private String artifactId;

    @ApiModelProperty(value = "旧版本号", example = "1.0.0")
    private String oldVersion;

    @ApiModelProperty(value = "版本号", example = "1.0.1")
    private String newVersion;

    @ApiModelProperty(value = "描述", example = "backend of sca system")
    private String description;

    @ApiModelProperty(value = "创建者", example = "000000000")
    private String creator;
}
