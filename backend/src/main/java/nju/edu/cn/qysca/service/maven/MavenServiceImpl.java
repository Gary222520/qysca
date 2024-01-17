package nju.edu.cn.qysca.service.maven;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import nju.edu.cn.qysca.dao.component.JavaCloseComponentDao;
import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
import nju.edu.cn.qysca.dao.project.ProjectDependencyTableDao;
import nju.edu.cn.qysca.domain.component.dos.JavaCloseComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaOpenComponentDO;
import nju.edu.cn.qysca.domain.component.dos.LicenseDO;
import nju.edu.cn.qysca.domain.project.dos.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDependencyTableDO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.SpiderService;
import nju.edu.cn.qysca.utils.idGenerator.UUIDGenerator;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.*;

@Service
public class MavenServiceImpl implements MavenService {

    @Autowired
    private JavaOpenComponentDao javaOpenComponentDao;

    @Autowired
    private JavaCloseComponentDao javaCloseComponentDao;

    @Autowired
    private ProjectDependencyTableDao projectDependencyTableDao;


    @Autowired
    private SpiderService spiderService;

    /**
     * 解析pom文件
     *
     * @param filePath
     */
    @Override
    public ComponentDependencyTreeDO projectDependencyAnalysis(String filePath) throws Exception {
        Node node = mavenDependencyTreeAnalyzer(filePath);
        ComponentDependencyTreeDO componentDependencyTreeDO = convertNode(node, 0);
        return componentDependencyTreeDO;
    }

    /**
     * 调用maven dependency:tree解析依赖树
     *
     * @param filePath
     * @return Node
     * @throws Exception
     */
    private Node mavenDependencyTreeAnalyzer(String filePath) throws Exception {
        InvocationRequest request = new DefaultInvocationRequest();
        File pom = new File(filePath);
        request.setPomFile(pom);
        request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("D:\\apache-maven-3.8.6"));
        invoker.execute(request);
        // 获得result结果的路径
        FileInputStream fis = new FileInputStream(new File(pom.getParent() + File.separator + "result"));
        Reader reader = new BufferedReader(new InputStreamReader(fis));
        InputType type = InputType.TEXT;
        Parser parser = type.newParser();
        Node node = parser.parse(reader);
        return node;
    }

    /**
     * 递归解析依赖树 返回根节点
     *
     * @param node
     * @param depth
     * @return
     * @throws PlatformException
     */
    private ComponentDependencyTreeDO convertNode(Node node, int depth) throws PlatformException {
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setGroupId(node.getGroupId());
        componentDependencyTreeDO.setArtifactId(node.getArtifactId());
        componentDependencyTreeDO.setVersion(node.getVersion());
        componentDependencyTreeDO.setScope(node.getScope());
        componentDependencyTreeDO.setLanguage("Java");
        if (depth != 0) {
            // 从开源知识库中查找
            JavaOpenComponentDO javaOpenComponentDO = null;
            JavaCloseComponentDO javaCloseComponentDO = null;
            javaOpenComponentDO = javaOpenComponentDao.findByGroupIdAndArtifactIdAndVersion(node.getGroupId(), node.getArtifactId(), node.getVersion());
            // 如果开源知识库中没有则查看闭源知识库
            if (javaOpenComponentDO == null) {
                javaCloseComponentDO = javaCloseComponentDao.findByGroupIdAndArtifactIdAndVersion(node.getGroupId(), node.getArtifactId(), node.getVersion());
                // 如果闭源知识库中没有则爬取 爬取过程已经包含插入数据库的步骤
                if (javaCloseComponentDO == null) {
                    javaOpenComponentDO = spiderService.crawlByGav(node.getGroupId(), node.getArtifactId(), node.getVersion());
                }
                //如果爬虫没有爬到则扫描错误 通过抛出异常处理
            }
            //设置知识库中的信息
            if (javaOpenComponentDO != null) {
                componentDependencyTreeDO.setName(javaOpenComponentDO.getName());
                componentDependencyTreeDO.setOpensource(true);
                List<LicenseDO> licenses = javaOpenComponentDO.getLicenses();
                StringBuilder license = new StringBuilder();
                for (LicenseDO licenseDO : licenses) {
                    license.append(licenseDO.getLicenseName()).append(";");
                }
                componentDependencyTreeDO.setLicenses(license.toString());
            } else {
                componentDependencyTreeDO.setName(javaCloseComponentDO.getName());
                componentDependencyTreeDO.setOpensource(false);
                List<LicenseDO> licenses = javaCloseComponentDO.getLicenses();
                StringBuilder license = new StringBuilder();
                for (LicenseDO licenseDO : licenses) {
                    license.append(licenseDO.getLicenseName()).append(";");
                }
                componentDependencyTreeDO.setLicenses(license.toString());
            }
            componentDependencyTreeDO.setDirect(depth == 1);
        }
        componentDependencyTreeDO.setDepth(depth);
        for (Node child : node.getChildNodes()) {
            ComponentDependencyTreeDO childDependencyTreeDO = convertNode(child, depth + 1);
            componentDependencyTreeDO.getDependencies().add(childDependencyTreeDO);
        }
        return componentDependencyTreeDO;
    }

    /**
     * 保存项目依赖平铺表
     *
     * @param projectDependencyTreeDO
     */
    private void projectDependencyTable(ProjectDependencyTreeDO projectDependencyTreeDO) {
        List<ProjectDependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(projectDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            ProjectDependencyTableDO projectDependencyTableDO = new ProjectDependencyTableDO();
            projectDependencyTableDO.setId(UUIDGenerator.getUUID());
            projectDependencyTableDO.setProjectName(projectDependencyTreeDO.getName());
            projectDependencyTableDO.setProjectVersion(projectDependencyTreeDO.getVersion());
            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
            BeanUtils.copyProperties(componentDependencyTreeDO, projectDependencyTableDO);
            result.add(projectDependencyTableDO);
            queue.addAll(componentDependencyTreeDO.getDependencies());
        }
        projectDependencyTableDao.saveAll(result);
    }

    //爬取方法
    JavaOpenComponentDO crawl(String groupId, String artifactId, String version) throws PlatformException {
        return null;
    }
}
