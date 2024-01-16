package nju.edu.cn.qysca.domain.project;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("project_version_info")
@ApiModel("项目版本信息DO")
public class ProjectVersionDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty(value = "项目名称",example = "qysca")
    private String name;

    @ApiModelProperty(value = "项目版本号",example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "项目语言",example = "java")
    private String language;

    @ApiModelProperty(value = "构建工具",example = "maven")
    private String builder;

    @ApiModelProperty(value = "扫描对象",example = "zip")
    private String scanner;

    @ApiModelProperty(value = "最近一次更新时间",example = "2024-01-17")
    private String time;

    @ApiModelProperty(value = "备注",example = "a software component analysis project")
    private String note;

    @ApiModelProperty(value = "扫描状态",example="SUCCESS,FAILED,RUNNING")
    private String state;
}
