package nju.edu.cn.qysca.domain.project.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.utils.excel.ExcelExport;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("项目依赖平铺信息（详细）Excel表DTO")
public class TableExcelDetailDTO {
    @ApiModelProperty(value = "组件名称", example = "spring-boot-starter")
    @ExcelExport(value = "组件名称")
    private String name;

    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
    @ExcelExport(value = "组织ID")
    private String groupId;

    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
    @ExcelExport(value = "工件ID")
    private String artifactId;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    @ExcelExport(value = "版本")
    private String version;

    @ApiModelProperty(value = "语言", example = "java")
    @ExcelExport(value = "语言")
    private String language;

    @ApiModelProperty(value = "是否直接依赖", example = "true")
    @ExcelExport(value = "依赖方式", kv = "true-直接依赖;false-间接依赖")
    private Boolean direct;

    @ApiModelProperty(value = "依赖层级", example = "1")
    @ExcelExport(value = "依赖层级")
    private Integer depth;

    @ApiModelProperty(value = "依赖范围", example = "compile")
    @ExcelExport(value = "依赖范围")
    private String scope;

    @ApiModelProperty(value = "是否开源", example = "true")
    @ExcelExport(value = "是否开源", kv = "true-开源;false-闭源")
    private Boolean opensource;

    @ApiModelProperty(value = "组件描述", example = "Core starter, including auto-configuration support, logging and YAML")
    @ExcelExport(value = "组件描述")
    private String description;

    @ApiModelProperty(value = "组件主页地址", example = "https://spring.io/projects/spring-boot")
    @ExcelExport(value = "主页地址")
    private String url;

    @ApiModelProperty(value = "下载地址", example = "https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter/2.5.15/spring-boot-starter-2.5.15.pom")
    @ExcelExport(value = "下载地址")
    private String downloadUrl;

    @ApiModelProperty(value = "源码地址", example = "https://github.com/spring-projects/spring-boot")
    @ExcelExport(value = "源码地址")
    private String sourceUrl;

    @ApiModelProperty(value = "许可证名称", example = "Eclipse Public License v2.0")
    @ExcelExport(value = "许可证名称")
    private String licensesName;

    @ApiModelProperty(value = "许可证URL", example = "https://www.eclipse.org/legal/epl-v20.html")
    @ExcelExport(value = "许可证地址")
    private String licensesUrl;

    @ApiModelProperty(value = "开发者id", example = "piv")
    @ExcelExport(value = "开发者ID")
    private String developersId;

    @ApiModelProperty(value = "开发者名称", example = "Pivotal")
    @ExcelExport(value = "开发者名称")
    private String developersName;

    @ApiModelProperty(value = "开发者邮箱", example = "info@pivotal.io")
    @ExcelExport(value = "开发者邮箱")
    private String developersEmail;
}
