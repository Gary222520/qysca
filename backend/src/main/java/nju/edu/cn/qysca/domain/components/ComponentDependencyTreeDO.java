package nju.edu.cn.qysca.domain.components;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "组件依赖树DO")
public class ComponentDependencyTreeDO {
    @ApiModelProperty(value = "组织id", example = "org.hamcrest")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "hamcrest-core")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "1.3")
    private String version;

    @ApiModelProperty(value = "名称", example = "Hamcrest")
    private String name;

    @ApiModelProperty(value = "依赖子树", example = "{com.example:cc:1.3,org.vk:util:v2.31}")
    private List<ComponentDependencyTreeDO> dependencies=new ArrayList<>();

}
