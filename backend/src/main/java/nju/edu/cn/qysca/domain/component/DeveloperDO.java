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

    @ApiModelProperty(value = "开发者id")
    private String developerId;

    @ApiModelProperty(value = "开发者名称")
    private String developerName;

    @ApiModelProperty(value = "开发者邮箱")
    private String developerEmail;
}
