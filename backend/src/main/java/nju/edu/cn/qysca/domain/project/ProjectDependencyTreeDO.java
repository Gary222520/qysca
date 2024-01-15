package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("project_dependency_tree")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目依赖树")
public class ProjectDependencyTreeDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty("项目名称")
    private String name;

    @ApiModelProperty("项目版本")
    private String version;

    @ApiModelProperty("项目依赖树")
    private ComponentDependencyTreeDO tree;

}
