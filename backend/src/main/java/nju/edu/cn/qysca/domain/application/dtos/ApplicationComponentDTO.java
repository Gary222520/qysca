package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "增加/删除应用组件信息接口")
public class ApplicationComponentDTO {

    @ApiModelProperty(value = "父应用名称")
    private String parentName;

    @ApiModelProperty(value = "父应用版本号", example = "1.0.0")
    private String parentVersion;

    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "qysca")
    private String artifactId;

    @ApiModelProperty(value= "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

}
