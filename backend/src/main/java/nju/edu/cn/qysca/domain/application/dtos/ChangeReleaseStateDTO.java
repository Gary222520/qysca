package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "改变发布状态接口")
public class ChangeReleaseStateDTO {

    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "qysca")
    private String artifactId;

    @ApiModelProperty(value = "版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "发布类型")
    private String type;
}
