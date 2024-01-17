package nju.edu.cn.qysca.domain.component.dos;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("保存闭源组件DTO")
public class SaveCloseComponentDTO {
    @ApiModelProperty(value = "文件存储路径", example = "resources/static/upload/")
    String filePath;
}
