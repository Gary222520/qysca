package nju.edu.cn.qysca.utils;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import org.apache.maven.shared.invoker.*;

import java.io.*;
import java.util.Collections;
import java.util.List;

public class MavenUtil {

    /**
     * 根据pom文件路径在pom文件路径下生成result.txt文件，文件中记录了pom文件中所有依赖的树形结构
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

}
