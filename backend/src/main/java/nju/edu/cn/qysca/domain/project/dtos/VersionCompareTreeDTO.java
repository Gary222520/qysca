package nju.edu.cn.qysca.domain.project.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentCompareTreeDTO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目版本对比树DTO")
public class VersionCompareTreeDTO {
    @ApiModelProperty(value = "项目名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "被对比的版本号", example = "1.0.0")
    private String fromVersion;

    @ApiModelProperty(value = "当前版本号", example = "1.0.1")
    private String toVersion;

    @ApiModelProperty(value = "项目依赖树")
    private ComponentCompareTreeDTO tree;
}
