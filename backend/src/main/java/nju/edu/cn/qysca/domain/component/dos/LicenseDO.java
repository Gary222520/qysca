package nju.edu.cn.qysca.domain.component.dos;

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

    @ApiModelProperty(value = "许可证名称", example = "Eclipse Public License v2.0")
    private String licenseName;

    @ApiModelProperty(value = "许可证URL", example = "https://www.eclipse.org/legal/epl-v20.html")
    private String licenseUrl;
}
