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
@Table(name= "plt_user_role")
public class UserRoleDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "402851818e4af0ab018e4af0bbc80000")
    private String id;

    @Column(name="user_id",nullable = false)
    @ApiModelProperty(value = "用户id", example = "402851818e4af0ab018e4af0bbc80000")
    private String userId;

    @Column(name="role_id",nullable = false)
    @ApiModelProperty(value = "角色id", example = "402851818e4af0ab018e4af0bbc80000")
    private String roleId;

    @Column(name="bu_id",nullable = false)
    @ApiModelProperty(value = "部门id", example = "402851818e4af0ab018e4af0bbc80000")
    private String buId;

    @Column(name="app_id",nullable = false)
    @ApiModelProperty(value = "应用id", example = "402851818e4af0ab018e4af0bbc80000")
    private String appId;
}
