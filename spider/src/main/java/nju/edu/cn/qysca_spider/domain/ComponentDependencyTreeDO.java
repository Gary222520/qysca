package nju.edu.cn.qysca_spider.domain;


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
    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @ApiModelProperty(value = "是否开源",example = "true")
    private Boolean opensource;

    @ApiModelProperty(value = "范围", example = "test")
    private String scope;

    @ApiModelProperty(value = "依赖子树")
    private List<ComponentDependencyTreeDO> dependencies = new ArrayList<>();

}
