package nju.edu.cn.qysca.domain.component.dos;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "dependency_table")
public class DependencyTableDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="group_id",nullable = false)
    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
    private String groupId;

    @Column(name="artifact_id",nullable = false)
    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
    private String artifactId;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @Column(name="c_group_id",nullable = false)
    @ApiModelProperty(value = "子组件组织id", example = "org.springframework.boot")
    private String cGroupId;

    @Column(name="c_artifact_id",nullable = false)
    @ApiModelProperty(value = "子组件工件id", example = "spring-boot-starter")
    private String cArtifactId;

    @Column(name = "c_version",nullable = false)
    @ApiModelProperty(value = "子组件版本号", example = "2.5.15")
    private String cVersion;

    @Column(name = "scope")
    @ApiModelProperty(value = "依赖范围", example = "compile")
    private String scope;

    @Column(name = "depth",nullable = false)
    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @Column(name = "direct",nullable = false)
    @ApiModelProperty(value = "是否直接依赖", example = "true")
    private Boolean direct;

    @Column(name="opensource",nullable = false)
    @ApiModelProperty(value = "是否开源",example = "true")
    private Boolean opensource;

    @Column(name="language",nullable = false)
    @ApiModelProperty(value = "语言", example = "java")
    private String language;
}
