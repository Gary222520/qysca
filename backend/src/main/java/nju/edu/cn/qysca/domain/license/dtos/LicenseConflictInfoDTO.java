package nju.edu.cn.qysca.domain.license.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "许可证冲突信息")
public class LicenseConflictInfoDTO {

    @ApiModelProperty(value = "义务冲突信息条目")
    private List<LicenseConflictInfoTermDTO> obligations_terms;

    @ApiModelProperty(value = "权利冲突信息条目")
    private List<LicenseConflictInfoTermDTO> rights_terms;

}
