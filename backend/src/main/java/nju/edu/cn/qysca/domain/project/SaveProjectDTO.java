package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveProjectDTO {

    @ApiModelProperty("项目语言")
    private String language;

    @ApiModelProperty("项目构建工具")
    private String builder;

    @ApiModelProperty("项目扫描工具")
    private String scanner;

    @ApiModelProperty("项目名称")
    private String name;

    @ApiModelProperty("项目版本")
    private String version;

    @ApiModelProperty("项目备注信息")
    private String note;

    @ApiModelProperty("pom文件路径")
    private String filePath;

}
