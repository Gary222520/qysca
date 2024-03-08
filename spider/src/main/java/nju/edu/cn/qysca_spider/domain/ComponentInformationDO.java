package nju.edu.cn.qysca_spider.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "组件完整信息DO")
public class ComponentInformationDO {
    @ApiModelProperty(value = "组件信息")
    private ComponentDO componentDO;

    @ApiModelProperty(value = "依赖树")
    private DependencyTreeDO dependencyTreeDO;

    @ApiModelProperty(value = "平铺依赖表")
    private List<DependencyTableDO> dependencyTableDO;
}
