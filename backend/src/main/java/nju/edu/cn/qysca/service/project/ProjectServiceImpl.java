package nju.edu.cn.qysca.service.project;

import nju.edu.cn.qysca.dao.Mongo.ProjectDao;
import nju.edu.cn.qysca.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectDao projectDao;


    @Override
    public List<String> findAllDistinctProjectName() {
        List<String> projectNameList = projectDao.findAllDistinctProjectName();
        Set<String> projectNameSet = new HashSet<>();
        for (String projectName : projectNameList) {
            projectNameSet.add(JsonUtil.extractValue(projectName, "name"));
        }
        return new ArrayList<>(projectNameSet);
    }
}
