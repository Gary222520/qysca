package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.components.ComponentDependencyTreeDO;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Document("project")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "项目信息")
public class ProjectDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty(value = "项目名称", example = "项目1")
    @Field("name")
    private String name;

    @Field("version")
    @ApiModelProperty(value = "项目版本", example = "1.0.0")
    private String version;

    @Field("language")
    @ApiModelProperty(value = "项目语言", example = "Java")
    private String language;

    @Field("builder")
    @ApiModelProperty(value = "构建工具", example = "Maven")
    private String builder;

    @Field("scanner")
    @ApiModelProperty(value = "扫描对象", example = "pom.xml")
    private String scanner;

    @Field("time")
    @ApiModelProperty(value = "最近一次扫描时间", example = "2022-01-01 00:00:00")
    private String time;

    @Field("note")
    @ApiModelProperty(value = "备注", example = "")
    private String note;

    @Field("dependencies")
    @ApiModelProperty(value = "依赖组件列表", example = "")
    private List<ComponentDependencyTreeDO> dependencies;
}
