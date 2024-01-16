package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
import nju.edu.cn.qysca.dao.project.ProjectInfoDao;
import nju.edu.cn.qysca.dao.project.ProjectVersionDao;
import nju.edu.cn.qysca.domain.project.*;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectInfoDao projectInfoDao;

    private JavaOpenComponentDao javaOpenComponentDao;

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

    /**
     * 分页获取项目信息
     *
     * @param name 项目名称
     * @param number 页码
     * @param size 页大小
     * @return Page<ProjectVersionDO> 项目信息分页结果
     */
    @Override
    public Page<ProjectInfoDO> findProjectInfoPage(String name, int number, int size) {
        // 模糊查询，允许参数name为空值
        ExampleMatcher matcher=ExampleMatcher.matching().withIgnorePaths("id").withIgnoreNullValues();
        ProjectInfoDO projectInfoDO=new ProjectInfoDO();
        if(name!=null && !name.equals("")){
            projectInfoDO.setName(name);
        }
        Example<ProjectInfoDO> example=Example.of(projectInfoDO,matcher);
        // 数据库页号从0开始，需减1
        Pageable pageable=PageRequest.of(number-1,size);
        return projectInfoDao.findAll(example,pageable);
    }

    /**
     * 分页获取指定项目的版本信息
     *
     * @param name   项目名称
     * @param number 页码
     * @param size   页大小
     * @return Page<ProjectVersionDO> 项目版本信息分页结果
     */
    @Override
    public Page<ProjectVersionDO> findProjectVersionPage(String name, int number, int size) {
        // 数据库页号从0开始，需减1
        return projectVersionDao.findAllByName(name,PageRequest.of(number-1,size,Sort.by(Sort.Order.desc("version").nullsLast())));
    }

}
