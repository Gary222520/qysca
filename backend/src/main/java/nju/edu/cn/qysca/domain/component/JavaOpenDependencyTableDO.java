package nju.edu.cn.qysca.domain.component;

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
@Document("java_component_open_dependency_table")
@ApiModel("Java开源组件依赖平铺信息DO")
public class JavaOpenDependencyTableDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty(value = "父级组织id", example = "org.springframework.boot")
    private String parentGroupId;

    @ApiModelProperty(value = "父级工件id", example = "spring-boot-starter-parent")
    private String parentArtifactId;

    @ApiModelProperty(value = "父级版本号", example = "2.5.15")
    private String parentVersion;

    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @ApiModelProperty(value = "组件名称", example = "spring-boot-starter")
    private String name;

    @ApiModelProperty(value = "依赖范围", example = "compile")
    private String scope;

    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @ApiModelProperty(value = "是否开源", example = "true")
    private Boolean opensource;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "许可证", example = "Apache License, Version 2.0")
    private String licenses;

    @ApiModelProperty(value = "是否直接依赖", example = "true")
    private Boolean direct;
}
