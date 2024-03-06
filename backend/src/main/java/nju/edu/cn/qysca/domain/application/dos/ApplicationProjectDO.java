package nju.edu.cn.qysca.domain.application.dos;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import org.apache.poi.sl.usermodel.ObjectMetaData;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "application_project")
public class ApplicationProjectDO {

    @Id
    @Column(name="id",nullable = false)
    @GenericGenerator(name = "jpa-uuid", strategy = "uuid")
    @GeneratedValue(generator = "jpa-uuid")
    @ApiModelProperty(value = "uuid", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private String id;

    @ManyToOne
    @JoinColumn(name = "application_id")
    @Column(name = "application_id", nullable = false)
    @ApiModelProperty(value = "应用id", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private ApplicationDO applicationDO;

    @ManyToOne
    @JoinColumn(name = "project_id")
    @Column(name = "project_id", nullable = false)
    @ApiModelProperty(value = "项目id", example = "123e456-e74-b37-4d7a-9421d59bf3b")
    private ProjectDO projectDO;

    @Column(name = "deleted", nullable = false)
    @ApiModelProperty(value = "是否删除", example = "true")
    private Boolean deleted;
}
