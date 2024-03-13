package nju.edu.cn.qysca.domain.sbom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("Sbom外部链接DTO")
public class SbomExternalReferenceDTO {
    @ApiModelProperty(value = "类型", example = "website")
    private String type;

    @ApiModelProperty(value = "url", example = "http://jcp.org/en/jsr/detail?id=250")
    private String url;
}
