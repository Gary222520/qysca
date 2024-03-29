package nju.edu.cn.qysca.domain.license.dos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("许可证义务与权利条目")
public class LicenseTerm {

    @ApiModelProperty(value = "条目名", example = "保留许可信息")
    private String title;

    @ApiModelProperty(value = "条目具体内容", example = "使用或修改了被许可软件后，需要保留被许可软件的许可声明与许可证文本。")
    private String content;
}
