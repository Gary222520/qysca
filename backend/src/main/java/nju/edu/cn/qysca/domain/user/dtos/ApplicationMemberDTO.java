package nju.edu.cn.qysca.domain.user.dtos;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "应用成员信息接口")
public class ApplicationMemberDTO {

    private String name;

    private String version;

    private String uid;
}
