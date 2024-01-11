package nju.edu.cn.qysca.domain.components;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DependencyComponentDO {


    @ApiModelProperty(value = "", example = "org.hamcrest")
    private String groupId;

    @ApiModelProperty(value = "", example = "hamcrest-core")
    private String artifactId;

    @ApiModelProperty(value = "组件版本", example = "1.3")
    private String version;

    @ApiModelProperty(value = "组件名称", example = "Hamcrest")
    private String name;

    @ApiModelProperty(value = "组件依赖", example = "org.hamcrest:hamcrest-core:1.3")
    private List<DependencyComponentDO> dependencies;

}
