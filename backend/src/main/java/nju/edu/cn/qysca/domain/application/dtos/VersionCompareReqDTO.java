package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("应用版本对比DTO")
public class VersionCompareReqDTO {

    @ApiModelProperty(value = "项目名称")
    private String name;

    @ApiModelProperty(value = "被对比的版本号", example = "1.0.0")
    private String fromVersion;

    @ApiModelProperty(value = "当前版本号", example = "1.0.1")
    private String toVersion;
}
