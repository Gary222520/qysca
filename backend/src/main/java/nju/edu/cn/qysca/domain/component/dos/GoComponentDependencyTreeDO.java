package nju.edu.cn.qysca.domain.component.dos;

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
@ApiModel(description = "Go组件依赖树DO")
public class GoComponentDependencyTreeDO extends DependencyTreeDO{
    @ApiModelProperty(value = "组件名称", example = "github.com/gin-gonic/gin")
    private String name;

    @ApiModelProperty(value = "版本号", example = "v1.4.0")
    private String version;

    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @ApiModelProperty(value = "类型",example = "opensource")
    private String type;

    @ApiModelProperty(value = "依赖子树")
    private List<GoComponentDependencyTreeDO> dependencies = new ArrayList<>();
}
