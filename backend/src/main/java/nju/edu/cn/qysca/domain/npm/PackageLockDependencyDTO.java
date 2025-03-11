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
@ApiModel(description = "package-lock.json文件中的依赖项")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PackageLockDependencyDTO {

    @ApiModelProperty(value = "版本", example = "1.0.0")
    private String version;

    @ApiModelProperty(value = "对于注册表路径", example = "https://registry.npmjs.org/zrender/-/zrender-4.3.2.tgz")
    private String resolved;

    @ApiModelProperty(value = "资源完整性字符串", example = "sha512-bIusJLS8c4DkIcdiK+s13HiQ/zjQQVgpNohtd8d94Y2DnJqgM1yjh/jpDb8DoL6hd7r8Awagw8e3qK/oLaWr3g==")
    private String integrity;

    @ApiModelProperty(value = "依赖的其他包及版本要求")
    private Map<String,String> requires = new HashMap<>();

    @ApiModelProperty(value = "当前包实际安装的依赖项目及其确切版本")
    private Map<String, PackageLockDependencyDTO> dependencies = new HashMap<>();

}
