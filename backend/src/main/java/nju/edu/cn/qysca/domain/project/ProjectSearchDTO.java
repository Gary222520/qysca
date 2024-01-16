package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "项目版本信息搜索DTO")
public class ProjectSearchDTO {
    @ApiModelProperty(value = "项目名称",example = "qysca")
    private String name;

    @ApiModelProperty(value = "项目版本号",example = "1.0.0")
    private String version;
}
