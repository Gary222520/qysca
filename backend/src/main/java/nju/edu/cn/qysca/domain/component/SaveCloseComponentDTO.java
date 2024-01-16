package nju.edu.cn.qysca.domain.component;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveCloseComponentDTO {
    /**
     * pom文件地址
     */
    String filePath;
}
