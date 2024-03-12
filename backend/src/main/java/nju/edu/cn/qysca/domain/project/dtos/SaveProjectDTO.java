package nju.edu.cn.qysca.domain.project.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("保存项目信息DTO")
public class SaveProjectDTO {

    @ApiModelProperty(value = "父项目Id", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String parentId;

    @ApiModelProperty(value = "项目Id", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "backend")
    private String artifactId;

    @ApiModelProperty(value = "项目版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "项目名称", example = "backend")
    private String name;

    @ApiModelProperty(value = "项目描述", example = "backend of sca system")
    private String description;

    @ApiModelProperty(value = "类型",example = "UI")
    private String type;

    @ApiModelProperty(value = "项目创建者", example = "000000000")
    private String creator;
}
