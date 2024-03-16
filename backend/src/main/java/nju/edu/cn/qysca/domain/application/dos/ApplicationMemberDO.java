package nju.edu.cn.qysca.domain.application.dos;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(name = "application_member")
public class ApplicationMemberDO {

    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @ManyToOne
    @JoinColumn(name = "application_id", nullable = false)
    private ApplicationDO applicationDO;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private UserDO userDO;

    @Column(name = "role", nullable = false)
    @ApiModelProperty(value = "角色", example = "App Member")
    private String role;
}
