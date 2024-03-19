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
@Table(name= "plt_user_role", uniqueConstraints = {@UniqueConstraint(columnNames = {"uid", "rid","bid","aid"})})
public class UserRoleDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "402851818e4af0ab018e4af0bbc80000")
    private String id;

    @Column(name="uid",nullable = false)
    @ApiModelProperty(value = "用户id", example = "402851818e4af0ab018e4af0bbc80000")
    private String uid;

    @Column(name="rid",nullable = false)
    @ApiModelProperty(value = "角色id", example = "402851818e4af0ab018e4af0bbc80000")
    private String rid;

    @Column(name="bid",nullable = false)
    @ApiModelProperty(value = "部门id", example = "402851818e4af0ab018e4af0bbc80000")
    private String bid;

    @Column(name="aid",nullable = false)
    @ApiModelProperty(value = "应用id", example = "402851818e4af0ab018e4af0bbc80000")
    private String aid;
}
