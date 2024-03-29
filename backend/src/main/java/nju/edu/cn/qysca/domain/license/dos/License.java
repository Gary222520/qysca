package nju.edu.cn.qysca.domain.license.dos;

import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name= "plt_license")
@TypeDefs({
        @TypeDef(name = "jsonb",typeClass = JsonBinaryType.class)
})
public class License {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="name",nullable = false)
    @ApiModelProperty(value = "许可证名称（id）", example = "Apache-2.0")
    private String name;

    @Column(name="full_name",nullable = false)
    @ApiModelProperty(value = "许可证全名", example = "Apache License 2.0")
    private String fullName;

    @Column(name="url",nullable = true)
    @ApiModelProperty(value = "许可证链接", example = "https://opensource.org/licenses/Apache-2.0")
    private String url;

    @Column(name="is_osi_approved",nullable = true)
    @ApiModelProperty(value = "OSI认证", example = "true")
    private Boolean isOsiApproved;

    @Column(name="is_fsf_approved",nullable = true)
    @ApiModelProperty(value = "FSF许可", example = "false")
    private Boolean isFsfApproved;

    @Column(name="is_spdx_approved",nullable = true)
    @ApiModelProperty(value = "SPDX认证", example = "true")
    private Boolean isSpdxApproved;

    @Column(name="risk_level",nullable = true)
    @ApiModelProperty(value = "风险等级", example = "high")
    private String riskLevel;

    @Column(name="risk_disclosure",nullable = true)
    @ApiModelProperty(value = "风险说明", example = "该协议不包含实质性的限制条款")
    private String riskDisclosure;

    @Column(name="gpl_compatibility",nullable = true)
    @ApiModelProperty(value = "GPL兼容性", example = "true")
    private Boolean gplCompatibility;

    @Column(name="gpl_compatibility_description",nullable = true)
    @ApiModelProperty(value = "GPL兼容性说明", example = "这是一个自由软件许可证，它兼容 GNU GPL v3。 请注意该许可证不兼容 GPL v2，因为它的一些要求没有包含在 GPL v2 中。这包括某些专利中止和侵害保护条款。其中的专利中止条款是不错的，因此我们推荐大型的软件可以使用 Apache 2.0 许可证而不是其他松散型许可证。")
    private String gplCompatibilityDescription;

    @Column(name="obligations_required",nullable = true)
    @ApiModelProperty(value = "必须义务")
    @Type(type="jsonb")
    private List<LicenseTerm> obligationsRequired;

    @Column(name="obligations_not_required",nullable = true)
    @ApiModelProperty(value = "无需义务")
    @Type(type="jsonb")
    private List<LicenseTerm> obligationsNotRequired;

    @Column(name="rights_allowed",nullable = true)
    @ApiModelProperty(value = "允许权利")
    @Type(type="jsonb")
    private List<LicenseTerm> rightsAllowed;

    @Column(name="rights_prohibited",nullable = true)
    @ApiModelProperty(value = "禁止权利")
    @Type(type="jsonb")
    private List<LicenseTerm> rightsProhibited;

    @Column(name="text",nullable = true)
    @ApiModelProperty(value = "许可证内容", example = "Apache License, Version 2.0 Apache License Version 2.0, January 2004 http://www.apache.org/licenses/ TERMS AND CONDITIONS FOR USE, REPRODUCTION, AND DISTRIBUTION 1\\. Definitions. \"License\" shall mean the terms and conditions for use, reproduction, and dist....")
    private String text;
}
