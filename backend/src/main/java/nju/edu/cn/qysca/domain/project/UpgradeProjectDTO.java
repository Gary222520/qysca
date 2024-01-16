package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("升级项目接口信息")
public class UpgradeProjectDTO {

    @ApiModelProperty("项目语言")
    private String language;

    @ApiModelProperty("项目构建工具")
    private String builder;

    @ApiModelProperty("项目扫描工具")
    private String scanner;

    @ApiModelProperty(value = "项目名称")
    private String name;

    @ApiModelProperty(value = "项目版本")
    private String version;

    @ApiModelProperty(value = "项目备注")
    private String note;

    @ApiModelProperty(value = "pom文件路径")
    private String filePath;
}
