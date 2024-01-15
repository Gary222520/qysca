package nju.edu.cn.qysca.domain.file;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel("文件分块传输对象")
public class FileChunkDO {

    @ApiModelProperty("文件MD5")
    private String identifier;

    @ApiModelProperty("分块文件")
    private MultipartFile file;

    @ApiModelProperty("当前分块序号")
    private int chunkNumber;

    @ApiModelProperty("分块大小")
    private long chunkSize;

    @ApiModelProperty("当前分块大小")
    private long currentChunkSize;

    @ApiModelProperty("文件总大小")
    private long totalSize;

    @ApiModelProperty("文件总分片数")
    private int totalChunks;

    @ApiModelProperty("文件名")
    private String filename;

    @ApiModelProperty("相对路径")
    private String relativePath;
}
