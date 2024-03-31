package nju.edu.cn.qysca.service.gradle;

import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.SpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class GradleServiceImpl implements GradleService{

    @Autowired
    private SpiderService spiderService;

    @Autowired
    private JavaComponentDao javaComponentDao;

    private final String FILE_SEPARATOR = "/";

    /**
     * 构造gradle java组件
     * @param name 组件名
     * @param version 版本号
     * @param type 组件类型
     * @return PythonComponentDO
     */
    @Override
    public JavaComponentDO componentAnalysis(String name, String version, String type) {
        JavaComponentDO javaComponentDO = new JavaComponentDO();
        javaComponentDO.setName(name);
        javaComponentDO.setVersion(version);
        javaComponentDO.setType(type);
        javaComponentDO.setLanguage("java");
        javaComponentDO.setJName(name.split(":")[1]);
        javaComponentDO.setDescription("-");
        javaComponentDO.setUrl("-");
        javaComponentDO.setDownloadUrl("-");
        javaComponentDO.setSourceUrl("-");
        javaComponentDO.setPUrl("-");
        javaComponentDO.setDevelopers(new ArrayList<>());
        javaComponentDO.setHashes(new ArrayList<>());
        //creator和state由调用此方法者填充
        javaComponentDO.setCreator(null);
        javaComponentDO.setState(null);
        return javaComponentDO;
    }


    /**
     *  解析上传文件的依赖信息，并生成依赖树
     * @param filePath 上传文件路径
     * @param builder 构造器， 例如 zip
     * @param name 组件名
     * @param version 版本号
     * @param type 类型，例如 opensource
     * @return JavaDependencyTreeDO
     */
    @Override
    public JavaDependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String name, String version, String type) {
        if (builder.equals("zip")){
            unzip(filePath);
            filePath = filePath.substring(0,filePath.lastIndexOf("."));
        } else {
            throw new PlatformException(500, "gradle解析暂不支持此文件类型");
        }

        // 调用./gradlew dependencies命令，并获取结果
        List<String> lines = runGradleCommand(filePath);
        deleteFolder(filePath);

        // 解析命令结果，并设置root的JavaComponentDependencyTreeDO
        List<JavaComponentDependencyTreeDO> trees = gradleDependencyTreeAnalyze(lines);

        JavaComponentDependencyTreeDO root = new JavaComponentDependencyTreeDO();
        root.setName(name);
        root.setVersion(version);
        root.setType(type);
        root.setDependencies(trees);
        root.setDepth(0);

        // 封装为JavaDependencyTreeDO
        JavaDependencyTreeDO dependencyTreeDO = new JavaDependencyTreeDO();
        dependencyTreeDO.setName(name);
        dependencyTreeDO.setVersion(version);
        dependencyTreeDO.setTree(root);
        return dependencyTreeDO;
    }

    /**
     * 调用./gradlew dependencies命令，并返回命令结果
     * @param filePath 文件路径，用以设置工作目录
     * @return List<String> 命令结果
     */
    private static List<String> runGradleCommand(String filePath) {
        List<String> lines = new ArrayList<>();
        try{
            File file = new File(filePath);
            // 创建命令 ./gradlew dependencies
            // windows系统执行gradlew.bat，linux还是gradlew
            List<String> command = List.of(file.getAbsolutePath()+"\\gradlew.bat", "dependencies");
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
            process.waitFor();
        } catch (IOException | InterruptedException e){
            throw new PlatformException(500, "gradle项目解析失败");
        }
        return lines;
    }

    /**
     * 解析gradle依赖树文件
     * @param lines 带解析内容 整个文件 List<String>
     * @return List<ComponentDependencyTreeDO>
     */
    public List<JavaComponentDependencyTreeDO> gradleDependencyTreeAnalyze(List<String> lines){

        // 用以记录直接依赖
        Set<String> visited = new HashSet<>();
        List<JavaComponentDependencyTreeDO> trees = new ArrayList<>();
        int begin = 0;
        // 扫描文件，找出依赖树形式的文本块进行解析
        while(begin < lines.size()){
            // 通过begin和end两个指针来确定依赖树文本块的位置
            while (begin < lines.size() && !(lines.get(begin).startsWith("+---") || lines.get(begin).startsWith("\\---"))){
                begin++;
            }
            if (begin >= lines.size())
                break;
            //String scope = getScope(lines.get(begin-1));
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
     */
    private List<JavaComponentDependencyTreeDO> parseTree(Set<String> visited, List<String> lines, int depth){
        List<JavaComponentDependencyTreeDO> trees = new ArrayList<>();
        int begin = 0;
        while (begin < lines.size()){
            // 通过begin和end两个指针，从依赖树文本块中分割出单个组件依赖树
            int end = begin + 1;
            while (end < lines.size() && !(lines.get(end).startsWith("+---") || lines.get(end).startsWith("\\---"))){
                end++;
            }
            // 提取组件信息
            JavaComponentDependencyTreeDO componentDependencyTreeDO = new JavaComponentDependencyTreeDO();
            componentDependencyTreeDO.setDepth(depth);
            extractGAVFromLine(componentDependencyTreeDO, lines.get(begin).substring(5));
            String name = componentDependencyTreeDO.getName();
            String version = componentDependencyTreeDO.getVersion();

            // 如果该直接依赖已记录过，或者该组件的gav缺失，则跳过
            if (!(depth == 1 && visited.contains(name + ":" + version)) && !(name == null || version == null) && !(name.isEmpty() || version.isEmpty()))
            {
                if (depth==1)
                    visited.add(name + ":" + version);

                // 查知识库
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(name, version);
                // 如果知识库没有则爬取
                if (javaComponentDO == null){
                    String groupId = name.split(":")[0];
                    String artifactId = name.split(":")[1];
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
     * 使用正则表达式从每行信息中提取出组件的gav，并存入tree中
     *
     * @param tree ComponentDependencyTreeDO
     * @param line 一行文本
     */
    private void extractGAVFromLine(JavaComponentDependencyTreeDO tree, String line){

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
//            tree.setGroupId(m1.group(1));
//            tree.setArtifactId(m1.group(2));
            tree.setName(m1.group(1) + ":" + m1.group(2));
            tree.setVersion(m1.group(3));
            return;
        }

        // "org.springframework.boot:spring-boot-starter-jdbc -> 3.2.3"
        // "org.springframework.boot:spring-boot-starter-jdbc -> 3.2.3 (c)"
        String pattern2 = "^([^:]+):([^:]+)\\s+->\\s+(\\S+).*$";
        Pattern r2 = Pattern.compile(pattern2);
        Matcher m2 = r2.matcher(line);
        if (m2.find()) {
//            tree.setGroupId(m2.group(1));
//            tree.setArtifactId(m2.group(2));
            tree.setName(m2.group(1) + ":" + m2.group(2));
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
//            tree.setGroupId(m3.group(1));
//            tree.setArtifactId(m3.group(2));
            tree.setName(m3.group(1) + ":" + m3.group(2));
            tree.setVersion(m3.group(4));
            return;
        }
    }

    /**
     * 获取scope
     * 问题：gradle的解析文件中，对于同一个依赖，可能在不同的scope中都出现了，因此一个依赖不止一个scope，无法对应上。
     * @param line "developmentOnly - Configuration for development-only dependencies such as Spring Boot's DevTools."
     * @return scope "developmentOnly"
     */
    private String getScope(String line){
        return line.split(" ")[0];
    }


    /**
     * 解压zip文件
     *
     * @param filePath zip文件路径
     */
    private void unzip(String filePath) {
        try {
            File file = new File(filePath);
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            ZipFile zipFile = new ZipFile(filePath, Charset.forName("GBK"));
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                String fileDestPath = dir + FILE_SEPARATOR + entryName;
                if (!zipEntry.isDirectory()) {
                    File destFile = new File(fileDestPath);
                    destFile.getParentFile().mkdirs();
                    InputStream inputStream = zipFile.getInputStream(zipEntry);
                    FileOutputStream outputStream = new FileOutputStream(destFile);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = inputStream.read(buffer)) > 0) {
                        outputStream.write(buffer, 0, bytesRead);
                    }
                    outputStream.close();
                    inputStream.close();
                } else {
                    File dirToCreate = new File(fileDestPath);
                    dirToCreate.mkdirs();
                }
            }
            zipFile.close();
        } catch (Exception e) {
            throw new PlatformException(500, "解压zip文件失败");
        }
    }

    private void deleteFolder(String filePath) {
        File folder = new File(filePath);
        if (folder.exists()) {
            deleteFolderFile(folder);
        }
    }

    /**
     * 递归删除文件夹下的文件
     *
     * @param folder 文件夹
     */
    private void deleteFolderFile(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolderFile(file);
            }
            file.delete();
        }
        folder.delete();
    }

}
