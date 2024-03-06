package nju.edu.cn.qysca.service.application;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.AddProjectDTO;
import nju.edu.cn.qysca.domain.application.dtos.CreateApplicationDTO;
import nju.edu.cn.qysca.domain.application.dtos.DeleteProjectDTO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ApplicationService {


    Boolean createApplication(CreateApplicationDTO createApplicationDTO);

    Boolean deleteApplication(String groupId, String artifactId);
    Boolean deleteApplicationVersion(String groupId, String artifactId, String version);

    Page<ApplicationDO> getApplicationList(Integer number, Integer size);

    List<String> getApplicationVersionList(String groupId, String artifactId);

    List<ProjectDO> getApplicationVersion(String groupId, String artifactId, String version);

    Boolean addProject(AddProjectDTO addProjectDTO);

    Boolean deleteProject(DeleteProjectDTO deleteProjectDTO);
}
