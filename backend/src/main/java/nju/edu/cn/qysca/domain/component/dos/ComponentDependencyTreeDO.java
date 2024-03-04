//package nju.edu.cn.qysca.domain.component.dos;
//
//
//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@ApiModel(description = "组件依赖树DO")
//public class ComponentDependencyTreeDO {
//    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
//    private String groupId;
//
//    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
//    private String artifactId;
//
//    @ApiModelProperty(value = "版本号", example = "2.5.15")
//    private String version;
//
//    @ApiModelProperty(value = "组件名称", example = "spring-boot-starter")
//    private String name;
//
//    @ApiModelProperty(value = "依赖范围", example = "compile")
//    private String scope;
//
//    @ApiModelProperty(value = "依赖层级", example = "1")
//    private Integer depth;
//
//    @ApiModelProperty(value = "是否开源", example = "true")
//    private Boolean opensource;
//
//    @ApiModelProperty(value = "语言", example = "java")
//    private String language;
//
//    @ApiModelProperty(value = "许可证", example = "Apache License, Version 2.0")
//    private String licenses;
//
//    @ApiModelProperty(value = "是否直接依赖", example = "true")
//    private Boolean direct;
//
//    @ApiModelProperty(value = "依赖子树")
//    private List<ComponentDependencyTreeDO> dependencies = new ArrayList<>();
//
//}
