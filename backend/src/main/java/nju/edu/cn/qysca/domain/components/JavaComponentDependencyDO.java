package nju.edu.cn.qysca.domain.components;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.utils.idGenerator.Neo4jIdGenerator;
import org.springframework.data.neo4j.core.schema.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RelationshipProperties
@ApiModel(description = "Java组件依赖关系DO")
public class JavaComponentDependencyDO {
    @Id
    @GeneratedValue
    private Long id;

    @TargetNode
    @ApiModelProperty(value = "目标节点", example = "com.example:cc:1.3")
    private JavaComponentDO targetNode;

    @ApiModelProperty(value = "依赖范围", example = "compile")
    private String scope="compile";

    @ApiModelProperty(value = "是否可选", example = "true")
    private boolean optional=false;
}
