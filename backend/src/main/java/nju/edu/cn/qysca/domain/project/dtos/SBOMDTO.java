package nju.edu.cn.qysca.domain.project.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "项目级别SBOM")
public class SBOMDTO {
    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "com.example:example-project")
    private String artifactId;

    @ApiModelProperty(value = "项目版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "直接依赖组件")
    private List<ComponentDO> components;
}
