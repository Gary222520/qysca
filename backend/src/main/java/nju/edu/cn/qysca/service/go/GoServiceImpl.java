package nju.edu.cn.qysca.service.go;

import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.dao.component.GoComponentDao;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.GoDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.GoDependencyTreeDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.spider.GoSpiderService;
import nju.edu.cn.qysca.service.vulnerability.VulnerabilityService;
import nju.edu.cn.qysca.utils.FolderUtil;
import nju.edu.cn.qysca.utils.ZipUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
public class GoServiceImpl implements GoService {

    private final String FILE_SEPARATOR = "/";
    @Autowired
    private GoSpiderService goSpiderService;
    @Autowired
    private GoComponentDao goComponentDao;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private VulnerabilityService vulnerabilityService;
    @Value("${tempGoFolder}")
    private String tempFolder;

    /**
     * 根据给定的名称、版本和类型构造GO组件
     *
     * @param name    组件名称
     * @param version 版本号
     * @param type    组件类型
     * @return Go组件
     */
    @Override
    public GoComponentDO componentAnalysis(String name, String version, String type) {
        GoComponentDO goComponentDO = new GoComponentDO();
        goComponentDO.setName(name);
        goComponentDO.setVersion(version);
        goComponentDO.setType(type);
        goComponentDO.setLanguage("golang");
        goComponentDO.setUrl("-");
        goComponentDO.setPUrl("-");
        goComponentDO.setSourceUrl("-");
        goComponentDO.setDownloadUrl("-");
        goComponentDO.setDescription("-");
        // creator和state由调用此方法者填充
        goComponentDO.setCreator(null);
        goComponentDO.setState(null);
        return goComponentDO;
    }

    /**
     * 根据给定的信息解析Go依赖树
     *
     * @param name     根组件名称
     * @param version  根组件版本号
     * @param type     根组件类型
     * @param filePath 扫描文件路径
     * @param builder  扫描方式
     * @return Go依赖树
     */
    @Override
    public GoDependencyTreeDO dependencyTreeAnalysis(String name, String version, String type, String filePath, String builder) {
        // 填充依赖树根结点信息
        GoDependencyTreeDO goDependencyTreeDO = new GoDependencyTreeDO();
        goDependencyTreeDO.setName(name);
        goDependencyTreeDO.setVersion(version);
        // 解析上传的文件（go.mod或zip），得到go.mod文件所在文件夹的路径
        String modPath = "";
        try {
            if (builder.equals("zip")) {
                ZipUtil.unzip(filePath);
                modPath = filePath.substring(0, filePath.length() - 4);
            } else if (builder.equals("go.mod")) {
                modPath = filePath.substring(0, filePath.length() - 6);
            } else {
                throw new RuntimeException();
            }
        } catch (Exception e) {
            throw new PlatformException(500, "文件解析失败");
        }
        // 执行命令go mod graph，获取命令行输出信息
        List<String> cmdLines = executeCommand("go mod graph", modPath);
        // 解析命令行输出信息获得依赖图，再由依赖图BFS生成依赖树
        GoComponentDependencyTreeDO goComponentDependencyTreeDO = parseTree(cmdLines);
        // 调整树根结点信息，完成解析并返回
        goComponentDependencyTreeDO.setName(name);
        goComponentDependencyTreeDO.setVersion(version);
        goComponentDependencyTreeDO.setType(type);
        goComponentDependencyTreeDO.setDepth(0);
        goDependencyTreeDO.setTree(goComponentDependencyTreeDO);
        return goDependencyTreeDO;
    }

    /**
     * 根据给定的Go依赖树解析Go依赖平铺信息表
     *
     * @param tree Go依赖树
     * @return Go依赖平铺信息表
     */
    @Override
    public List<GoDependencyTableDO> dependencyTableAnalysis(GoDependencyTreeDO tree) {
        List<GoDependencyTableDO> tableList = new ArrayList<>();
        Queue<GoComponentDependencyTreeDO> queue = new LinkedList<>(tree.getTree().getDependencies());
        while (!queue.isEmpty()) {
            GoComponentDependencyTreeDO node = queue.poll();
            GoDependencyTableDO tableDO = new GoDependencyTableDO();
            tableDO.setName(tree.getName());
            tableDO.setVersion(tree.getVersion());
            tableDO.setCName(node.getName());
            tableDO.setCVersion(node.getVersion());
            tableDO.setLanguage("golang");
            tableDO.setDepth(node.getDepth());
            tableDO.setDirect(node.getDepth() == 1);
            tableDO.setType(node.getType());
            tableDO.setLicenses(node.getLicenses());
            tableDO.setVulnerabilities(node.getVulnerabilities());
            tableList.add(tableDO);
            queue.addAll(node.getDependencies());
        }
        return tableList;
    }

