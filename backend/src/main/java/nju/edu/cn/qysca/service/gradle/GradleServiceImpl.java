package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class GradleServiceImpl implements GradleService{

    @Override
    public ComponentDependencyTreeDO projectDependencyAnalysis(String filePath) {
        return null;
    }

    /**
     * 解析gradle依赖树文件
     * @param filePath
     * @return
     */
    public List<ComponentDependencyTreeDO> gradleDependencyTreeAnalyze(String filePath){

        List<String> lines = readLinesFromFile(filePath);
        // 用以记录直接依赖
        Set<String> visited = new HashSet<>();
        List<ComponentDependencyTreeDO> trees = new ArrayList<>();
        int begin = 0;
        // 扫描文件，找出依赖树形式的文本块进行解析
        while(begin < lines.size()){
            while (begin < lines.size() && !(lines.get(begin).startsWith("+---") || lines.get(begin).startsWith("\\---"))){
                begin++;
            }
            if (begin >= lines.size())
                break;
            int end = begin;
            while (end < lines.size() && !lines.get(end).replaceAll(" ", "").isEmpty()){
                end++;
            }
            trees.addAll(parseTree(visited, lines.subList(begin, end), 1));
            begin = end;
        }
        return trees;
    }

    /**
     * 对一段依赖树进行递归解析
     * @param lines
     * @param depth
     * @return
     */
    private List<ComponentDependencyTreeDO> parseTree(Set<String> visited, List<String> lines, int depth){
        List<ComponentDependencyTreeDO> trees = new ArrayList<>();
        int begin = 0;
        while (begin < lines.size()){
            // 从依赖树文本块中分割出单个组件依赖树
            int end = begin + 1;
            while (end < lines.size() && !(lines.get(end).startsWith("+---") || lines.get(end).startsWith("\\---"))){
                end++;
            }
            // 提取组件信息
            ComponentDependencyTreeDO tree = new ComponentDependencyTreeDO();
            tree.setDepth(depth);
            setTreeInformation(tree, lines.get(begin).substring(5));
            // 如果该直接依赖已记录过，或者该组件的gav缺失，则跳过
            if (!(depth == 1 && visited.contains(tree.getGroupId() + ":" + tree.getArtifactId() + ":" + tree.getVersion())) && !(tree.getGroupId() == null || tree.getArtifactId() == null || tree.getVersion() == null) && !(tree.getGroupId().isEmpty() || tree.getArtifactId().isEmpty() || tree.getVersion().isEmpty()))
            {
                if (depth==1)
                    visited.add(tree.getGroupId() + ":" + tree.getArtifactId() + ":" + tree.getVersion());

                //todo 查数据库和爬取


                // 递归解析该组件的子依赖树文本块
                if (begin < end) {
                    tree.setDependencies(parseTree(visited, lines.subList(begin + 1, end).stream().map(s -> s.substring(5)).collect(Collectors.toList()), depth + 1));
                }
                trees.add(tree);
            }
            begin = end;
        }
        return trees;
    }

    public static List<String> readLinesFromFile(String filePath) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    private void setTreeInformation(ComponentDependencyTreeDO tree, String line){
        // example:
        // "org.springframework.boot:spring-boot-starter:3.2.3",
        // "org.springframework:spring-core:6.1.4 (*)",
        // "org.springframework.boot:spring-boot-starter-jdbc -> 3.2.3",
        // "com.netease.cloudmusic.android:module_a:1.0.0 -> 1.1.0",
        // "com.netease.cloudmusic.android:module_c:1.1.0 (c)",
        // "com.netease.cloudmusic.android:module_c:1.2.0 -> 1.3.0 (c)",
        // "com.netease.cloudmusic.android:module_c:{strictly 1.0.0} -> 1.0.0",
        // "org.springframework:spring-core (n)"

        if (line.contains("(n)")){
            return;
        }
        tree.setGroupId(line);
        tree.setArtifactId(line);
        tree.setVersion(line);
//        String pattern = "^(<groupId>[^:]+):(<artifactId>[^:\\s]+):?(<version>[.]+)";
//        Pattern r = Pattern.compile(pattern);
//        Matcher m = r.matcher(line);
//        if (m.find()) {
//            String groupId = m.group("groupId");
//            String artifactId = m.group("artifactId");
//            String version = m.group("version");
//            tree.setGroupId(groupId);
//            tree.setArtifactId(artifactId);
//            tree.setVersion(version);
//        }
    }

    public static void main(String[] args) {
        GradleServiceImpl gradleService = new GradleServiceImpl();
        List<ComponentDependencyTreeDO> trees = gradleService.gradleDependencyTreeAnalyze("C:\\Users\\ASUS\\Desktop\\1.txt");
        System.out.println(trees);
    }
}
