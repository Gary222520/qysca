package nju.edu.cn.qysca.domain.component.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "更新闭源组件接口")
public class UpdateCloseComponentDTO {

    @ApiModelProperty(value = "名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "文件路径", example = "src/main/java/nju/edu/cn/qysca/")
    private String filePath;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "构建工具", example = "maven")
    private String builder;

    @ApiModelProperty(value = "类型", example = "opensource")
    private String type;
}
