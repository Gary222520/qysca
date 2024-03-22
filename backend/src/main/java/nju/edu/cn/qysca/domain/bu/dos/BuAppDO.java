package nju.edu.cn.qysca.domain.bu.dos;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "plt_bu_app")
public class BuAppDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="bid",nullable = false)
    @ApiModelProperty(value = "部门编号", example = "001")
    private String bid;

    @Column(name="aid",nullable = false)
    @ApiModelProperty(value = "应用id", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String aid;
}
