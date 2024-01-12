package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.Mongo.ProjectDao;
import nju.edu.cn.qysca.domain.components.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.project.ProjectDO;
import nju.edu.cn.qysca.service.project.ProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class MongoProjectTest {

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ProjectService projectService;

    //@Documention('集合名'), 而不是@Documention(collection = '集合名'), 否则会新建和类名相同的新集合名字
    //MongoId具有唯一性，如果插入重复的id，会覆盖之前的数据
    //Mongodb的插入支持List类型
    //Mongodb的插入如果java对象没有赋值则没有这个field
    @Test
    public void save() {
        ProjectDO projectDO = new ProjectDO();
        projectDO.setId("0BAC7D48D1A8124D99F14805CE32DFF6");
        projectDO.setName("test");
        projectDO.setVersion("1.0.0");
        List<ComponentDependencyTreeDO> dependencies = new ArrayList<>();
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setName("dam-core");
        componentDependencyTreeDO.setVersion("1.0.0");
        List<ComponentDependencyTreeDO> componentDependencies = new ArrayList<>();
        ComponentDependencyTreeDO componentDependencyDO = new ComponentDependencyTreeDO();
        componentDependencyDO.setName("dam-core-dependency");
        componentDependencyDO.setVersion("1.0.0");
        componentDependencies.add(componentDependencyDO);
        componentDependencyTreeDO.setDependencies(componentDependencies);
        dependencies.add(componentDependencyTreeDO);
        projectDO.setDependencies(dependencies);
        projectDao.save(projectDO);
    }

    //没有的属性值为null
    //Mongodb能够很好的支持List类型
    @Test
    public void find() {
        ProjectDO projectDO = projectDao.findProjectDOByNameAndVersion("test", "2.0.0");
        System.out.println(projectDO);
        System.out.println(projectDO.getDependencies());
        System.out.println(projectDO.getDependencies().get(0).getDependencies().get(0).getName());
    }

    @Test
    public void findAll() {
        List<ProjectDO> projectDOList = projectDao.findAllByName("dam");
        for (ProjectDO projectDO : projectDOList) {
            System.out.println(projectDO);
        }
    }

    //mongodb的update使用的save接口
    //update操作会部分替换，而不会全量替换
    @Test
    public void update() {
        ProjectDO projectDO = projectDao.findProjectDOByNameAndVersion("test", "2.0.0");
        projectDO.setVersion("2.0.0");
        projectDO.setLanguage("Java");
        projectDao.save(projectDO);
    }

    //无需使用@Query进行条件查询的注解
    @Test
    public void delete() {
        projectDao.deleteProjectDOByNameAndVersion("dam", "1.0.0");
    }

    @Test
    public void deleteAll() {
        projectDao.deleteAllByName("dam");
    }

    @Test
    public void findAllDistinctProjectName() {
        List<String> projectNames = projectService.findAllDistinctProjectName();
        System.out.println(projectNames);
    }
}
