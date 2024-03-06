package nju.edu.cn.qysca.service.application;


import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.CreateApplicationDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ApplicationServiceImpl implements ApplicationService{

    @Autowired
    private ApplicationDao applicationDao;
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
    public Boolean deleteApplication(String groupId, String artifactId, String version) {
        applicationDao.deleteByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        return true;
    }

    @Override
    public List<ApplicationDO> getApplication(String groupId, String artifactId) {
        return applicationDao.findAllByGroupIdAndArtifactId(groupId, artifactId);
    }

    @Override
    public List<String> getApplicationVersionList(String groupId, String artifactId) {
        return applicationDao.findVersionsByGroupIdAndArtifactId(groupId, artifactId);
    }

    @Override
    public ApplicationDO getApplicationVersion(String groupId, String artifactId, String version) {
        return applicationDao.findByGroupIdAndArtifactIdAndVersionWithProject(groupId, artifactId, version);
    }
}
