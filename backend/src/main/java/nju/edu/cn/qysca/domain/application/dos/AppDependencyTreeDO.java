package nju.edu.cn.qysca.domain.application.dos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "plt_app_dependency_tree", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "version"}))
@ApiModel(description = "App组件依赖树信息")
public class AppDependencyTreeDO extends DependencyTreeDO {

    @Id
    @Column(name = "id", nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="name", nullable = false)
    @ApiModelProperty(value = "组件名称", example = "spring-boot-starter")
    private String name;

    @Column(name = "version",nullable = false)
    @ApiModelProperty(value = "版本号", example = "2.5.15")
    private String version;

    @Column(name = "tree")
    @ApiModelProperty(value = "依赖树")
    @Type(type="jsonb")
    private AppComponentDependencyTreeDO tree;
}
