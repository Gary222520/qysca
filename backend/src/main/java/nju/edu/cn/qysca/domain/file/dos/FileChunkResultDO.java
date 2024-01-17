package nju.edu.cn.qysca.domain.file.dos;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文件分片传输结果DO")
public class FileChunkResultDO {

    @ApiModelProperty(value = "是否跳过上传", example = "true")
    private Boolean skipUpload;

    @ApiModelProperty(value = "已上传分片的集合")
    private Set<Integer> uploaded;
}
