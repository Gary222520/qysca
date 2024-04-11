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
@ApiModel(description = "SBOM-依赖DTO")
public class SbomDependencyDTO {
    @ApiModelProperty(value = "组件purl", example = "pkg:maven/io.springfox/springfox-swagger2@2.9.2")
    private String ref;

    @ApiModelProperty(value = "依赖的purl")
    private List<String> dependsOn;
}
