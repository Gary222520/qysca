package nju.edu.cn.qysca.domain.component.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.component.dos.DeveloperDO;
import nju.edu.cn.qysca.domain.component.dos.HashDO;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("组件详细信息DTO")
public class ComponentDetailDTO {

    @ApiModelProperty(value = "组件名称", example = "spring-boot-starter")
    private String name;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "类型",example = "opensource")
    private String type;

    @ApiModelProperty(value = "组件描述", example = "Core starter, including auto-configuration support, logging and YAML")
    private String description;

    @ApiModelProperty(value = "组件主页地址", example = "https://spring.io/applications/spring-boot")
    private String url;

    @ApiModelProperty(value = "下载地址", example = "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/2.5.15/spring-boot-starter-2.5.15.pom")
    private String downloadUrl;

    @ApiModelProperty(value = "源码地址", example = "https://github.com/spring-applications/spring-boot")
    private String sourceUrl;

    @ApiModelProperty(value = "包获取地址",example = "pkg:maven/io.grpc/grpc-protobuf@1.44.1?type=jar")
    private String pUrl;

    @ApiModelProperty(value = "开发者列表")
    private List<DeveloperDO> developers = new ArrayList<>();

    @ApiModelProperty(value = "许可证列表")
    private String[] licenses = {};

    @ApiModelProperty(value = "哈希值列表")
    private List<HashDO> hashes=new ArrayList<>();

    @ApiModelProperty(value = "创建者")
    private String creator;
}
