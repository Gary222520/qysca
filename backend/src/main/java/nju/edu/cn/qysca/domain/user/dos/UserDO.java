package nju.edu.cn.qysca.domain.user.dos;

import io.netty.channel.unix.PeerCredentials;
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
@Table(name= "user_role")
public class UserDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="uid",nullable = false,unique = true)
    @ApiModelProperty(value = "用户编号", example = "201258794")
    private String uid;

    @Column(name="name",nullable = false)
    @ApiModelProperty(value = "用户姓名", example = "Jimmy Cart")
    private String name;

    @Column(name="password",nullable = false)
    @ApiModelProperty(value = "用户密码", example = "2724fhfihwiwhHH")
    private String password;

    @Column(name = "email",nullable = false)
    @ApiModelProperty(value = "用户邮箱", example = "2378@xyz.com")
    private String email;

    @Column(name="phone",nullable = false)
    @ApiModelProperty(value = "用户手机", example = "18930298746")
    private String phone;

    @Column(name = "role",nullable = false)
    @ApiModelProperty(value = "用户角色", example = "Bu Rep, App Leader, App Member, Bu PO, Admin")
    private String role;
}
