package nju.edu.cn.qysca.domain.statistics.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "许可证比对结果DTO")
public class LicenseCompareDTO implements Comparable<LicenseCompareDTO>{

    @ApiModelProperty(value = "应用名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "应用版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "许可证数量", example = "{\"high\": 10, \"medium:\" 10, \"low\": 20, \"unknown\": 50}")
    private Map<String, Integer> map = new HashMap<>();

    public int getTotalLicenses() {
        return map.values().stream().mapToInt(Integer::intValue).sum();
    }

    @Override
    public int compareTo(LicenseCompareDTO licenseCompareDTO) {
        int totalComparison = Integer.compare(licenseCompareDTO.getTotalLicenses(), this.getTotalLicenses());
        if(totalComparison != 0) return totalComparison;

        int highComparison = Integer.compare(licenseCompareDTO.getMap().get("high"), this.getMap().get("high"));
        if(highComparison != 0) return highComparison;

        int mediumComparison = Integer.compare(licenseCompareDTO.getMap().get("medium"), this.getMap().get("medium"));
        if(mediumComparison != 0) return mediumComparison;

        int lowComparison = Integer.compare(licenseCompareDTO.getMap().get("low"), this.getMap().get("low"));
        if(lowComparison != 0) return lowComparison;

        return Integer.compare(licenseCompareDTO.getMap().get("unknown"), this.getMap().get("unknown"));
    }
}
