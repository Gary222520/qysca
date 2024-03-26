package nju.edu.cn.qysca.domain.component.dos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "js组件DO")
public class JsComponentDO extends ComponentDO{

    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="artifact_id",nullable = false)
    @ApiModelProperty(value = "工件id", example = "fontawesome-free")
    private String artifactId;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @Column(name = "namespace")
    @ApiModelProperty(value = "命名空间", example = "@fortawesome")
    private String namespace;

    @Column(name = "description")
    @ApiModelProperty(value = "组件描述", example = "The iconic font, CSS, and SVG framework")
    private String description;

    @Column(name = "website")
    @ApiModelProperty(value = "官网", example = "https://fontawesome.com/")
    private String website;

    @Column(name = "repo_url")
    @ApiModelProperty(value = "仓库地址", example = "https://github.com/FortAwesome/Font-Awesome")
    private String repoUrl;

    @Column(name = "copyright_statements")
    @ApiModelProperty(value = "版权声明", example = "[\"The Font Awesome Team, https://github.com/orgs/FortAwesome/people\"]")
    private String[] copyright_statements;

    @Column(name = "purl")
    @ApiModelProperty(value = "purl地址", example = "pkg:npm/fontawesome-free@2.5.15")
    private String purl;

    @Column(name= "license")
    @ApiModelProperty(value = "许可证", example = "MIT")
    private String license;

    @ApiModelProperty(value = "下载地址", example = "https://registry.npmjs.org/fontawesome-free/-/fontawesome-free-2.5.15.tgz")
    @Column(name= "download_url")
    private String downloadUrl;

    @ApiModelProperty(value = "语言", example = "JavaScript")
    @Column(name = "language", nullable = false)
    private String language;
}
