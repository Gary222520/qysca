package nju.edu.cn.qysca.domain.application.dtos;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "删除应用的信息接口")
public class DeleteApplicationDTO {


    @ApiModelProperty(value = "父应用名称", example = "app")
    private String parentName;

    @ApiModelProperty(value = "父应用版本号", example = "1.0.0")
    private String parentVersion;

    @ApiModelProperty(value = "应用名称", example = "app")
    private String name;

    @ApiModelProperty(value= "版本号", example = "1.0.0")
    private String version;
}
