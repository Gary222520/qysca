package nju.edu.cn.qysca.domain.components;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.*;

import java.io.Serializable;
import java.util.*;
import static org.springframework.data.neo4j.core.schema.Relationship.Direction.OUTGOING;

@Data
@Node("JavaComponent")
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "Java组件节点DO")
public class JavaComponentDO implements Serializable {
    @Id
    @GeneratedValue(generatorRef = "neo4jIdGenerator")
    @ApiModelProperty(value = "uuid", example = "0BAC7D48D1A8124D99F14805CE32DFF4")
    private String id;

    @Property
    @ApiModelProperty(value = "组织id", example = "org.hamcrest")
    private String groupId;

    @Property
    @ApiModelProperty(value = "工件id", example = "hamcrest-core")
    private String artifactId;

    @Property
    @ApiModelProperty(value = "版本号", example = "2.2")
    private String version;

    @Property
    @ApiModelProperty(value = "是否开源", example = "true")
    private boolean openSource=true;

    @Relationship(type = "depends", direction = OUTGOING)
    private Set<JavaComponentDO> dependencies = new HashSet<>();

    @Relationship(type = "hasParent", direction = OUTGOING)
    private Set<JavaComponentDO> parents = new HashSet<>();

    // 以下内容均为可选字段

    @Property
    @ApiModelProperty(value = "开源许可证名称", example = "{BSD License 3,Apache License 2.0}")
    private List<String> licenseNames = new ArrayList<>();

    @Property
    @ApiModelProperty(value = "开源许可证访问链接", example = "{http://opensource.org/licenses/BSD-3-Clause,https://www.apache.org/licenses/LICENSE-2.0.txt}")
    private List<String> licenseUrls = new ArrayList<>();

    @Property
    @ApiModelProperty(value = "名称", example = "Hamcrest Core")
    private String name;

    @Property
    @ApiModelProperty(value = "开发者", example = "{Joe Walnes,Nat Pryce,Steve Freeman}")
    private List<String> developers= new ArrayList<>();

    @Property
    @ApiModelProperty(value = "描述", example = "Core Hamcrest API - deprecated, please use \"hamcrest\" instead")
    private String description;

    @Property
    @ApiModelProperty(value = "组件连接", example = "http://hamcrest.org/JavaHamcrest/")
    private String url;

    @Property
    @ApiModelProperty(value = "获取地址链接", example = "https://repo1.maven.org/maven2/org/hamcrest/hamcrest-core/2.2/hamcrest-core-2.2.pom")
    private String pomUrl;
}
