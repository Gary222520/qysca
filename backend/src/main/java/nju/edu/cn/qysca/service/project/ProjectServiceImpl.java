package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.dao.components.JavaOpenComponentDao;
import nju.edu.cn.qysca.dao.project.ProjectInfoDao;
import nju.edu.cn.qysca.dao.project.ProjectVersionDao;
import nju.edu.cn.qysca.domain.project.*;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private ProjectInfoDao projectInfoDao;

    @Autowired
    private ProjectVersionDao projectVersionDao;


    @Override
    public Boolean saveProject(SaveProjectDTO saveProjectDTO) {
        // 新建Mongodb项目信息
        ProjectInfoDO projectInfoDO = new ProjectInfoDO();
        projectInfoDO.setId(UUIDGenerator.getUUID());
        projectInfoDO.setName(saveProjectDTO.getName());
        projectInfoDao.save(projectInfoDO);
        // 新建Mongodb项目版本信息
        ProjectVersionDO projectVersionDO = new ProjectVersionDO();
        projectVersionDO.setName(saveProjectDTO.getName());
        projectVersionDO.setVersion(saveProjectDTO.getVersion());
        projectVersionDO.setLanguage(saveProjectDTO.getLanguage());
        projectVersionDO.setBuilder(saveProjectDTO.getBuilder());
        projectVersionDO.setScanner(saveProjectDTO.getScanner());
        projectVersionDO.setNote(saveProjectDTO.getNote());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        projectVersionDO.setTime(timeStamp);
        projectVersionDO.setState("RUNNING");
        projectVersionDao.save(projectVersionDO);
        return true;
    }

    @Override
    public List<String> findAllDistinctProjectName() {
        return null;
    }

    /**
     * 获取所有项目信息
     *
     * @return List<Project> 项目信息列表
     */
    @Override
    public List<ProjectInfoDO> getProjectList() {
        return null;
    }

}
