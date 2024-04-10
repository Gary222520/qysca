package nju.edu.cn.qysca.domain.spider.dos;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

/**
 * 用以记录爬取过的python包
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name= "plt_visited_python_packages", uniqueConstraints = @UniqueConstraint(columnNames = {"name", "version"}))
public class PythonVisitedPackagesDO {
    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @Column(name="name",nullable = false)
    @ApiModelProperty(value = "包名称", example = "zxshareProject")
    private String name;

    @Column(name="version",nullable = false)
    @ApiModelProperty(value = "版本", example = "-")
    private String version;

    @Column(name = "visited",nullable = false)
    @ApiModelProperty(value = "是否访问过", example = "true")
    private Boolean visited;

    @Column(name = "is_success",nullable = false)
    @ApiModelProperty(value = "是否爬取成功", example = "true")
    private Boolean isSuccess;
}
