package nju.edu.cn.qysca.service.application;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.CreateApplicationDTO;

import java.util.List;

public interface ApplicationService {


    Boolean createApplication(CreateApplicationDTO createApplicationDTO);

    Boolean deleteApplication(String groupId, String artifactId, String version);

    List<ApplicationDO> getApplication(String groupId, String artifactId);

    List<String> getApplicationVersionList(String groupId, String artifactId);

    ApplicationDO getApplicationVersion(String groupId, String artifactId, String version);
}
