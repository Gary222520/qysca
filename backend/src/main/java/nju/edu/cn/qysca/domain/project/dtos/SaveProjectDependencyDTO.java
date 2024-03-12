package nju.edu.cn.qysca.domain.project.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "新增/更新项目依赖信息")
public class SaveProjectDependencyDTO {

    @ApiModelProperty(value = "项目Id", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @ApiModelProperty(value = "项目组织Id", example = "nju.edu,cn")
    private String groupId;

    @ApiModelProperty(value = "项目工件Id", example = "qysca")
    private String artifactId;

    @ApiModelProperty(value = "项目版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "Pom文件路径", example = "/pom.xml")
    private String filePath;

    @ApiModelProperty(value = "项目语言", example = "java")
    private String language;

    @ApiModelProperty(value = "构建工具",example = "maven")
    private String builder;
}
