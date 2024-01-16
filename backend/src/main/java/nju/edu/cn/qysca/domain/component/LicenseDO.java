package nju.edu.cn.qysca.domain.component;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("许可证DO")
public class LicenseDO {

    @ApiModelProperty("许可证名称")
    private String licenseName;

    @ApiModelProperty("许可证URL")
    private String licenseUrl;
}
