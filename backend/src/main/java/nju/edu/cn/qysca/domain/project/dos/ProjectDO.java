package nju.edu.cn.qysca.domain.project.dos;

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
@Table(name= "project",uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id","artifact_id","version"})})
public class ProjectDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="group_id",nullable = false)
    @ApiModelProperty(value = "组织id", example = "nju.edu.cn")
    private String groupId;

    @Column(name="artifact_id",nullable = false)
    @ApiModelProperty(value = "工件id", example = "backend")
    private String artifactId;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "2.0.1")
    private String version;

    @Column(name="name")
    @ApiModelProperty(value = "项目名称", example = "backend")
    private String name;

    @Column(name = "description")
    @ApiModelProperty(value = "项目描述", example = "backend of sca system")
    private String description;

    @Column(name="language",nullable = false)
    @ApiModelProperty(value = "项目语言", example = "java")
    private String language;

    @Column(name = "type",nullable = false)
    @ApiModelProperty(value = "类型",example = "UI")
    private String type;

    @Column(name="builder",nullable = false)
    @ApiModelProperty(value = "构建工具", example = "maven")
    private String builder;

    @Column(name = "scanner",nullable = false)
    @ApiModelProperty(value = "扫描对象", example = "zip")
    private String scanner;

    @Column(name = "state",nullable = false)
    @ApiModelProperty(value = "扫描状态", example = "SUCCESS,FAILED,RUNNING")
    private String state;

    @Column(name = "time",nullable = false)
    @ApiModelProperty(value = "最近一次更新时间", example = "yyyy-MM-dd HH:mm:ss")
    private String time;
}
