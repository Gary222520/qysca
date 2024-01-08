package nju.edu.cn.qysca.domain.example;

import java.io.Serializable;
import javax.persistence.*;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

@Data
@Entity
@Table(name = "example")
@ApiModel(description = "演示POJO类的ExampleDO")
public class ExampleDO implements Serializable {

    @Id
    @GenericGenerator(name = "idGenerator", strategy = "uuid")
    @GeneratedValue(generator = "idGenerator")
    @ApiModelProperty(value = "主键uuid", example = "2c9fa89a8ce6dede018ce6e0dba90003")
    private String uuid;

    @Column(name = "number", unique = true, nullable = false)
    @ApiModelProperty(value = "编号", example = "1")
    private int number;

    @Column(name = "info", nullable = false, length = 100)
    @ApiModelProperty(value = "信息", example = "i like you")
    private String info;
}
