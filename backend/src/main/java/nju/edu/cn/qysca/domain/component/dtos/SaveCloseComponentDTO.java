package nju.edu.cn.qysca.domain.component.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("保存闭源组件DTO")
public class SaveCloseComponentDTO {

    @ApiModelProperty(value = "名称", example = "my-component")
    private String name;

    @ApiModelProperty(value = "版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "文件存储路径", example = "resources/static/upload/")
    private String filePath;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "构建工具", example = "maven")
    private String builder;

    @ApiModelProperty(value = "类型", example = "opensource")
    private String type;
}
