package nju.edu.cn.qysca.domain.component.dos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.ValidationAnnotationUtils;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "组件依赖树DO")
public class JavaComponentDependencyTreeDO {
    @ApiModelProperty(value = "名称", example = "commons-lang")
    private String name;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @ApiModelProperty(value = "类型",example = "opensource")
    private String type;

    @ApiModelProperty(value = "许可证", example = "MIT")
    private String licenses;

    @ApiModelProperty(value = "漏洞", example = "CVE-2020-1197")
    private String vulnerabilities;

    @ApiModelProperty(value = "依赖子树")
    private List<JavaComponentDependencyTreeDO> dependencies = new ArrayList<>();

}
