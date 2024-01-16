package nju.edu.cn.qysca.service.project;

import fr.dutra.tools.maven.deptree.core.Node;
import nju.edu.cn.qysca.dao.components.OpenSourceComponentDao;
import nju.edu.cn.qysca.domain.components.JavaOpenComponentDO;
import nju.edu.cn.qysca.domain.project.*;
import nju.edu.cn.qysca.utils.MavenUtil;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private OpenSourceComponentDao openSourceComponentDao;


    @Override
    public Boolean saveProject(SaveProjectDTO saveProjectDTO) {
        // 解析前端上传的pom文件路径，拿到Node节点
        Node node = MavenUtil.mavenDependencyTreeAnalyzer(saveProjectDTO.getFilePath());
        // 新建Mongodb项目信息
        ProjectInfoDO projectInfoDO = new ProjectInfoDO();
        projectInfoDO.setId(UUIDGenerator.getUUID());
        projectInfoDO.setName(saveProjectDTO.getName());
        List<ProjectVersionDO> versions = new ArrayList<>();
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
        versions.add(projectVersionDO);
        // 获得树状信息
        ComponentDependencyTreeDO componentDependencyTreeDO = convertNode(node, 0);
        ProjectDependencyTreeDO projectDependencyTreeDO = new ProjectDependencyTreeDO();
        projectDependencyTreeDO.setId(UUIDGenerator.getUUID());
        projectDependencyTreeDO.setName(saveProjectDTO.getName());
        projectDependencyTreeDO.setVersion(saveProjectDTO.getVersion());
        projectDependencyTreeDO.setTree(componentDependencyTreeDO);
        //平铺信息处理
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

    private ComponentDependencyTreeDO convertNode(Node node, int depth) {
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setGroupId(node.getGroupId());
        componentDependencyTreeDO.setArtifactId(node.getArtifactId());
        componentDependencyTreeDO.setVersion(node.getVersion());
        componentDependencyTreeDO.setScope(node.getScope());
        // 从开源知识库中查找
        JavaOpenComponentDO javaOpenComponentDO = openSourceComponentDao.findByGroupIdAndArtifactIdAndVersion(node.getGroupId(), node.getArtifactId(), node.getVersion());
        // 如果开源知识库中没有则通过爬虫并增量写入知识库
        if (javaOpenComponentDO == null) {
            JavaOpenComponentDO spider = null;
            //如果爬虫没有爬到则视为闭源数据库，从闭源数据库拿并增量更新

        }
        componentDependencyTreeDO.setDirect(depth == 1);
        componentDependencyTreeDO.setDepth(depth);
        for (Node child : node.getChildNodes()) {
            ComponentDependencyTreeDO childDependencyTreeDO = convertNode(child, depth + 1);
            componentDependencyTreeDO.getDependencies().add(childDependencyTreeDO);
        }
        return componentDependencyTreeDO;

    }
}
