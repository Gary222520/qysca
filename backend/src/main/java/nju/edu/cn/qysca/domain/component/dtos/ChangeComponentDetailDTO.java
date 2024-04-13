package nju.edu.cn.qysca.domain.component.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "修改组件详细信息DTO")
public class ChangeComponentDetailDTO {

    @ApiModelProperty(value = "组件名称", example = "Spring Boot")
    private String name;

    @ApiModelProperty(value = "组件版本", example = "2.7.6")
    private String version;

    @ApiModelProperty(value = "组件语言", example = "Java")
    private String language;

    @ApiModelProperty(value = "组件主页地址", example = "https://spring.io/applications/spring-boot")
    private String url;

    @ApiModelProperty(value = "下载地址", example = "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/2.5.15/spring-boot-starter-2.5.15.pom")
    private String downloadUrl;

    @ApiModelProperty(value = "源码地址", example = "https://github.com/spring-applications/spring-boot")
    private String sourceUrl;

    @ApiModelProperty(value = "包获取地址",example = "pkg:maven/io.grpc/grpc-protobuf@1.44.1?type=jar")
    private String pUrl;
}
