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
@Table(name= "plt_role")
public class RoleDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "402851818e4af0ab018e4af0bbc80000")
    private String id;

    @Column(name="name",unique = true,nullable = false)
    @ApiModelProperty(value = "角色名称", example = "Bu PO, Bu Rep, App Leader, App Member, Admin")
    private String name;
}
