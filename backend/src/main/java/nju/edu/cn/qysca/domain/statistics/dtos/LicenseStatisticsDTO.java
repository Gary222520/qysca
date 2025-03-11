package nju.edu.cn.qysca.domain.statistics.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "许可证统计DTO")
public class LicenseStatisticsDTO {

    @ApiModelProperty(value = "许可证总数", example = "100")
    private Integer totalNumber;

    @ApiModelProperty(value = "许可证类型数量统计")
    private Map<String, Integer> licenseTypeNumberMap;

    @ApiModelProperty(value = "许可证风险排序")
    private List<LicenseCompareDTO> compareDTOList;
}
