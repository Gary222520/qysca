package nju.edu.cn.qysca.service.project;

import fr.dutra.tools.maven.deptree.core.Node;
import nju.edu.cn.qysca.dao.component.OpenSourceComponentDao;
import nju.edu.cn.qysca.dao.project.ProjectInfoDao;
import nju.edu.cn.qysca.dao.project.ProjectVersionDao;
import nju.edu.cn.qysca.domain.component.JavaOpenComponentDO;
import nju.edu.cn.qysca.domain.project.*;
import nju.edu.cn.qysca.utils.MavenUtil;
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

    @Autowired
    private OpenSourceComponentDao openSourceComponentDao;

    @Autowired
    private ProjectVersionDao projectVersionDao;

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
