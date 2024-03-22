package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("保存应用信息DTO")
public class SaveApplicationDTO {

    @ApiModelProperty(value = "应用Id", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @ApiModelProperty(value = "部门名称",example = "Software Development Department")
    private String buName;

    @ApiModelProperty(value = "名称", example = "backend")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "描述", example = "backend of sca system")
    private String description;

    @ApiModelProperty(value = "类型",example = "UI")
    private String type;
}
