package nju.edu.cn.qysca.domain.component.dos;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "js组件DO")
@Entity
@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class),
        @TypeDef(name = "jsonb",typeClass = JsonBinaryType.class)
})
@Table(name="plt_js_component", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "version"}))
public class JsComponentDO extends ComponentDO{

    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="name",nullable = false)
    @ApiModelProperty(value = "名称", example = "fontawesome-free")
    private String name;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

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
    @Type(type = "string-array")
    private String[] copyrightStatements;

    @Column(name = "purl")
    @ApiModelProperty(value = "purl地址", example = "pkg:npm/fontawesome-free@2.5.15")
    private String purl;

    @Column(name = "licenses")
    @ApiModelProperty(value = "许可证", example = "[\"MIT\",]")
    @Type(type = "string-array")
    private String[] licenses = {};

    @ApiModelProperty(value = "下载地址", example = "https://registry.npmjs.org/fontawesome-free/-/fontawesome-free-2.5.15.tgz")
    @Column(name= "download_url")
    private String downloadUrl;

    @ApiModelProperty(value = "语言", example = "javaScript")
    @Column(name = "language", nullable = false)
    private String language;

    @Column(name="type",nullable = false)
    @ApiModelProperty(value = "组件类型",example = "opensource")
    private String type;

    @Column(name="creator")
    @ApiModelProperty(value = "创建者",example = "Font Awesome")
    private String creator;

    @Column(name = "state", nullable = false)
    @ApiModelProperty(value = "扫描状态", example = "SUCCESS,FAILED,RUNNING,CREATED")
    private String state;
}
