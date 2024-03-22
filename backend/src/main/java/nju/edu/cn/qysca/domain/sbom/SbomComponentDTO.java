package nju.edu.cn.qysca.domain.sbom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.component.dos.HashDO;
import nju.edu.cn.qysca.domain.component.dos.LicenseDO;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("Sbom组件DTO")
public class SbomComponentDTO {
    @ApiModelProperty(value = "组织Id", example = "nju.edu.cn")
    private String groupId;

    @ApiModelProperty(value = "工件Id", example = "qysca")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "组件类型", example = "opensource")
    private String type;

    @ApiModelProperty(value = "组件描述", example = "Core starter, including auto-configuration support, logging and YAML")
    private String description;

    @ApiModelProperty(value = "包获取地址",example = "pkg:maven/io.grpc/grpc-protobuf@1.44.1?type=jar")
    private String pUrl;

    @ApiModelProperty(value = "许可证列表")
    private List<LicenseDO> licenses = new ArrayList<>();

    @ApiModelProperty(value = "哈希值列表")
    private List<HashDO> hashes = new ArrayList<>();

    @ApiModelProperty(value = "外部链接")
    private List<SbomExternalReferenceDTO> externalReferences = new ArrayList();
}
