package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.SpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class GradleServiceImpl implements GradleService{

    @Autowired
    private SpiderService spiderService;

    @Autowired
    private JavaComponentDao javaComponentDao;

    /**
     *
     * @param filePath gradle项目地址
     * @return root ComponentDependencyTreeDO
     */
    @Override
    public ComponentDependencyTreeDO projectDependencyAnalysis(String filePath) {
        List<String> lines = new ArrayList<>();
        try{
            File file = new File(filePath);
            // 创建命令 ./gradlew dependency
            List<String> command = List.of("./gradlew", "dependency");
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(file);
            // 启动命令
            Process process = processBuilder.start();
            // 直接将命令执行结果保存在lines中，没有生成中间文件
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            // 等待命令执行完毕
            int exitCode = process.waitFor();
        } catch (IOException | InterruptedException e){
            throw new PlatformException(500, "gradle项目解析失败");
        }
        List<ComponentDependencyTreeDO> trees = gradleDependencyTreeAnalyze(lines);
        ComponentDependencyTreeDO root = new ComponentDependencyTreeDO();
        // todo root的信息设置（应该存项目的信息）
        root.setDependencies(trees);
        root.setDepth(0);
        return root;
    }

    /**
     * 解析gradle依赖树文件
     * @param lines 带解析内容 整个文件 List<String>
     * @return List<ComponentDependencyTreeDO>
     * @throws PlatformException PlatformException(500, "存在未识别的组件")
     */
    public List<ComponentDependencyTreeDO> gradleDependencyTreeAnalyze(List<String> lines) throws PlatformException{

        // 用以记录直接依赖
        Set<String> visited = new HashSet<>();
        List<ComponentDependencyTreeDO> trees = new ArrayList<>();
        int begin = 0;
        // 扫描文件，找出依赖树形式的文本块进行解析
        while(begin < lines.size()){
            // 通过begin和end两个指针来确定依赖树文本块的位置
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
     * @param lines 文本行
     * @param depth 深度
     * @return List<ComponentDependencyTreeDO>
     * @throws PlatformException PlatformException(500, "存在未识别的组件")
     */
    private List<ComponentDependencyTreeDO> parseTree(Set<String> visited, List<String> lines, int depth) throws PlatformException{
        List<ComponentDependencyTreeDO> trees = new ArrayList<>();
        int begin = 0;
        while (begin < lines.size()){
            // 通过begin和end两个指针，从依赖树文本块中分割出单个组件依赖树
            int end = begin + 1;
            while (end < lines.size() && !(lines.get(end).startsWith("+---") || lines.get(end).startsWith("\\---"))){
                end++;
            }
            // 提取组件信息
            ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
            componentDependencyTreeDO.setDepth(depth);
            setTreeInformation(componentDependencyTreeDO, lines.get(begin).substring(5));
            String groupId = componentDependencyTreeDO.getGroupId();
            String artifactId = componentDependencyTreeDO.getArtifactId();
            String version = componentDependencyTreeDO.getVersion();

            // 如果该直接依赖已记录过，或者该组件的gav缺失，则跳过
            if (!(depth == 1 && visited.contains(groupId + ":" + artifactId + ":" + version)) && !(groupId == null || artifactId == null || version == null) && !(groupId.isEmpty() || artifactId.isEmpty() || version.isEmpty()))
            {
                if (depth==1)
                    visited.add(groupId + ":" + artifactId + ":" + version);

                // 查知识库
                JavaComponentDO javaComponentDO = javaComponentDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
                // 如果知识库没有则爬取
                if (javaComponentDO == null){
                    javaComponentDO = spiderService.crawlByGav(groupId, artifactId, version);
                    if (javaComponentDO != null){
                        componentDependencyTreeDO.setType("opensource");
                    } else{
                        componentDependencyTreeDO.setType("opensource");
                        //如果爬虫没有爬到则扫描错误 通过抛出异常处理
                        throw new PlatformException(500, "存在未识别的组件");
                    }
                } else {
                    componentDependencyTreeDO.setType(javaComponentDO.getType());
                }


                // 递归解析该组件的子依赖树文本块
                if (begin < end) {
                    componentDependencyTreeDO.setDependencies(parseTree(visited, lines.subList(begin + 1, end).stream().map(s -> s.substring(5)).collect(Collectors.toList()), depth + 1));
                }
                trees.add(componentDependencyTreeDO);
            }
            begin = end;
        }
        return trees;
    }

    /**
     * 从文件中读取每一行
     * @param filePath 文件地址
     * @return lines List<String>
     */
    private static List<String> readLinesFromFile(String filePath) {
        List<String> lines = null;
        try {
            lines = Files.readAllLines(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lines;
    }

    /**
     * 使用正则表达式从每行信息中提取出组件的gav，并存入tree中
     *
     * @param tree ComponentDependencyTreeDO
     * @param line 一行文本
     */
    private void setTreeInformation(ComponentDependencyTreeDO tree, String line){

        // "org.springframework:spring-core (n)"
        if (line.contains("(n)")){
            return;
        }

        // "org.springframework.boot:spring-boot-starter:3.2.3"
        // "org.springframework.boot:spring-boot-starter:3.2.3 (*)"
        String pattern1 = "^([^:]+):([^:]+):(\\S+).*$";
        Pattern r1 = Pattern.compile(pattern1);
        Matcher m1 = r1.matcher(line);
        if (m1.find()) {
            tree.setGroupId(m1.group(1));
            tree.setArtifactId(m1.group(2));
            tree.setVersion(m1.group(3));
            return;
        }

        // "org.springframework.boot:spring-boot-starter-jdbc -> 3.2.3"
        // "org.springframework.boot:spring-boot-starter-jdbc -> 3.2.3 (c)"
        String pattern2 = "^([^:]+):([^:]+)\\s+->\\s+(\\S+).*$";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(line);
        if (m2.find()) {
            tree.setGroupId(m2.group(1));
            tree.setArtifactId(m2.group(2));
            tree.setVersion(m2.group(3));
            return;
        }

        // "com.netease.cloudmusic.android:module_a:1.0.0 -> 1.1.0"
        // "com.netease.cloudmusic.android:module_c:1.2.0 -> 1.3.0 (c)"
        // "com.netease.cloudmusic.android:module_c:{strictly 1.0.0} -> 1.0.0"
        String pattern3 = "^([^:]+):([^:]+):(.*?)->\\s+(\\S+).*$";
        Pattern r3 = Pattern.compile(pattern3);
        Matcher m3 = r3.matcher(line);
        if (m3.find()) {
            tree.setGroupId(m3.group(1));
            tree.setArtifactId(m3.group(2));
            tree.setVersion(m3.group(4));
            return;
        }
    }

//    public static void main(String[] args) {
//        GradleServiceImpl gradleService = new GradleServiceImpl();
//        List<ComponentDependencyTreeDO> trees = gradleService.gradleDependencyTreeAnalyze("C:\\Users\\ASUS\\Desktop\\1.txt");
//        System.out.println(trees);
//    }
}
