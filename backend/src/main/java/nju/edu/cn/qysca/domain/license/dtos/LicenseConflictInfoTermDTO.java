package nju.edu.cn.qysca.domain.license.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.license.dos.LicenseDO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "许可证冲突信息条目")
public class LicenseConflictInfoTermDTO {

    @ApiModelProperty(value = "许可证条款题目", example = "保留许可信息")
    private String title;

    @ApiModelProperty(value = "符合该条款的许可证列表", notes = "对于义务，表示这些许可证必须满足该条款; 对于权利，表示这些许可证允许行使该条款的权利")
    private List<LicenseDO> pos_licenses;

    @ApiModelProperty(value = "不符合该条款的许可证列表", notes = "对于义务，表示这些许可证无需满足该条款; 对于权利，表示这些许可证禁止行使该条款的权利")
    private List<LicenseDO> neg_licenses;
}
