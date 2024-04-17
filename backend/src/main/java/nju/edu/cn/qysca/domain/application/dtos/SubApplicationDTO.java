package nju.edu.cn.qysca.domain.application.dtos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ApiModel(description = "子应用信息")
public class SubApplicationDTO {

    @ApiModelProperty(value = "子应用信息")
    private List<ApplicationDO> subApplication;

    @ApiModelProperty(value = "子组件信息")
    private Map<String, List<? extends ComponentDO>> subComponent;
}
