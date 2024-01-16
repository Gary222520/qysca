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
@ApiModel("文件分块传输对象DO")
public class FileChunkDO {

    @ApiModelProperty(value = "文件MD5", example = "23we392px9420028407272043")
    private String identifier;

    @ApiModelProperty(value = "分块文件")
    private MultipartFile file;

    @ApiModelProperty(value = "当前分块序号", example = "1")
    private int chunkNumber;

    @ApiModelProperty(value = "分块大小", example = "1024")
    private long chunkSize;

    @ApiModelProperty(value = "当前分块大小", example = "1024")
    private long currentChunkSize;

    @ApiModelProperty(value = "文件总大小", example = "6000")
    private long totalSize;

    @ApiModelProperty(value = "文件总分片数", example = "8")
    private int totalChunks;

    @ApiModelProperty(value = "文件名", example = "pom.xml")
    private String filename;

    @ApiModelProperty(value = "相对路径", example = "poms/project1/1.8.7")
    private String relativePath;
}
