package nju.edu.cn.qysca.domain.license.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "许可证简明信息")
public class LicenseBriefDTO {

    @ApiModelProperty(value = "许可证名称（id）", example = "Apache-2.0")
    private String name;

    @ApiModelProperty(value = "许可证全名", example = "Apache LicenseDO 2.0")
    private String fullName;


    @ApiModelProperty(value = "OSI认证", example = "true")
    private Boolean isOsiApproved;


    @ApiModelProperty(value = "FSF许可", example = "false")
    private Boolean isFsfApproved;


    @ApiModelProperty(value = "SPDX认证", example = "true")
    private Boolean isSpdxApproved;


    @ApiModelProperty(value = "风险等级", example = "high")
    private String riskLevel;

    @ApiModelProperty(value = "GPL兼容性", example = "true")
    private Boolean gplCompatibility;
}