    /**
     * 根据给定的名称和版本爬取并解析Go组件依赖树
     *
     * @param name    名称
     * @param version 版本
     * @return Go组件依赖树
     */
    @Override
    public GoDependencyTreeDO spiderDependency(String name, String version) {
        // 先检查组件库中是否有对应信息，若无，爬取对应信息
        GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(name, version);
        if (null == goComponentDO) {
            goComponentDO = goSpiderService.crawlByNV(name, version);
            if (null == goComponentDO) {
                throw new PlatformException(500, "无法识别的组件：" + name + ":" + version);
            }
            goComponentDao.save(goComponentDO);
        }

        // 设置爬取存储的临时文件夹
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);
        File tempDir = new File(tempFolder, timeStamp);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        try {
            // 爬取对应组件的zip文件，下载到指定路径
            goSpiderService.spiderContent(goComponentDO.getDownloadUrl(), tempDir.getPath());
            String tmpZipPath = tempDir.getPath() + FILE_SEPARATOR + "gotmp.zip";
            // 解压zip文件
            ZipUtil.unzip(tmpZipPath);
            // 定位go.mod路径
            String realPath = tmpZipPath.substring(0, tmpZipPath.lastIndexOf('/')) + FILE_SEPARATOR + name + "@" + version + FILE_SEPARATOR + "go.mod";
            // 调用依赖树分析方法
            return dependencyTreeAnalysis(name, version, "opensource", realPath, "go.mod");
        } catch (Exception e) {
            e.printStackTrace();
            throw new PlatformException(500, "识别依赖信息失败");
        } finally {
            FolderUtil.deleteFolder(tempDir.getPath());
        }
    }


    /**
     * 将Go组件依赖关系转换成应用组件依赖关系
     *
     * @param goComponentDependencyTreeDO go组件依赖树关系
     * @return AppComponentDependencyTreeDO 应用组件依赖树
     */
    @Override
    public AppComponentDependencyTreeDO translateComponentDependency(GoComponentDependencyTreeDO goComponentDependencyTreeDO) {
        if (goComponentDependencyTreeDO == null) {
            return null;
        }
        AppComponentDependencyTreeDO appComponentDependencyTreeDO = new AppComponentDependencyTreeDO();
        appComponentDependencyTreeDO.setName(goComponentDependencyTreeDO.getName());
        appComponentDependencyTreeDO.setVersion(goComponentDependencyTreeDO.getVersion());
        appComponentDependencyTreeDO.setDepth(goComponentDependencyTreeDO.getDepth());
        appComponentDependencyTreeDO.setType(goComponentDependencyTreeDO.getType());
        appComponentDependencyTreeDO.setLicenses(goComponentDependencyTreeDO.getLicenses());
        appComponentDependencyTreeDO.setVulnerabilities(goComponentDependencyTreeDO.getVulnerabilities());
        appComponentDependencyTreeDO.setLanguage("golang");
        List<AppComponentDependencyTreeDO> children = new ArrayList<>();
        for (GoComponentDependencyTreeDO childGoComponentDependencyTreeDO : goComponentDependencyTreeDO.getDependencies()) {
            AppComponentDependencyTreeDO childAppComponentDependencyTreeDO = translateComponentDependency(childGoComponentDependencyTreeDO);
            children.add(childAppComponentDependencyTreeDO);
        }
        appComponentDependencyTreeDO.setDependencies(children);
        return appComponentDependencyTreeDO;
    }

    /**
     * 将go依赖信息表转化成应用依赖信息表
     *
     * @param goDependencyTableDOS go依赖信息表
     * @return List<AppDependencyTableDO> 应用依赖关系表
     */
    @Override
    public List<AppDependencyTableDO> translateDependencyTable(List<GoDependencyTableDO> goDependencyTableDOS) {
        List<AppDependencyTableDO> appDependencyTableDOS = new ArrayList<>();
        for (GoDependencyTableDO goDependencyTableDO : goDependencyTableDOS) {
            AppDependencyTableDO appDependencyTableDO = new AppDependencyTableDO();
            BeanUtils.copyProperties(goDependencyTableDO, appDependencyTableDO);
            appDependencyTableDOS.add(appDependencyTableDO);
        }
        return appDependencyTableDOS;
    }

    /**
     * 解析go mod graph输出，构造图，BFS生成依赖树
     *
     * @param lines 命令行输出信息
     * @return Go组件依赖树
     */
    private GoComponentDependencyTreeDO parseTree(List<String> lines) {
        if (null == lines || lines.isEmpty()) {
            throw new PlatformException(500, "解析执行失败");
        }
        // 读取命令行输出信息，构造图（邻接表形式）
        Map<String, List<String>> graph = new HashMap<>();
        Map<String, Boolean> visited = new HashMap<>();
        String root = lines.get(0).split(" ")[0];
        for (String line : lines) {
            String[] edge = line.split(" ");
            String start = edge[0];
            String end = edge[1];
            // 去掉go语言自身的依赖
            if (start.startsWith("go@") || end.startsWith("go@")) {
                continue;
            }
            if (!graph.containsKey(start)) {
                graph.put(start, new ArrayList<>());
                visited.put(start, false);
            }
            if (!graph.containsKey(end)) {
                graph.put(end, new ArrayList<>());
                visited.put(end, false);
            }
            graph.get(start).add(end);
        }
        // BFS生成依赖树
        GoComponentDependencyTreeDO tree = new GoComponentDependencyTreeDO();
        Queue<GoComponentDependencyTreeDO> queue = new LinkedList<>();
        // 设置根结点
        String[] rootNv = root.split("@");
        if (rootNv.length == 1) {
            tree.setName(rootNv[0]);
            tree.setVersion("");
        } else {
            tree.setName(rootNv[0]);
            tree.setVersion(rootNv[1]);
        }
        tree.setDepth(0);
        tree.setDependencies(new ArrayList<>());
        if (visited.size() == 0 || graph.size() == 0) {
            return tree;
        }
        queue.add(tree);
        int depth = 0;
        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                GoComponentDependencyTreeDO node = queue.poll();
                String key = Objects.equals(node.getVersion(), "") ? node.getName() : node.getName() + "@" + node.getVersion();
                if (visited.get(key)) {
                    continue;
                }
                for (String neighbor : graph.get(key)) {
                    if (visited.get(neighbor)) {
                        continue;
                    }
                    GoComponentDependencyTreeDO child = new GoComponentDependencyTreeDO();
                    String[] nv = neighbor.split("@");
                    if (nv.length == 1) {
                        child.setName(nv[0]);
                        child.setVersion("");
                    } else {
                        child.setName(nv[0]);
                        child.setVersion(nv[1]);
                    }
                    child.setDepth(depth + 1);
                    child.setDependencies(new ArrayList<>());
                    // 检查知识库中是否已有子组件信息，若无，则爬取写入知识库中
                    GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(child.getName(), child.getVersion());
                    if (null == goComponentDO) {
                        goComponentDO = goSpiderService.crawlByNV(child.getName(), child.getVersion());
                        if (goComponentDO != null){
                            try {
                                goComponentDao.save(goComponentDO);
                            } catch (Exception e){
                                // save组件时出现错误，跳过该组件，仍继续执行
                                log.error("组件存入数据库失败：" + goComponentDO.toString());
                                e.printStackTrace();
                            }
                        } else {
                            // 如果爬虫没有爬到则打印报错信息，仍继续执行
                            log.error("存在未识别的组件：" + child.getName() + ":" + child.getVersion());
                            child.setType("opensource");
                            child.setLicenses("-");
                            child.setVulnerabilities("-");
                        }
                    }
                    child.setLicenses(String.join(",", goComponentDO.getLicenses()));
                    child.setVulnerabilities(String.join(",", goComponentDO.getVulnerabilities()));
                    child.setType(goComponentDO.getType());
                    node.getDependencies().add(child);
                    queue.add(child);
                }
                visited.put(key, true);
            }
            depth++;
        }
        return tree;
    }

    /**
     * 调用cmd执行命令
     *
     * @param command  命令语句
     * @param filePath 命令执行的文件路径
     * @return 命令行输出结果
     */
    private List<String> executeCommand(String command, String filePath) {
        List<String> commandList = List.of(command.split(" "));
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(filePath);
            // 创建命令
            ProcessBuilder processBuilder = new ProcessBuilder(commandList);
            processBuilder.directory(file);
            // 启动命令
            Process process = processBuilder.start();
            // 直接将命令执行结果保存在lines中，没有生成中间文件
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = errReader.readLine()) != null){
                    log.error(line);
                }
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
                // 等待命令执行完毕
                int exitCode = process.waitFor();
            }
        } catch (IOException | InterruptedException e) {
            throw new PlatformException(500, "解析执行失败");
        }
        return lines;
    }
}
