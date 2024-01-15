package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Document("project_version_info")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目版本信息")
public class ProjectVersionDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty("项目名称")
    private String name;

    @ApiModelProperty("版本号")
    private String version;

    @ApiModelProperty("项目语言")
    private String language;

    @ApiModelProperty("构建工具")
    private String builder;

    @ApiModelProperty("扫描工具")
    private String scanner;

    @ApiModelProperty("最近一次更新时间")
    private String time;

    @ApiModelProperty("备注")
    private String note;

    @ApiModelProperty("是否扫描完成")
    private Boolean analyzed;
}
