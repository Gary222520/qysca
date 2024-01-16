package nju.edu.cn.qysca.domain.components;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.project.ComponentDependencyTreeDO;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.ArrayList;
import java.util.List;

@Document("java_component_close_dependency_tree")
@ApiModel("Java闭源组件依赖树")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class JavaCloseDependencyTreeDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty(value = "组织id", example = "org.hamcrest")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "hamcrest-core")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "1.3")
    private String version;

    @ApiModelProperty(value = "依赖子树", example = "")
    ComponentDependencyTreeDO tree;
}
