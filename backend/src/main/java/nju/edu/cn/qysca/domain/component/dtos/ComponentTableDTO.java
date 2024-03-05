package nju.edu.cn.qysca.domain.component.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("组件平铺信息DTO")
public class ComponentTableDTO {
    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
    private String cGroupId;

    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
    private String cArtifactId;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String cVersion;

    @ApiModelProperty(value = "依赖范围", example = "compile")
    private String scope;

    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @ApiModelProperty(value = "是否开源", example = "true")
    private Boolean opensource;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "是否直接依赖", example = "true")
    private Boolean direct;
}
