package nju.edu.cn.qysca.domain.sbom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "SBOM，每一个SbomDTO生成一个json文件")
public class SbomDTO {

    @ApiModelProperty(value = "名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "生成时间", example = "2023-11-10 06:05:02 UTC")
    private String timestamp;

    @ApiModelProperty(value = "组件列表")
    private List<SbomComponentDTO> components;
}
