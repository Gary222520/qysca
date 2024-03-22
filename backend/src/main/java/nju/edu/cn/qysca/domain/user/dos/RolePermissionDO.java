package nju.edu.cn.qysca.domain.user.dos;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "plt_role_permission", uniqueConstraints = {@UniqueConstraint(columnNames = {"rid","pid"})})
public class RolePermissionDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "402851818e4af0ab018e4af0bbc80000")
    private String id;

    @Column(name="rid",nullable = false)
    @ApiModelProperty(value = "角色id", example = "402851818e4af0ab018e4af0bbc80000")
    private String rid;

    @Column(name="pid",nullable = false)
    @ApiModelProperty(value = "权限id", example = "402851818e4af0ab018e4af0bbc80000")
    private String pid;
}
