package nju.edu.cn.qysca.domain.sbom;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.component.dos.HashDO;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("Sbom-js组件DTO")
public class SbomJsComponentDTO extends SbomComponentDTO{

    @ApiModelProperty(value = "来源", example = "opensource")
    private String origin;

    @ApiModelProperty(value = "purl",example = "pkg:golang/github.com/bi-zone/go-winio@v0.4.15")
    private String pUrl;

    @ApiModelProperty(value = "命名空间", example = "github.com")
    private String namespace;

    @ApiModelProperty(value = "工件id", example = "go-winio")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "v0.4.15")
    private String version;

    @ApiModelProperty(value = "语言", example = "golang")
    private String primaryLanguage;

    @ApiModelProperty(value = "组件描述", example = "Core starter, including auto-configuration support, logging and YAML")
    private String description;

    @ApiModelProperty(value = "许可证列表")
    private List<String> licenses = new ArrayList<>();

    @ApiModelProperty(value = "是否直接依赖", example = "true")
    private boolean directDependency;

    @ApiModelProperty(value = "网站网址")
    private String website;

    @ApiModelProperty(value = "仓库url")
    private String repoUrl;

    @ApiModelProperty(value = "下载url")
    private String downloadUrl;

    @ApiModelProperty(value = "版权声明")
    private String[] copyrightStatements;

}
