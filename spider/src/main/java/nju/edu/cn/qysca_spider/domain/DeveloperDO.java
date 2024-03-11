package nju.edu.cn.qysca_spider.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("开发者信息")
public class DeveloperDO implements Serializable {

    @ApiModelProperty(value = "开发者id", example = "piv")
    private String id;

    @ApiModelProperty(value = "开发者名称", example = "Pivotal")
    private String name;

    @ApiModelProperty(value = "开发者邮箱", example = "info@pivotal.io")
    private String email;
}
