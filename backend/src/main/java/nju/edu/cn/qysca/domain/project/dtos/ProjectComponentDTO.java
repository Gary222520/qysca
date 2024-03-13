package nju.edu.cn.qysca.domain.project.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "增加/删除项目组件信息接口")
public class ProjectComponentDTO {

    @ApiModelProperty(value = "父项目组织Id", example = "nju.edu.cn")
    private String parentGroupId;

    @ApiModelProperty(value = "父项目工件Id", example = "qysca")
    private String parentArtifactId;

    @ApiModelProperty(value = "父项目版本号", example = "1.0.0")
    private String parentVersion;

    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "qysca")
    private String artifactId;

    @ApiModelProperty(value= "版本号", example = "1.0.0")
    private String version;

}
