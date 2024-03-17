package nju.edu.cn.qysca.domain.application.dos;

import com.vladmihalcea.hibernate.type.array.StringArrayType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "application", uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id", "artifact_id", "version"})})
@TypeDefs({
        @TypeDef(name = "string-array", typeClass = StringArrayType.class)
})
public class ApplicationDO {
    @Id
    @Column(name = "id", nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name = "group_id", nullable = false)
    @ApiModelProperty(value = "组织id", example = "nju.edu.cn")
    private String groupId;

    @Column(name = "artifact_id", nullable = false)
    @ApiModelProperty(value = "工件id", example = "backend")
    private String artifactId;

    @Column(name = "version", nullable = false)
    @ApiModelProperty(value = "版本号", example = "2.0.1")
    private String version;

    @Column(name = "name")
    @ApiModelProperty(value = "应用名称", example = "backend")
    private String name;

    @Column(name = "description")
    @ApiModelProperty(value = "应用描述", example = "backend of sca system")
    private String description;

    @Column(name = "language", nullable = false)
    @ApiModelProperty(value = "应用语言", example = "java")
    private String language;

    @Column(name = "type", nullable = false)
    @ApiModelProperty(value = "类型", example = "UI")
    private String type;

    @Column(name = "builder", nullable = false)
    @ApiModelProperty(value = "构建工具", example = "maven")
    private String builder;

    @Column(name = "scanner", nullable = false)
    @ApiModelProperty(value = "扫描对象", example = "zip")
    private String scanner;

    @Column(name = "state", nullable = false)
    @ApiModelProperty(value = "扫描状态", example = "SUCCESS,FAILED,RUNNING")
    private String state;

    @Column(name = "time", nullable = false)
    @ApiModelProperty(value = "最近一次更新时间", example = "yyyy-MM-dd HH:mm:ss")
    private String time;

    @Column(name = "lock", nullable = false)
    @ApiModelProperty(value = "应用是否被锁住", example = "true")
    private Boolean lock;

    @Column(name = "release", nullable = false)
    @ApiModelProperty(value = "应用是否被发布", example = "true")
    private Boolean release;

    @Column(name = "creator", nullable = false)
    @ApiModelProperty(value = "创建人", example = "000000000")
    private String creator;

    @Column(name = "childApplication")
    @ApiModelProperty(value = "子应用", example = "['123e456-e74-b37-4d7a-9421d59bf3b',]")
    @Type(type = "string-array")
    private String[] childApplication = {};

    @Column(name = "childComponent")
    @ApiModelProperty(value = "子组件", example = "['123e456-e74-b37-4d7a-9421d59bf3b',]")
    @Type(type = "string-array")
    private  String[] childComponent = {};

    @ManyToOne
    @JoinColumn(name = "bu_id", nullable = false)
    private BuDO bu;
}
