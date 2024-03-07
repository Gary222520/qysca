package nju.edu.cn.qysca.service.application;


import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.application.ApplicationProjectDao;
import nju.edu.cn.qysca.dao.project.ProjectDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationProjectDO;
import nju.edu.cn.qysca.domain.application.dtos.AddProjectDTO;
import nju.edu.cn.qysca.domain.application.dtos.CreateAppProjectDTO;
import nju.edu.cn.qysca.domain.application.dtos.CreateApplicationDTO;
import nju.edu.cn.qysca.domain.application.dtos.DeleteProjectDTO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import nju.edu.cn.qysca.domain.project.dtos.SaveProjectDTO;
import nju.edu.cn.qysca.service.project.ProjectService;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ApplicationServiceImpl implements ApplicationService{

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private ApplicationProjectDao applicationProjectDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProjectService projectService;
    @Override
    @Transactional
    public Boolean createApplication(CreateApplicationDTO createApplicationDTO) {
        ApplicationDO applicationDO = new ApplicationDO();
        BeanUtils.copyProperties(createApplicationDTO, applicationDO);
        applicationDao.save(applicationDO);
        return true;
    }


    @Override
    @Transactional
    public Boolean deleteApplication(String groupId, String artifactId) {
        return null;
    }

    @Override
    @Transactional
    public Boolean deleteApplicationVersion(String groupId, String artifactId, String version) {
        applicationDao.deleteByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        return true;
    }

    @Override
    public Page<ApplicationDO> getApplicationList(Integer number, Integer size) {
        Pageable pageable = PageRequest.of(number - 1, size);
        return applicationDao.findLatestVersionOfAllProjects(pageable);
    }

    @Override
    public List<String> getApplicationVersionList(String groupId, String artifactId) {
        return applicationDao.findVersionsByGroupIdAndArtifactId(groupId, artifactId);
    }

    @Override
    public List<ProjectDO> getApplicationVersion(String groupId, String artifactId, String version) {
        return applicationProjectDao.findProjectByApplicationGAV(groupId, artifactId, version);
    }

    @Override
    @Transactional
    public Boolean addProject(AddProjectDTO addProjectDTO) {
        ApplicationProjectDO applicationProjectDO = new ApplicationProjectDO();
        applicationProjectDO.setId(UUIDGenerator.getUUID());
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(addProjectDTO.getAppGroupId(), addProjectDTO.getAppArtifactId(), addProjectDTO.getAppVersion());
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(addProjectDTO.getGroupId(), addProjectDTO.getArtifactId(), addProjectDTO.getVersion());
        applicationProjectDO.setApplicationDO(applicationDO);
        applicationProjectDO.setProjectDO(projectDO);
        applicationProjectDO.setDeleted(false);
        applicationProjectDao.save(applicationProjectDO);
        return true;
    }

    @Override
    @Transactional
    public Boolean deleteProject(DeleteProjectDTO deleteProjectDTO) {
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(deleteProjectDTO.getAppGroupId(), deleteProjectDTO.getAppArtifactId(), deleteProjectDTO.getAppVersion());
        ProjectDO projectDO = projectDao.findByGroupIdAndArtifactIdAndVersion(deleteProjectDTO.getGroupId(), deleteProjectDTO.getArtifactId(), deleteProjectDTO.getVersion());
        applicationProjectDao.deleteByApplicationDO_IdAndProjectDO_Id(applicationDO.getId(), projectDO.getId());
        return true;
    }

    @Override
    @Transactional
    public Boolean createAppProject(CreateAppProjectDTO createAppProjectDTO) {
        SaveProjectDTO saveProjectDTO = new SaveProjectDTO();
        BeanUtils.copyProperties(createAppProjectDTO, saveProjectDTO);
        projectService.saveProject(saveProjectDTO);
        AddProjectDTO addProjectDTO = new AddProjectDTO();
        BeanUtils.copyProperties(createAppProjectDTO, addProjectDTO);
        addProject(addProjectDTO);
        return true;
    }
}
