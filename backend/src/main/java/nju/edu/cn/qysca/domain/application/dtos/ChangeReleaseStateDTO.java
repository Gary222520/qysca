package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "改变发布状态接口")
public class ChangeReleaseStateDTO {

    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String name;

    @ApiModelProperty(value = "版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "发布类型")
    private String type;

    @ApiModelProperty(value = "组件主页地址", example = "https://spring.io/applications/spring-boot")
    private String url;

    @ApiModelProperty(value = "源码地址", example = "https://github.com/spring-applications/spring-boot")
    private String sourceUrl;

    @ApiModelProperty(value = "下载地址", example = "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/2.5.15/spring-boot-starter-2.5.15.pom")
    private String downloadUrl;

    @ApiModelProperty(value = "包获取地址",example = "pkg:maven/io.grpc/grpc-protobuf@1.44.1?type=jar")
    private String packageUrl;
}
