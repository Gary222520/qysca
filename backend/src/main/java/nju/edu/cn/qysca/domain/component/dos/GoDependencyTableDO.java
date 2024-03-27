package nju.edu.cn.qysca.domain.component.dos;

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
@Table(name= "plt_go_dependency_table")
public class GoDependencyTableDO extends DependencyTableDO{
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="name",nullable = false)
    @ApiModelProperty(value = "组件名称", example = "github.com/gin-gonic/gin")
    private String name;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "v1.4.0")
    private String version;

    @Column(name="c_name",nullable = false)
    @ApiModelProperty(value = "子组件名称", example = "github.com/gin-gonic/gin")
    private String cName;

    @Column(name = "c_version",nullable = false)
    @ApiModelProperty(value = "子组件版本号", example = "v1.4.0")
    private String cVersion;

    @Column(name = "depth",nullable = false)
    @ApiModelProperty(value = "依赖层级", example = "1")
    private Integer depth;

    @Column(name = "direct",nullable = false)
    @ApiModelProperty(value = "是否直接依赖", example = "true")
    private Boolean direct;

    @Column(name="type",nullable = false)
    @ApiModelProperty(value = "类型",example = "opensource")
    private String type;

    @Column(name="language",nullable = false)
    @ApiModelProperty(value = "语言", example = "java")
    private String language;
}
