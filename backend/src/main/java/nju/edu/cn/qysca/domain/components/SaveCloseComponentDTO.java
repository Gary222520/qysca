package nju.edu.cn.qysca.domain.components;


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
