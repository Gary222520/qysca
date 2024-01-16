package nju.edu.cn.qysca.domain.components;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;

@Document("java_component_close_detail")
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("java闭源组件详情")
public class JavaCloseComponentDO {

    @MongoId
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    @Field("_id")
    private String id;

    @ApiModelProperty(value = "组件id", example = "")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "")
    private String artifactId;

    @ApiModelProperty(value = "组件版本", example = "")
    private String version;

    @ApiModelProperty(value = "组件名称", example = "")
    private String name;

    @ApiModelProperty(value = "组件描述", example = "")
    private String description;

    @ApiModelProperty(value = "组件主页地址")
    private String url;

    @ApiModelProperty(value = "下载地址")
    private String downloadUrl;

    @ApiModelProperty(value = "源码地址")
    private String sourceUrl;

    @ApiModelProperty(value = "开发者列表")
    private List<DeveloperDO> developers;

    @ApiModelProperty(value = "许可证列表")
    private List<LicenseDO> licenses;

    @ApiModelProperty(value = "pom文件")
    private String pom;

    @ApiModelProperty(value = "扫描状态",example="SUCCESS,FAILED,RUNNING")
    private String state;

}
