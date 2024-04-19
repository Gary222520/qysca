package nju.edu.cn.qysca.domain.npm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "package.json文件信息")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageJsonDTO {

    @ApiModelProperty(value = "名称", example = "my-project")
    private String name;

    @ApiModelProperty(value = "版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "描述", example = "这是一个示例项目")
    private String description;

    @ApiModelProperty(value = "作者", example = "John Doe")
    private String author;

    @ApiModelProperty(value = "许可证", example = "MIT")
    private String license;

    @ApiModelProperty(value = "依赖")
    private Map<String, String> dependencies = new HashMap<>();

    @ApiModelProperty(value = "开发时依赖")
    private Map<String, String> devDependencies = new HashMap<>();
}
