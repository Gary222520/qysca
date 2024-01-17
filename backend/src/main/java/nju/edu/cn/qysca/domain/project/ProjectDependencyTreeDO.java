package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("project_dependency_tree")
@ApiModel("项目依赖树信息DO")
public class ProjectDependencyTreeDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty(value = "项目名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "项目版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "项目依赖树")
    private ComponentDependencyTreeDO tree;

}
