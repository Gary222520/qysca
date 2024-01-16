package nju.edu.cn.qysca.service.maven;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import nju.edu.cn.qysca.dao.component.JavaCloseComponentDao;
import nju.edu.cn.qysca.dao.component.JavaOpenComponentDao;
import nju.edu.cn.qysca.domain.component.JavaCloseComponentDO;
import nju.edu.cn.qysca.domain.component.JavaOpenComponentDO;
import nju.edu.cn.qysca.domain.component.LicenseDO;
import nju.edu.cn.qysca.domain.project.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.SpiderService;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
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

}
