package nju.edu.cn.qysca.domain.statistics.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "许可证比对结果DTO")
public class LicenseCompareDTO implements Comparable<LicenseCompareDTO>{

    @ApiModelProperty(value = "应用名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "应用版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "安全许可证个数",example = "10")
    private int secure;

    @ApiModelProperty(value = "风险许可证个数", example = "10")
    private int risk;

    @Override
    public int compareTo(LicenseCompareDTO licenseCompareDTO) {
        double thisRatio;
        if(this.risk == 0 &&this.secure == 0) {
            thisRatio = Double.NEGATIVE_INFINITY;
        }else{
            thisRatio = this.risk / (double) (this.secure + this.risk);
        }
        double otherRatio;
        if(licenseCompareDTO.getRisk() == 0 && licenseCompareDTO.getSecure() == 0) {
            otherRatio = Double.NEGATIVE_INFINITY;
        }else{
            otherRatio = licenseCompareDTO.getRisk() / (double) (licenseCompareDTO.getSecure() + licenseCompareDTO.getRisk());
        }
        int ratioCompare = Double.compare(otherRatio, thisRatio);
        if(ratioCompare != 0){
            return ratioCompare;
        } else{
            return Integer.compare(licenseCompareDTO.risk, this.risk);
        }
    }
}
