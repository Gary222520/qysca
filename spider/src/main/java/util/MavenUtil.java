package util;

import dataAccess.MongoDBAccess;
import domain.component.ComponentDependencyTreeDO;
import domain.component.JavaCloseComponentDO;
import domain.component.JavaOpenComponentDO;
import domain.component.LicenseDO;
import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;
import spider.JavaOpenPomSpider;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class MavenUtil {

    /**
     * 根据pom文件路径在pom文件路径下生成result.txt文件，文件中记录了pom文件中所有依赖的树形结构
     * 调用 maven dependency:tree命令
     * Maven_HOME
     *
     * @param filePath
     */
    public static Node mavenDependencyTreeAnalyzer(String filePath) {
        InvocationRequest request = new DefaultInvocationRequest();
        File pom = new File(filePath);
        request.setPomFile(pom);
        request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
        Invoker invoker = new DefaultInvoker();
        invoker.setMavenHome(new File("D:\\apache-maven-3.8.6"));
        try {
            invoker.execute(request);
            // 获得result结果的路径
            FileInputStream fis = new FileInputStream(new File(pom.getParent() + File.separator + "result"));
            Reader reader = new BufferedReader(new InputStreamReader(fis));
            InputType type = InputType.TEXT;
            Parser parser = type.newParser();
            Node node = parser.parse(reader);
            return node;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 递归解析依赖树 返回根节点
     *
     * @param node
     * @param depth
     * @return
     */
    public ComponentDependencyTreeDO convertNode(Node node, int depth) {

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

            MongoDBAccess<JavaOpenComponentDO> javaOpenComponentDOMongoDBAccess = new MongoDBAccess<JavaOpenComponentDO>("java_component_open_detail", JavaOpenComponentDO.class);
            javaOpenComponentDO = javaOpenComponentDOMongoDBAccess.readByGAV(node.getGroupId(), node.getArtifactId(), node.getVersion());
            // 如果开源知识库中没有则查看闭源知识库
            if (javaOpenComponentDO == null) {
                MongoDBAccess<JavaCloseComponentDO> javaCloseComponentDOMongoDBAccess = new MongoDBAccess<JavaCloseComponentDO>("java_component_close_detail", JavaCloseComponentDO.class);
                javaCloseComponentDO = javaCloseComponentDOMongoDBAccess.readByGAV(node.getGroupId(), node.getArtifactId(), node.getVersion());
                // 如果闭源知识库中没有则爬取 爬取过程已经包含插入数据库的步骤
                if (javaCloseComponentDO == null) {
                    JavaOpenPomSpider javaOpenPomSpider = new JavaOpenPomSpider();
                    javaOpenComponentDO = javaOpenPomSpider.crawlByGav(node.getGroupId(), node.getArtifactId(), node.getVersion());
                }
                //如果爬虫没有爬到则扫描错误 通过抛出异常处理
            }

            //设置依赖树中的信息
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

        // 递归处理子节点
        for (Node child : node.getChildNodes()) {
            ComponentDependencyTreeDO childDependencyTreeDO = convertNode(child, depth + 1);
            componentDependencyTreeDO.getDependencies().add(childDependencyTreeDO);
        }

        return componentDependencyTreeDO;
    }

}
