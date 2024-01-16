package nju.edu.cn.qysca.domain.component;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("开发者信息")
public class DeveloperDO {

    @ApiModelProperty(value = "开发者id", example = "piv")
    private String developerId;

    @ApiModelProperty(value = "开发者名称", example = "Pivotal")
    private String developerName;

    @ApiModelProperty(value = "开发者邮箱", example = "info@pivotal.io")
    private String developerEmail;
}
