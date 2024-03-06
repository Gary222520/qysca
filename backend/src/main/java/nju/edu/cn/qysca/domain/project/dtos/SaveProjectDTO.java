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

    @ApiModelProperty(value = "项目语言", example = "java")
    private String language;

    @ApiModelProperty(value = "构建工具", example = "maven")
    private String builder;

    @ApiModelProperty(value = "扫描对象", example = "zip")
    private String scanner;

    @ApiModelProperty(value = "pom文件路径", example = "/resources/static/upload/qysca/1.0.0/pom.xml")
    private String filePath;
}
