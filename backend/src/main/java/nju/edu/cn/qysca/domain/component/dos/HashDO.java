package nju.edu.cn.qysca.domain.component.dos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("哈希值DO")
public class HashDO implements Serializable {

    @ApiModelProperty(value = "算法", example = "md5")
    private String alg;


    @ApiModelProperty(value = "内容", example = "1b641966952f448411fe995406fea1a6")
    private String content;
}
