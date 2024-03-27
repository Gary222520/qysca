package nju.edu.cn.qysca.domain.component.dos;

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

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "js依赖树")
@Entity
@TypeDefs({
        @TypeDef(name = "jsonb",typeClass = JsonBinaryType.class)
})
@Table(name = "plt_js_dependency_tree", uniqueConstraints = @UniqueConstraint(columnNames = {"name","version"}))
public class JsDependencyTreeDO extends DependencyTreeDO {

    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name = "name", nullable = false)
    @ApiModelProperty(value = "名称", example = "websocket-extensions")
    private String name;

    @Column(name = "version", nullable = false)
    @ApiModelProperty(value = "版本", example = "0.1.0")
    private String version;

    @Column(name= "tree", nullable = false)
    @ApiModelProperty(value = "依赖树")
    @Type(type = "jsonb")
    private JsComponentDependencyTreeDO tree;
}
