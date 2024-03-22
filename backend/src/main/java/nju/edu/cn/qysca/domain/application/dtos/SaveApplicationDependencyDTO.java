package nju.edu.cn.qysca.domain.application.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "新增/更新应用依赖信息")
public class SaveApplicationDependencyDTO {

    @ApiModelProperty(value = "应用名称", example = "my-app")
    private String name;

    @ApiModelProperty(value = "应用版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "Pom文件路径", example = "/pom.xml")
    private String filePath;

    @ApiModelProperty(value = "应用语言", example = "java")
    private String language;

    @ApiModelProperty(value = "构建工具",example = "maven")
    private String builder;

    @ApiModelProperty(value = "扫描工具", example = "pom")
    private String scanner;
}
