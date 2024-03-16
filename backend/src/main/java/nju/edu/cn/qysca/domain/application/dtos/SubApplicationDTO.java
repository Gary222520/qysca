package nju.edu.cn.qysca.domain.application.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "子应用信息")
public class SubApplicationDTO {

    @ApiModelProperty(value = "子应用信息", example = "['123e456-e74-b37-4d7a-9421d59bf3b',]")
    private List<ApplicationDO> subapplication;

    @ApiModelProperty(value = "子组件信息",example = "['123e456-e74-b37-4d7a-9421d59bf3b',]")
    private List<ComponentDO> subComponent;
}
