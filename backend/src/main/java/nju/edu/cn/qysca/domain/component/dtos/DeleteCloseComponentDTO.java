package nju.edu.cn.qysca.domain.component.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "删除闭源组件接口信息")
public class DeleteCloseComponentDTO {

    @ApiModelProperty(value = "组件id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "qysca")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;
}
