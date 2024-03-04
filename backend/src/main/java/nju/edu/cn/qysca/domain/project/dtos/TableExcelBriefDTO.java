//package nju.edu.cn.qysca.domain.project.dtos;
//
//import io.swagger.annotations.ApiModel;
//import io.swagger.annotations.ApiModelProperty;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import nju.edu.cn.qysca.utils.excel.ExcelExport;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@ApiModel("项目依赖平铺信息（简明）Excel表DTO")
//public class TableExcelBriefDTO {
//
//    @ApiModelProperty(value = "组件名称", example = "spring-boot-starter")
//    @ExcelExport(value = "组件名称")
//    private String name;
//
//    @ApiModelProperty(value = "组织id", example = "org.springframework.boot")
//    @ExcelExport(value = "组织ID")
//    private String groupId;
//
//    @ApiModelProperty(value = "工件id", example = "spring-boot-starter")
//    @ExcelExport(value = "工件ID")
//    private String artifactId;
//
//    @ApiModelProperty(value = "版本号", example = "2.5.15")
//    @ExcelExport(value = "版本")
//    private String version;
//
//    @ApiModelProperty(value = "语言", example = "java")
//    @ExcelExport(value = "语言")
//    private String language;
//
//    @ApiModelProperty(value = "是否直接依赖", example = "true")
//    @ExcelExport(value = "依赖方式", kv = "true-直接依赖;false-间接依赖")
//    private Boolean direct;
//
//    @ApiModelProperty(value = "依赖层级", example = "1")
//    @ExcelExport(value = "依赖层级")
//    private Integer depth;
//
//    @ApiModelProperty(value = "依赖范围", example = "compile")
//    @ExcelExport(value = "依赖范围")
//    private String scope;
//
//    @ApiModelProperty(value = "是否开源", example = "true")
//    @ExcelExport(value = "是否开源", kv = "true-开源;false-闭源")
//    private Boolean opensource;
//
//    @ApiModelProperty(value = "许可证", example = "Apache License, Version 2.0")
//    @ExcelExport(value = "许可证")
//    private String licenses;
//}
