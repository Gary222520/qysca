package nju.edu.cn.qysca.domain.component;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "查询组件信息DTO")
public class ComponentSearchDTO {
    @ApiModelProperty(value = "组织id", example = "org.hamcrest")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "hamcrest-core")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "1.3")
    private String version;

    @ApiModelProperty(value = "名称", example = "Hamcrest Core")
    private String name;

    @ApiModelProperty(value = "语言", example = "language")
    private String language;

    @ApiModelProperty(value = "页码", example = "1")
    private Integer number;

    @ApiModelProperty(value = "页大小", example = "10")
    private Integer size;
}
