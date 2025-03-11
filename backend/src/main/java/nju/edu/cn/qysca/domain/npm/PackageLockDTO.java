package nju.edu.cn.qysca.domain.npm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "package-lock.json文件信息")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageLockDTO {
    @ApiModelProperty(value = "名称", example = "my-project")
    private String name;

    @ApiModelProperty(value = "版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "依赖信息")
    private Map<String, PackageLockDependencyDTO> dependencies = new HashMap<>();
}
