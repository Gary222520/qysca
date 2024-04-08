package nju.edu.cn.qysca.domain.spider.dos;


import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name= "plt_maven_visited_urls")
public class MavenVisitedUrlsDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="url",nullable = false,unique = true)
    @ApiModelProperty(value = "url", example = "https://repo1.maven.org/maven2/junit/junit/4.13/")
    private String url;

    @Column(name = "is_success",nullable = false)
    @ApiModelProperty(value = "是否爬取成功", example = "true")
    private Boolean isSuccess;

    @Column(name = "is_last_level",nullable = false)
    @ApiModelProperty(value = "是否是底层目录", example = "true")
    private Boolean isLastLevel;
}
