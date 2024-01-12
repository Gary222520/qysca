package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.components.JavaComponentDao;
import nju.edu.cn.qysca.domain.components.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.components.ComponentGavDTO;
import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import nju.edu.cn.qysca.service.components.ComponentsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class JavaComponentDOTest {

    @Autowired
    private ComponentsService componentsService;

    @Autowired
    private JavaComponentDao javaComponentDao;

    /**
     * 测试查询邻接的dao层方法
     */
    @Test
    public void test1(){
        String groupId="junit";
        String artifactId="junit";
        String version="4.13.1";
        List<JavaComponentDO> res1=javaComponentDao.findAdjacentDependencyByGAV(groupId,artifactId,version);
        List<JavaComponentDO> res2=javaComponentDao.findAdjacentParentByGAV(groupId,artifactId,version);
        System.out.println(res1.size());
        System.out.println(res2.size());
    }

    /**
     * 测试展示树形结构服务
     */
    @Test
    public void test2(){
        String groupId="org.slf4j";
        String artifactId="slf4j-api";
        String version="2.0.9";
        ComponentGavDTO componentGavDTO=new ComponentGavDTO(groupId,artifactId,version);
        ComponentDependencyTreeDO ans=componentsService.getComponentTreeByGAV(componentGavDTO);
        System.out.println(ans.getDependencies().size());
    }

    /**
     * 测试查询单个节点信息
     */
    @Test
    public void test3(){
        String groupId="org.slf4j";
        String artifactId="slf4j-api";
        String version="2.0.9";
        JavaComponentDO javaComponentDO=javaComponentDao.findNodeByGAV(groupId,artifactId,version);
        System.out.println(javaComponentDO.getDependencies().size());
    }

    /**
     * 测试查询邻接的dao层新方法
     */
    @Test
    public void test4(){
        String groupId="junit";
        String artifactId="junit";
        String version="4.13.1";
        JavaComponentDO res=javaComponentDao.findAdjacentRNByGAV(groupId,artifactId,version);
        System.out.println(res.getDependencies().size());
    }
}
