package nju.edu.cn.qysca.domain.application.dtos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.utils.excel.ExcelExport;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("应用依赖平铺信息（简明）Excel表DTO")
public class TableExcelBriefDTO {

    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
    @ExcelExport(value = "组织ID")
    private String cGroupId;

    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
    @ExcelExport(value = "工件ID")
    private String cArtifactId;

    @ApiModelProperty(value = "版本号", example = "2.5.15")
    @ExcelExport(value = "版本")
    private String cVersion;

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

    @ApiModelProperty(value = "类型", example = "类型")
    @ExcelExport(value = "类型")
    private String type;
}
