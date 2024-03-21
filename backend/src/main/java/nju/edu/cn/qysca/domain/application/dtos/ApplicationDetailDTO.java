package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.user.dtos.UserBriefDTO;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "应用详细信息")
public class ApplicationDetailDTO {

    @ApiModelProperty(value = "应用信息")
    private ApplicationDO applicationDO;

    @ApiModelProperty(value = "应用成员信息")
    private List<UserBriefDTO> users;
}
