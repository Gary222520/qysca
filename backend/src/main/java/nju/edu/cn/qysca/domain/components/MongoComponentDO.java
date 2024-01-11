package nju.edu.cn.qysca.domain.components;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("components")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MongoComponentDO {

    @Id
    @ApiModelProperty(value = "组件id", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    private String Id;

    @ApiModelProperty(value = "组件标签", example = "JavaComponent")
    private String label;

    @ApiModelProperty(value = "组件作者", example = "QYSC")
    private String author;

    @ApiModelProperty(value = "", example = "org.hamcrest")
    private String groupId;

    @ApiModelProperty(value = "组件名称", example = "Hamcrest Core")
    private String name;

    @ApiModelProperty(value = "组件描述", example = "hamcrest-core")
    private String description;

    @ApiModelProperty(value = "", example = "hamcrest-core")
    private String artifactId;

    @ApiModelProperty(value = "组件版本", example = "1.3")
    private String version;

    @ApiModelProperty(value = "组件下载地址", example = "https://repo1.maven.org/maven2/org/hamcrest/ham")
    private String url;

    @ApiModelProperty(value = "组件许可证", example = "Apache License 2.0")
    private List<String> licenseName;

    @ApiModelProperty(value = "组件许可证URL", example = "https://www.apache.org/licenses/LICENSE-2.0.txt")
    private List<String> licenseUrl;

}
