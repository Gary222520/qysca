package nju.edu.cn.qysca.service.python;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.DependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.spider.PythonSpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class PythonServiceImpl implements PythonService {

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private PythonSpiderService pythonSpiderService;

//    public static void main(String[] args) {
//        PythonServiceImpl pythonService = new PythonServiceImpl();
//        System.out.println(pythonService.projectDependencyAnalysis("backend/src/main/resources/vyper"));
//    }

    /**
     * 解析项目依赖
     *
     * @param filePath 项目地址
     * @return jsonString json形式的项目依赖树
     */
    @Override
    public String projectDependencyAnalysis(String filePath) {

        File project = new File(filePath);

        String[] command1 = {"python", "-m", "venv", "venv"};
        //String[] command2 = {".\\venv\\Scripts\\activate.bat"};
        String[] command3 = {project.getPath()+".\\venv\\Scripts\\python.exe", "-m", "pip", "install", "pipdeptree"};
        String[] command4 = {project.getPath()+".\\venv\\Scripts\\python.exe", "-m", "pip", "install", "-r", project.getPath()+"\\requirements.txt"};
        String[] command5 = {project.getPath()+".\\venv\\Scripts\\python.exe", "-m", "pip", "install", "-r", project.getPath()+"\\requirements-docs.txt"};
        String[] command6 = {project.getAbsolutePath()+".\\venv\\Scripts\\python.exe", "setup.py", "develop"};
        String[] command7 = {project.getPath()+".\\venv\\Scripts\\python.exe", "-m", "pipdeptree", "--json-tree"};
        String[] command8 = {"rmdir", "/s", "/q", ".\\venv"};

        executeCommand(command1, project, false);
        executeCommand(command3, null, false);
        executeCommand(command4, null, false);
        executeCommand(command5, null, false);
        executeCommand(command6, project, false);
        String jsonString = executeCommand(command7, null, true);
        //executeCommand(command8, now, false);
        return jsonString;
    }

    /**
     * 执行命令行命令
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
            e.printStackTrace();
            throw new PlatformException(500, "");
        }
    }

    /**
     * 根据jsonString解析出依赖树
     *
     * @param jsonString json形式依赖树
     * @param groupId 组织id
     * @param artifactId 工件id
     * @param version 版本号
     * @return DependencyTreeDO 依赖树
     */
    @Override
    public DependencyTreeDO pythonDependencyTreeAnalyzer(String jsonString, String groupId, String artifactId, String version) {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode;
        try {
            jsonNode = objectMapper.readTree(jsonString);
        } catch (JsonProcessingException e) {
            throw new PlatformException(500, "");
        }

        ComponentDependencyTreeDO root = new ComponentDependencyTreeDO();
        root.setGroupId(groupId);
        root.setArtifactId(artifactId);
        root.setVersion(version);
        root.setDepth(0);
        root.setType("");

        List<ComponentDependencyTreeDO> dependencies = new ArrayList<>();
        for (JsonNode node : jsonNode) {
            // 这三个包是环境中生成的，并不是项目的依赖
            if (node.get("key").asText().equals("pipdeptree") || node.get("key").asText().equals("setuptools") || node.get("key").asText().equals("pip"))
                continue;
            dependencies.add(parseTree(node, 1));
        }
        root.setDependencies(dependencies);

        DependencyTreeDO dependencyTreeDO = new DependencyTreeDO();
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
    public ComponentDependencyTreeDO parseTree(JsonNode tree, int depth) {
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setGroupId("");
        componentDependencyTreeDO.setArtifactId(tree.get("key").asText());
        componentDependencyTreeDO.setVersion(tree.get("installed_version").asText());
        // 从知识库中查找
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(componentDependencyTreeDO.getGroupId(), componentDependencyTreeDO.getArtifactId(), componentDependencyTreeDO.getVersion());
        if (componentDO == null) {
            // 如果知识库中没有则爬取
            componentDO = pythonSpiderService.crawlByNV(componentDependencyTreeDO.getArtifactId(), componentDependencyTreeDO.getVersion());
            if (componentDO != null) {
                componentDao.save(componentDO);
                componentDependencyTreeDO.setType("opensource");
            } else {
                componentDependencyTreeDO.setType("opensource");
                //如果爬虫没有爬到则扫描错误 通过抛出异常处理
                throw new PlatformException(500, "存在未识别的组件");
            }
        } else {
            componentDependencyTreeDO.setType(componentDO.getType());
        }
        componentDependencyTreeDO.setDepth(depth);

        // 递归解析依赖
        List<ComponentDependencyTreeDO> dependencies = new ArrayList<>();
        for (JsonNode node : tree.get("dependencies")) {
            dependencies.add(parseTree(node, depth + 1));
        }
        componentDependencyTreeDO.setDependencies(dependencies);
        return componentDependencyTreeDO;
    }
}
