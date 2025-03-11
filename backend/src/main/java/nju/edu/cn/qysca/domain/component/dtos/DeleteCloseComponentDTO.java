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

    @ApiModelProperty(value = "名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;
}
