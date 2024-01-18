package nju.edu.cn.qysca.domain.component.dos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.ArrayList;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("java_component_open_detail")
@ApiModel("java开源组件详细信息")
public class JavaOpenComponentDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @ApiModelProperty(value = "组件名称", example = "spring-boot-starter")
    private String name;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "组件描述", example = "Core starter, including auto-configuration support, logging and YAML")
    private String description;

    @ApiModelProperty(value = "组件主页地址", example = "https://spring.io/projects/spring-boot")
    private String url;

    @ApiModelProperty(value = "下载地址", example = "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/2.5.15/spring-boot-starter-2.5.15.pom")
    private String downloadUrl;

    @ApiModelProperty(value = "源码地址", example = "https://github.com/spring-projects/spring-boot")
    private String sourceUrl;

    @ApiModelProperty(value = "开发者列表")
    private List<DeveloperDO> developers = new ArrayList<>();

    @ApiModelProperty(value = "许可证列表")
    private List<LicenseDO> licenses = new ArrayList<>();

    @ApiModelProperty(value = "pom文件", example = "{plugin:{},dependencies:[]}")
    private String pom;
}
