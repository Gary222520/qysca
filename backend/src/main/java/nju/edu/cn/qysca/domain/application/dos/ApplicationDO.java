package nju.edu.cn.qysca.domain.application.dos;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "application",uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id","artifact_id","version"})})
public class ApplicationDO {
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
    @ApiModelProperty(value = "工件id", example = "sca system")
    private String artifactId;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @Column(name="name")
    @ApiModelProperty(value = "应用名称", example = "sca system")
    private String name;

    @Column(name = "description")
    @ApiModelProperty(value = "应用描述", example = "A system designed for software composition analysis")
    private String description;

    @OneToMany(mappedBy = "application")
    @ApiModelProperty(value = "所包含的项目")
    private List<ProjectDO> projects=new ArrayList<>();
}
