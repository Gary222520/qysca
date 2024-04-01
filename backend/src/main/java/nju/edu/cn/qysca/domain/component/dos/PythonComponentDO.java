package nju.edu.cn.qysca.domain.component.dos;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "plt_python_component",uniqueConstraints = {@UniqueConstraint(columnNames = {"name","version"})})
@TypeDefs({
        @TypeDef(name = "jsonb",typeClass = JsonBinaryType.class)
})
public class PythonComponentDO extends ComponentDO{
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="name",nullable = false)
    @ApiModelProperty(value = "组件名", example = "kafka-counter")
    private String name;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "0.0.2")
    private String version;

    @Column(name="language",nullable = false)
    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @Column(name = "description")
    @ApiModelProperty(value = "组件描述", example = "Core starter, including auto-configuration support, logging and YAML")
    private String description;

    @Column(name="type",nullable = false)
    @ApiModelProperty(value = "组件类型",example = "opensource")
    private String type;

    @Column(name = "url")
    @ApiModelProperty(value = "组件主页地址", example = "https://github.com/dpkp/kafka-python")
    private String url;

    @Column(name = "download_url")
    @ApiModelProperty(value = "下载地址", example = "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/2.5.15/spring-boot-starter-2.5.15.pom")
    private String downloadUrl;

    @Column(name = "source_url")
    @ApiModelProperty(value = "源码地址", example = "https://github.com/spring-applications/spring-boot")
    private String sourceUrl;

    @Column(name = "p_url")
    @ApiModelProperty(value = "包获取地址",example = "pkg:maven/io.grpc/grpc-protobuf@1.44.1?type=jar")
    private String pUrl;

    @Column(name = "author")
    @ApiModelProperty(value = "作者", example = "Dana Powers")
    private String author;

    @Column(name = "author_email")
    @ApiModelProperty(value = "作者邮件地址", example = "dana.powers@gmail.com")
    private String authorEmail;

    @Column(name = "licenses")
    @ApiModelProperty(value = "许可证", example = "[\"MIT\",]")
    @Type(type = "string-array")
    private String[] licenses = {};

    @Column(name  = "vulnerabilities")
    @ApiModelProperty(value = "漏洞", example = "[\"CVE-2020-1197\",]")
    @Type(type= "string-array")
    private String[] vulnerabilities = {};

    @Column(name = "creator")
    @ApiModelProperty(value = "创建者", example = "000000000")
    private String creator;

    @Column(name = "state", nullable = false)
    @ApiModelProperty(value = "扫描状态", example = "SUCCESS,FAILED,RUNNING,CREATED")
    private String state;
}
