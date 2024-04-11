package nju.edu.cn.qysca.domain.sbom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.component.dos.HashDO;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("Sbom组件DTO")
public class SbomAppComponentDTO extends SbomComponentDTO{
    @ApiModelProperty(value = "名称", example = "qysca")
    private String name;

    @ApiModelProperty(value = "版本号", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "组件类型", example = "opensource")
    private String type;

    @ApiModelProperty(value = "语言", example = "java")
    private String language;

    @ApiModelProperty(value = "组件描述", example = "Core starter, including auto-configuration support, logging and YAML")
    private String description;

    @ApiModelProperty(value = "purl",example = "pkg:maven/io.grpc/grpc-protobuf@1.44.1?type=jar")
    private String pUrl;

    @ApiModelProperty(value = "许可证列表")
    private List<String> licenses = new ArrayList<>();

    @ApiModelProperty(value = "哈希值列表")
    private List<HashDO> hashes = new ArrayList<>();

    @ApiModelProperty(value = "外部链接")
    private List<SbomExternalReferenceDTO> externalReferences;
}
