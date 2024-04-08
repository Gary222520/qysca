package nju.edu.cn.qysca.service.python;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.component.PythonComponentDao;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.PythonSpiderService;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class PythonServiceImpl implements PythonService {

    private final String FILE_SEPARATOR = "/";
    @Autowired
    private PythonComponentDao componentDao;
    @Autowired
    private PythonSpiderService pythonSpiderService;
    @Value("${tempPythonFolder}")
    String tempFolder;

    /**
     * 构造python组件
     * @param name 组件名称
     * @param version 版本号
     * @param type 组件类型
     * @return PythonComponentDO
     */
    @Override
    public PythonComponentDO componentAnalysis(String name, String version, String type) {
        PythonComponentDO pythonComponentDO = new PythonComponentDO();
        pythonComponentDO.setName(name);
        pythonComponentDO.setVersion(version);
        pythonComponentDO.setType(type);
        pythonComponentDO.setLanguage("python");
        pythonComponentDO.setDescription("-");
        pythonComponentDO.setUrl("-");
        pythonComponentDO.setDownloadUrl("-");
        pythonComponentDO.setSourceUrl("-");
        pythonComponentDO.setPUrl("-");
        pythonComponentDO.setAuthor("-");
        pythonComponentDO.setAuthorEmail("-");
        //creator和state由调用此方法者填充
        pythonComponentDO.setCreator(null);
        pythonComponentDO.setState(null);
        return pythonComponentDO;
    }

    /**
     * 分析上传项目依赖，获得组件依赖树
     *
     * @param filePath 上传文件路径
     * @param builder  构造器 ，例如 zip
     * @param name     组件名
     * @param version  组件版本
     * @param type     组件类型，例如 opensource
     * @return PythonDependencyTreeDO
     */
    @Override
    public PythonDependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String name, String version, String type) {
        // 支持zip、tar.gz、txt三种
        if (builder.equals("zip")) {
            unzip(filePath);
            filePath = filePath.substring(0, filePath.lastIndexOf("."));
        } else if (builder.equals("tar.gz")){
            unTarGz(filePath);
            filePath = filePath.substring(0, filePath.lastIndexOf("."));
            filePath = filePath.substring(0, filePath.lastIndexOf("."));
        } else if (builder.equals("txt")) {
            File txt = new File(filePath);
            filePath = txt.getParentFile().getPath();
        } else {
            throw new PlatformException(500, "python解析暂不支持此文件类型");
        }

        File project = new File(filePath);
        // 调用以下命令，获取json形式的依赖树
        // 1. 创建虚拟环境venv
        // 2. 安装pipdeptree
        // 3. 安装在requirements.txt中的包
        // 4. 执行setup.py，里面要求了一些依赖
        // 5. pipdeptree --json-tree获取json形式的依赖树
        String[] command1 = {"python", "-m", "venv", "venv"};
        executeCommand(command1, project, false);

        String[] command2 = {project.getPath() + "\\venv\\Scripts\\python.exe", "-m", "pip", "install", "pipdeptree"};
        executeCommand(command2, null, false);

        List<String> requirementTxtList = findRequirementFiles(project);
        if(builder.equals("zip") || builder.equals("tar.gz") || builder.equals("txt")) {
            for (String requirementsTxt : requirementTxtList) {
                String[] command3 = {project.getPath() + "\\venv\\Scripts\\python.exe", "-m", "pip", "install", "-r", requirementsTxt};
                executeCommand(command3, null, true);
            }
        }

        String[] command4 = {project.getAbsolutePath() + "\\venv\\Scripts\\python.exe", "setup.py", "develop"};
        if (builder.equals("zip") || builder.equals("tar.gz")) {
            executeCommand(command4, project, true);
        }

        String[] command5 = {project.getPath() + "\\venv\\Scripts\\python.exe", "-m", "pipdeptree", "--json-tree"};
        String jsonString = executeCommand(command5, null, true);

        // 删除生成的文件
        if (builder.equals("zip") || builder.equals("tar.gz")) {
            deleteFolder(filePath);
        } else {
            deleteFolder(filePath + "\\venv");
        }

        // 将json形式的依赖树转化为PythonDependencyTreeDO
        PythonDependencyTreeDO dependencyTreeDO = parseJsonDependencyTree(jsonString, name, version, type);
        return dependencyTreeDO;
    }

    /**
     * 根据依赖树生成平铺依赖表
     *
     * @param pythonDependencyTreeDO PythonDependencyTreeDO
     * @return List<PythonDependencyTableDO>
     */
    @Override
    public List<PythonDependencyTableDO> dependencyTableAnalysis(PythonDependencyTreeDO pythonDependencyTreeDO) {
        List<PythonDependencyTableDO> pythonDependencyTableDOList = new ArrayList<>();
        Queue<PythonComponentDependencyTreeDO> queue = new LinkedList<>(pythonDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            PythonDependencyTableDO pythonDependencyTableDO = new PythonDependencyTableDO();
            pythonDependencyTableDO.setName(pythonDependencyTreeDO.getName());
            pythonDependencyTableDO.setVersion(pythonDependencyTreeDO.getVersion());
            PythonComponentDependencyTreeDO componentDependencyTree = queue.poll();
            pythonDependencyTableDO.setCName(componentDependencyTree.getName());
            pythonDependencyTableDO.setCVersion(componentDependencyTree.getVersion());
            pythonDependencyTableDO.setDepth(componentDependencyTree.getDepth());
            pythonDependencyTableDO.setDirect(componentDependencyTree.getDepth() == 1);
            pythonDependencyTableDO.setType(componentDependencyTree.getType());
            pythonDependencyTableDO.setLanguage("python");
            pythonDependencyTableDO.setLicenses(componentDependencyTree.getLicenses());
            pythonDependencyTableDO.setVulnerabilities(componentDependencyTree.getVulnerabilities());
            queue.addAll(componentDependencyTree.getDependencies());
            pythonDependencyTableDOList.add(pythonDependencyTableDO);
        }
        return pythonDependencyTableDOList;
    }

    /**
     * 根据name和version爬取组件的依赖信息并生成依赖树
     *
     * @param name    组件名
     * @param version 版本号
     * @return PythonDependencyTreeDO
     */
    @Override
    public PythonDependencyTreeDO spiderDependency(String name, String version) {

        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);

        File tempFile = new File(tempFolder+timeStamp);
        if (!tempFile.exists()){ //如果不存在
            boolean dr = tempFile.mkdirs(); //创建目录
        }

        // 调用以下命令，获取json形式的依赖树
        // 1. 创建虚拟环境venv
        // 2. 安装pipdeptree
        // 3. 安装要查询的包
        // 5. pipdeptree --json-tree获取json形式的依赖树
        String[] command1 = {"python", "-m", "venv", "venv"};
        String[] command3 = {tempFile.getAbsolutePath() +"\\venv\\Scripts\\python.exe", "-m", "pip", "install", "pipdeptree"};
        String[] command4;
        if (version.equals("-"))
            command4 = new String[]{tempFile.getAbsolutePath() + "\\venv\\Scripts\\python.exe", "-m", "pip", "install", name};
        else
            command4 = new String[]{tempFile.getAbsolutePath() + "\\venv\\Scripts\\python.exe", "-m", "pip", "install", name + "==" + version};
        String[] command5 = {tempFile.getAbsolutePath() +"\\venv\\Scripts\\python.exe", "-m", "pipdeptree", "--json-tree"};

        executeCommand(command1, tempFile, false);
        executeCommand(command3, null, true);
        executeCommand(command4, null, true);
        String jsonString = executeCommand(command5, null, true);
        deleteFolder(tempFile.getPath());

        PythonDependencyTreeDO dependencyTreeDO = parseJsonDependencyTree(jsonString, name, version, "opensource");
        return dependencyTreeDO;
    }


    /**
     * 将python依赖信息转换成应用依赖信息
     * @param pythonComponentDependencyTreeDO  python依赖信息
     * @return AppComponentDependencyTreeDO 应用依赖信息
     */
    @Override
    public AppComponentDependencyTreeDO translateComponentDependency(PythonComponentDependencyTreeDO pythonComponentDependencyTreeDO) {
        if(pythonComponentDependencyTreeDO == null){
            return null;
        }
        AppComponentDependencyTreeDO appComponentDependencyTreeDO = new AppComponentDependencyTreeDO();
        appComponentDependencyTreeDO.setName(pythonComponentDependencyTreeDO.getName());
        appComponentDependencyTreeDO.setVersion(pythonComponentDependencyTreeDO.getVersion());
        appComponentDependencyTreeDO.setDepth(pythonComponentDependencyTreeDO.getDepth());
        appComponentDependencyTreeDO.setType(pythonComponentDependencyTreeDO.getType());
        appComponentDependencyTreeDO.setLicenses(pythonComponentDependencyTreeDO.getLicenses());
        appComponentDependencyTreeDO.setVulnerabilities(pythonComponentDependencyTreeDO.getVulnerabilities());
        appComponentDependencyTreeDO.setLanguage("python");
        List<AppComponentDependencyTreeDO> children = new ArrayList<>();
        for(PythonComponentDependencyTreeDO childPythonComponentDependencyTreeDO : pythonComponentDependencyTreeDO.getDependencies()) {
            AppComponentDependencyTreeDO childAppComponentDependencyTreeDO = translateComponentDependency(childPythonComponentDependencyTreeDO);
            children.add(childAppComponentDependencyTreeDO);
        }
        appComponentDependencyTreeDO.setDependencies(children);
        return appComponentDependencyTreeDO;
    }

    /**
     * 将Python依赖表转换为App依赖表
     * @param pythonDependencyTableDOS python依赖表信息
     * @return List<AppDependencyTableDO> App依赖表
     */
    @Override
    public List<AppDependencyTableDO> translateDependencyTable(List<PythonDependencyTableDO> pythonDependencyTableDOS) {
        List<AppDependencyTableDO> appDependencyTableDOS = new ArrayList<>();
        for(PythonDependencyTableDO pythonDependencyTableDO : pythonDependencyTableDOS){
            AppDependencyTableDO appDependencyTableDO = new AppDependencyTableDO();
            BeanUtils.copyProperties(pythonDependencyTableDO, appDependencyTableDO);
            appDependencyTableDOS.add(appDependencyTableDO);
        }
        return appDependencyTableDOS;
    }

    /**
     * 执行命令行命令
     *
     * @param command    待执行命令
     * @param workDir    命令工作目录
     * @param needResult 是否打印命令输出
     * @return result 命令运行输出
     */
    private String executeCommand(String[] command, File workDir, Boolean needResult) {
        try {
            // 执行命令
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            // 合并input和error流
            processBuilder.redirectErrorStream(true);
            processBuilder.directory(workDir);
            Process process = processBuilder.start();

            // 捕获命令输出
            String result = null;
            if (needResult) {
                StringBuilder outputStringBuilder = new StringBuilder();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    outputStringBuilder.append(line);
                }
                result = outputStringBuilder.toString();
            }
            // 等待命令执行完毕
            process.waitFor();
            return result;
        } catch (IOException | InterruptedException e) {
            throw new PlatformException(500, "执行命令时错误，命令为:" + String.join(" ", command));
        }
    }

    /**
     * 根据jsonString解析出依赖树
     *
     * @param jsonString json形式依赖树
     * @param name       包名
     * @param version    版本号
     * @return DependencyTreeDO 依赖树
     */
    private PythonDependencyTreeDO parseJsonDependencyTree(String jsonString, String name, String version, String type) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new PlatformException(500, "json依赖树解析错误");
        }

        PythonComponentDependencyTreeDO root = new PythonComponentDependencyTreeDO();
        root.setName(name);
        root.setVersion(version);
        root.setDepth(0);
        root.setType(type);

        List<PythonComponentDependencyTreeDO> dependencies = new ArrayList<>();
        for (JsonNode node : jsonNode) {
            // 这三个包是环境中生成的，并不是项目的依赖
            if (node.get("key").asText().equals("pipdeptree") || node.get("key").asText().equals("setuptools") || node.get("key").asText().equals("pip"))
                continue;
            // 有时候树中会重复自己，跳过
            if (node.get("key").asText().equals(name) && node.get("installed_version").asText().equals(version)){
                for (JsonNode subNode : node.get("dependencies")) {
                    dependencies.add(parseTree(subNode, 1));
                }
            } else
                dependencies.add(parseTree(node, 1));
        }
        root.setDependencies(dependencies);
        // 去除重复组件
        removeDuplicates(root);

        PythonDependencyTreeDO dependencyTreeDO = new PythonDependencyTreeDO();
        dependencyTreeDO.setName(name);
        dependencyTreeDO.setVersion(version);
        dependencyTreeDO.setTree(root);
        return dependencyTreeDO;
    }

    /**
     * 递归解析jsonNode获取依赖
     *
     * @param tree  JsonNode
     * @param depth 深度
     * @return ComponentDependencyTreeDO
     */
    private PythonComponentDependencyTreeDO parseTree(JsonNode tree, int depth) {
        PythonComponentDependencyTreeDO componentDependencyTreeDO = new PythonComponentDependencyTreeDO();
        componentDependencyTreeDO.setName(tree.get("key").asText());
        componentDependencyTreeDO.setVersion(tree.get("installed_version").asText());
        // 从知识库中查找
        PythonComponentDO componentDO = componentDao.findByNameAndVersion(componentDependencyTreeDO.getName(), componentDependencyTreeDO.getVersion());
        if (componentDO == null) {
            // 如果知识库中没有则爬取
            componentDO = pythonSpiderService.crawlByNV(componentDependencyTreeDO.getName(), componentDependencyTreeDO.getVersion());
            if (componentDO != null) {
                componentDao.save(componentDO);
                componentDependencyTreeDO.setLicenses(String.join(",", componentDO.getLicenses()));
                componentDependencyTreeDO.setVulnerabilities(String.join(",", componentDO.getVulnerabilities()));
                componentDependencyTreeDO.setType("opensource");
            } else {
                componentDependencyTreeDO.setType("opensource");
                // 如果爬虫没有爬到则打印报错信息，仍继续执行
                System.err.println("存在未识别组件：" + componentDependencyTreeDO.getName() + ":" + componentDependencyTreeDO.getVersion());
//                //如果爬虫没有爬到则扫描错误 通过抛出异常处理
//                throw new PlatformException(500, "存在未识别的组件");
            }
        } else {
            componentDependencyTreeDO.setLicenses(String.join(",", componentDO.getLicenses()));
            componentDependencyTreeDO.setVulnerabilities(String.join(",", componentDO.getVulnerabilities()));
            componentDependencyTreeDO.setType(componentDO.getType());
        }
        componentDependencyTreeDO.setDepth(depth);

        // 递归解析依赖
        List<PythonComponentDependencyTreeDO> dependencies = new ArrayList<>();
        for (JsonNode node : tree.get("dependencies")) {
            dependencies.add(parseTree(node, depth + 1));
        }
        componentDependencyTreeDO.setDependencies(dependencies);
        return componentDependencyTreeDO;
    }

    /**
     * 去除依赖树中重复的组件
     * @param root 根节点
     */
    private void removeDuplicates(PythonComponentDependencyTreeDO root) {
        Set<String> visited = new HashSet<>();  // 用于存储已经访问过的组件
        Queue<PythonComponentDependencyTreeDO> queue = new ArrayDeque<>();
        // 根节点入队
        visited.add(root.getName() + ":" + root.getVersion());
        queue.offer(root);
        // 使用队列进行BFS
        while (!queue.isEmpty()) {
            // 出队
            PythonComponentDependencyTreeDO node = queue.poll();
            List<PythonComponentDependencyTreeDO> dependencies = node.getDependencies();
            if (dependencies != null) {
                List<PythonComponentDependencyTreeDO> modifiedDependencies = new ArrayList<>();
                // 遍历组件的依赖
                for (PythonComponentDependencyTreeDO dependency : dependencies) {
                    // 跳过已访问的依赖
                    if (visited.contains(dependency.getName() + ":" + dependency.getVersion()))
                        continue;
                    visited.add(dependency.getName() + ":" + dependency.getVersion());
                    // 没访问过的入队，并添加进更正后的依赖
                    queue.offer(dependency);
                    modifiedDependencies.add(dependency);
                }
                // 更新当前组件的依赖
                node.setDependencies(modifiedDependencies);
            }
        }
    }

    /**
     * 获取目录下的所有requirements文件
     * @param dir 目录
     */
    private List<String> findRequirementFiles(File dir) {
        List<String> requirements = new ArrayList<>();
        // 定义正则表达式匹配包含requirements的txt文件
        Pattern pattern = Pattern.compile(".*requirements.*\\.txt$");
        // 获取目录下的所有文件和子目录
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    // 如果是目录，递归调用方法获取目录下的 requirements 文件路径
                    requirements.addAll(findRequirementFiles(file));
                } else {
                    // 如果是文件名匹配 requirements.txt 的正则表达式，则添加文件路径到结果列表
                    Matcher matcher = pattern.matcher(file.getName());
                    if (matcher.matches()) {
                        requirements.add(file.getPath());
                    }
                }
            }
        }
        return requirements;
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

    /**
     * 解压tar.gz文件
     * @param filePath tar.gz文件路径
     */
    private void unTarGz(String filePath) {
        // 获取输出目录路径
        String outputDirPath = new File(filePath).getParentFile().getPath();
        // 创建输出目录
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try (FileInputStream fis = new FileInputStream(filePath);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             TarArchiveInputStream taris = new TarArchiveInputStream(gzis)) {
            // 循环遍历 Tar 文件中的条目，并解压到指定目录
            TarArchiveEntry entry;
            while ((entry = taris.getNextTarEntry()) != null) {
                // 获取条目文件名
                String entryName = entry.getName();
                // 构建实际解压后文件的路径
                File entryFile = new File(outputDir, entryName);
                if (entry.isDirectory()) {
                    // 如果是目录，创建目录
                    entryFile.mkdirs();
                } else {
                    // 如果是文件，创建父目录并解压文件
                    File parent = entryFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    // 创建文件输出流
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        // 读取 Tar 输入流并写入文件输出流
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = taris.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new PlatformException(500, "解压tar.gz文件失败");
        }
    }

    /**
     * 删除文件夹
     *
     * @param filePath 文件夹
     */
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
