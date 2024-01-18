package util;

import dataAccess.MongoDBAccess;
import domain.component.*;
import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import org.apache.maven.shared.invoker.*;
import org.springframework.beans.BeanUtils;
import spider.JavaOpenPomSpider;
import util.idGenerator.UUIDGenerator;

import java.io.*;
import java.util.*;

public class MavenUtil {

    /**
     * 根据pom文件路径在pom文件路径下生成result.txt文件，文件中记录了pom文件中所有依赖的树形结构
     * 调用 maven dependency:tree命令
     *
     * @param filePath
     */
    public static Node mavenDependencyTreeAnalyzer(String filePath) {
        InvocationRequest request = new DefaultInvocationRequest();
        File pom = new File(filePath);
        request.setPomFile(pom);
        request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
        Invoker invoker = new DefaultInvoker();
        String mavenHome = System.getenv("MAVEN_HOME");
        if (mavenHome != null && !mavenHome.isEmpty()) {
            // 使用环境变量中的MAVEN_HOME路径
            invoker.setMavenHome(new File(mavenHome));
        } else {
            System.err.println("MAVEN_HOME is not set in the environment variables.");
        }

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            // 设置InvocationOutputHandler为捕获输出，使得调用mvn dependency:tree后的输出重定向（不输出到终端）
            InvocationOutputHandler outputHandler = new PrintStreamHandler(printStream, true);
            invoker.setOutputHandler(outputHandler);

            invoker.execute(request);

            // 获得 result 结果的路径
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
     * 限时，120s = 2minutes
     */
    private static long timeoutMillis = 120000;

    /**
     * 与 mavenDependencyTreeAnalyzer方法功能类似，区别在于当调用mvn dependency:tree超时时，程序会重新调用该方法
     * @param filePath
     * @return
     */
    public static Node mavenDependencyTreeAnalyzer_restart_when_timeout(String filePath) {
        String mavenCommand = "mvn dependency:tree -DoutputFile=result -DoutputType=text";
        File pom = new File(filePath);

        try {
            ProcessBuilder processBuilder = new ProcessBuilder("cmd", "/c", mavenCommand);
            processBuilder.directory(pom.getParentFile());

            Process process = processBuilder.start();
            System.out.println("执行命令中："+ mavenCommand);

            // 等待进程执行完成或超时
            if (!waitForProcess(process, timeoutMillis)) {
                // 如果超时，强制终止进程并重新执行
                process.destroyForcibly();
                process = processBuilder.start();
                // 继续等待新进程执行完成或超时
                if (!waitForProcess(process, timeoutMillis)) {
                    // 如果仍然超时，返回 null 或者采取其他处理
                    return null;
                }
            }
            // 获得 result 结果的路径
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

            MongoDBAccess<JavaOpenComponentDO> javaOpenComponentDOMongoDBAccess = MongoDBAccess.getInstance("java_component_open_detail", JavaOpenComponentDO.class);
            javaOpenComponentDO = javaOpenComponentDOMongoDBAccess.readByGAV(node.getGroupId(), node.getArtifactId(), node.getVersion());
            // 如果开源知识库中没有则查看闭源知识库
            if (javaOpenComponentDO == null) {
                MongoDBAccess<JavaCloseComponentDO> javaCloseComponentDOMongoDBAccess = MongoDBAccess.getInstance("java_component_close_detail", JavaCloseComponentDO.class);
                javaCloseComponentDO = javaCloseComponentDOMongoDBAccess.readByGAV(node.getGroupId(), node.getArtifactId(), node.getVersion());
                // 如果闭源知识库中没有则爬取 爬取过程已经包含插入数据库的步骤
                if (javaCloseComponentDO == null) {
                    javaOpenComponentDO = JavaOpenPomSpider.getInstance().crawlByGav(node.getGroupId(), node.getArtifactId(), node.getVersion());
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
            } else if (javaCloseComponentDO != null) {
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

    public static List<JavaOpenDependencyTableDO> buildJavaOpenDependencyTable(JavaOpenDependencyTreeDO javaOpenDependencyTreeDO) {
        List<JavaOpenDependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(javaOpenDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            JavaOpenDependencyTableDO javaOpenDependencyTableDO = new JavaOpenDependencyTableDO();
            javaOpenDependencyTableDO.setId(UUIDGenerator.getUUID());
            javaOpenDependencyTableDO.setParentGroupId(javaOpenDependencyTreeDO.getGroupId());
            javaOpenDependencyTableDO.setParentArtifactId(javaOpenDependencyTreeDO.getArtifactId());
            javaOpenDependencyTableDO.setParentVersion(javaOpenDependencyTreeDO.getVersion());

            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
            BeanUtils.copyProperties(componentDependencyTreeDO, javaOpenDependencyTableDO);
            result.add(javaOpenDependencyTableDO);
            queue.addAll(componentDependencyTreeDO.getDependencies());
        }

        return result;
    }

    /**
     * 检查进程是否超时
     * @param process
     * @param timeoutMillis
     * @return
     */
    private static boolean waitForProcess(Process process, long timeoutMillis) {
        long startTime = System.currentTimeMillis();
        long elapsedTime;

        try {
            while (true) {
                if (!process.isAlive()) {
                    return true; // 进程执行完成
                }

                elapsedTime = System.currentTimeMillis() - startTime;

                if (elapsedTime > timeoutMillis) {
                    return false; // 超过时限
                }

                Thread.sleep(100); // 等待一段时间再检查进程状态
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}

