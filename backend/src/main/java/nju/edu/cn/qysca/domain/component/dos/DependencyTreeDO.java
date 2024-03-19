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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "plt_dependency_tree",uniqueConstraints = {@UniqueConstraint(columnNames = {"group_id","artifact_id","version"})})
@TypeDefs({
        @TypeDef(name = "jsonb",typeClass = JsonBinaryType.class)
})
public class DependencyTreeDO {
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

    @Column(name = "tree")
    @ApiModelProperty(value = "依赖树")
    @Type(type="jsonb")
    private ComponentDependencyTreeDO tree;
}
