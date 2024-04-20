package nju.edu.cn.qysca.service.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.dao.component.JsComponentDao;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.npm.PackageJsonDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDependencyDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.spider.JsSpiderService;
import nju.edu.cn.qysca.service.vulnerability.VulnerabilityService;
import nju.edu.cn.qysca.utils.FolderUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
@Slf4j
public class NpmServiceImpl implements NpmService {

    private final String FILE_SEPARATOR = "/";
    @Autowired
    private JsSpiderService jsSpiderService;
    @Autowired
    private JsComponentDao jsComponentDao;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private VulnerabilityService vulnerabilityService;
    @Value("${tempNpmFolder}")
    private String tempFolder;


    /**
     * 根据package.json生成ComponentDO
     *
     * @param filePath zip文件路径
     * @param builder  构建器
     * @param type     组件类型
     * @return JsComponentDO Js组件信息
     */
    @Override
    public JsComponentDO componentAnalysis(String filePath, String builder, String type) {
        try {
            if (builder.equals("zip")) {
                extractFile(filePath);
            }
            File file = new File(filePath);
            PackageJsonDTO packageJsonDTO = parsePackageJson(file.getParent() + FILE_SEPARATOR + "package.json");
            JsComponentDO jsComponentDO = new JsComponentDO();
            String name = packageJsonDTO.getName();
            jsComponentDO.setName(name);
            jsComponentDO.setLanguage("javaScript");
            jsComponentDO.setVersion(packageJsonDTO.getVersion());
            jsComponentDO.setDescription(packageJsonDTO.getDescription());
            List<String> licenses = new ArrayList<>();
            if (packageJsonDTO.getLicense() != null) {
                licenses.addAll(licenseService.searchLicense(packageJsonDTO.getLicense()));
            }
            jsComponentDO.setLicenses(licenses.toArray(new String[0]));
            jsComponentDO.setType(type);
            return jsComponentDO;
        } catch (Exception e) {
            throw new PlatformException(500, "解析package.json失败");
        }
    }

    /**
     * @param filePath package-lock.json文件路径
     * @param type     组件类型
     * @return JsDependencyTreeDO 依赖树信息
     */
    @Override
    public JsDependencyTreeDO dependencyTreeAnalysis(String filePath, String builder, String type) {
        try {
            File file = new File(filePath);
            File packageLock = new File(file.getParent() + FILE_SEPARATOR + "package-lock.json");
            if (!packageLock.exists()) {
                if (builder.equals("zip")) {
                    extractFile(filePath);
                    generatePackageLock(file.getParent());
                } else if (builder.equals("package.json")) {
                    generatePackageLock(file.getParent());
                }
            }
            PackageLockDTO packageLockDTO = parsePackageLock(file.getParent() + FILE_SEPARATOR + "package-lock.json");
            JsDependencyTreeDO jsDependencyTreeDO = new JsDependencyTreeDO();
            jsDependencyTreeDO.setName(packageLockDTO.getName());
            jsDependencyTreeDO.setVersion(packageLockDTO.getVersion());
            JsComponentDependencyTreeDO componentDependencyTreeDO = convertNode(packageLockDTO, type);
            jsDependencyTreeDO.setTree(componentDependencyTreeDO);
            return jsDependencyTreeDO;
        } catch (Exception e) {
            throw new PlatformException(500, "解析依赖树失败");
        }
    }

    /**
     * 根据依赖树信息生成依赖平铺列表
     *
     * @param jsDependencyTreeDO 依赖树信息
     * @return List<JsDependencyTableDO> 依赖平铺列表
     */
    @Override
    public List<JsDependencyTableDO> dependencyTableAnalysis(JsDependencyTreeDO jsDependencyTreeDO) {
        List<JsDependencyTableDO> jsDependencyTableDOS = new ArrayList<>();
        Queue<JsComponentDependencyTreeDO> queue = new LinkedList<>(jsDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            JsDependencyTableDO jsDependencyTableDO = new JsDependencyTableDO();
            jsDependencyTableDO.setName(jsDependencyTreeDO.getName());
            jsDependencyTableDO.setVersion(jsDependencyTreeDO.getVersion());
            JsComponentDependencyTreeDO jsComponentDependencyTree = queue.poll();
            jsDependencyTableDO.setCName(jsComponentDependencyTree.getName());
            jsDependencyTableDO.setCVersion(jsComponentDependencyTree.getVersion());
            jsDependencyTableDO.setDepth(jsComponentDependencyTree.getDepth());
            jsDependencyTableDO.setDirect(jsDependencyTableDO.getDepth() == 1);
            jsDependencyTableDO.setType(jsComponentDependencyTree.getType());
            jsDependencyTableDO.setLanguage("javaScript");
            jsDependencyTableDO.setLicenses(jsComponentDependencyTree.getLicenses());
            jsDependencyTableDO.setVulnerabilities(jsComponentDependencyTree.getVulnerabilities());
            queue.addAll(jsComponentDependencyTree.getDependencies());
            jsDependencyTableDOS.add(jsDependencyTableDO);
        }
        return jsDependencyTableDOS;
    }

    /**
     * 根据爬虫获得依赖信息
     *
     * @param name    组件名称
     * @param version 组件版本
     * @return JsDependencyTreeDO 组件依赖信息
     */
    @Override
    public JsDependencyTreeDO spiderDependency(String name, String version) {
        // 先检查组件库中是否有对应信息，若无，爬取对应信息
        JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(name, version);
        if (null == jsComponentDO){
            jsComponentDO = jsSpiderService.crawlByNV(name, version);
            if (null == jsComponentDO){
                throw new PlatformException(500, "无法识别的组件： " + name + ":" + version);
            }
            jsComponentDao.save(jsComponentDO);
        }

        // 设置爬取存储的临时文件夹
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timestamp = dateFormat.format(now);
        File tempDir = new File(tempFolder, timestamp);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        try {
            // 爬取对应组件的package.json文件，下载到指定路径
            jsSpiderService.spiderContent(name, version, tempDir.getPath());
            String tmpNpmPath = tempDir.getPath() + FILE_SEPARATOR + "package.json";
            return dependencyTreeAnalysis(tmpNpmPath, "package.json", "opensource");
        } catch (Exception e) {
            throw new PlatformException(500, "识别依赖信息失败");
        } finally {
            FolderUtil.deleteFolder(tempDir.getPath());
        }
    }


    /**
     * 将JsComponentDependencyTreeDO转换为AppComponentDependencyTreeDO
     *
     * @param jsComponentDependencyTreeDO Js组件依赖信息
     * @return AppComponentDependencyTreeDO App组件依赖信息
     */
    @Override
    public AppComponentDependencyTreeDO translateComponentDependencyTree(JsComponentDependencyTreeDO jsComponentDependencyTreeDO) {
        if (jsComponentDependencyTreeDO == null) {
            return null;
        }
        AppComponentDependencyTreeDO appComponentDependencyTreeDO = new AppComponentDependencyTreeDO();
        appComponentDependencyTreeDO.setName(jsComponentDependencyTreeDO.getName());
        appComponentDependencyTreeDO.setVersion(jsComponentDependencyTreeDO.getVersion());
        appComponentDependencyTreeDO.setDepth(jsComponentDependencyTreeDO.getDepth());
        appComponentDependencyTreeDO.setType(jsComponentDependencyTreeDO.getType());
        appComponentDependencyTreeDO.setLicenses(jsComponentDependencyTreeDO.getLicenses());
        appComponentDependencyTreeDO.setVulnerabilities(jsComponentDependencyTreeDO.getVulnerabilities());
        appComponentDependencyTreeDO.setLanguage("javaScript");
        List<AppComponentDependencyTreeDO> dependencies = new ArrayList<>();
        for (JsComponentDependencyTreeDO childJsComponentDependencyTreeDO : jsComponentDependencyTreeDO.getDependencies()) {
            AppComponentDependencyTreeDO childAppComponentDependencyTreeDO = translateComponentDependencyTree(childJsComponentDependencyTreeDO);
            dependencies.add(childAppComponentDependencyTreeDO);
        }
        appComponentDependencyTreeDO.setDependencies(dependencies);
        return appComponentDependencyTreeDO;
    }

    /**
     * 将Js组件依赖表转换成应用组件依赖表
     *
     * @param jsDependencyTableDOS js组件依赖表
     * @return List<AppDependencyTableDO> 应用组件依赖表
     */
    @Override
    public List<AppDependencyTableDO> translateDependencyTable(List<JsDependencyTableDO> jsDependencyTableDOS) {
        List<AppDependencyTableDO> appDependencyTableDOS = new ArrayList<>();
        for (JsDependencyTableDO jsDependencyTableDO : jsDependencyTableDOS) {
            AppDependencyTableDO appDependencyTableDO = new AppDependencyTableDO();
            BeanUtils.copyProperties(jsDependencyTableDO, appDependencyTableDO);
            appDependencyTableDOS.add(appDependencyTableDO);
        }
        return appDependencyTableDOS;
    }

    /**
     * 生成package-lock.json文件
     *
     * @param filePath package.json所在文件夹路径
     */
    public void generatePackageLock(String filePath) {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(filePath);
            List<String> command = List.of("cmd.exe", "/c", "npm install", "--package-lock-only", "--legacy-peer-deps");
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(file);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
        } catch (Exception e) {
            throw new PlatformException(500, "生成Package-lock.json文件失败");
        }
    }

    /**
     * 解析package-lock.json文件
     *
     * @param filePath package-lock.json文件路径
     * @return PackageLockDTO package-lock.json信息
     */
    public PackageLockDTO parsePackageLock(String filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PackageLockDTO packageLockDTO = objectMapper.readValue(new File(filePath), PackageLockDTO.class);
            return packageLockDTO;
        } catch (Exception e) {
            throw new PlatformException(500, "解析package-lock.json失败");
        }
    }

    /**
     * 解析package.json文件
     *
     * @param filePath package.json文件路径
     * @return PackageJsonDTO package.json信息
     */
    public PackageJsonDTO parsePackageJson(String filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PackageJsonDTO packageJsonDTO = objectMapper.readValue(new File(filePath), PackageJsonDTO.class);
            return packageJsonDTO;
        } catch (Exception e) {
            throw new PlatformException(500, "解析package.json失败");
        }
    }

    /**
     * 递归解析packLockDTO, 返回根节点
     *
     * @param packageLockDTO
     * @return ComponentDependencyTreeDO 组件依赖信息树
     */
    private JsComponentDependencyTreeDO convertPackageLock(PackageLockDTO packageLockDTO) {
        JsComponentDependencyTreeDO jsComponentDependencyTreeDO = new JsComponentDependencyTreeDO();
        jsComponentDependencyTreeDO.setName(packageLockDTO.getName());
        jsComponentDependencyTreeDO.setVersion(packageLockDTO.getVersion());
        jsComponentDependencyTreeDO.setType("-");
        return jsComponentDependencyTreeDO;
    }

    /**
     * 解析上传文件
     *
     * @param filePath 上传文件路径
     */
    private void extractFile(String filePath) throws Exception {
        File file = new File(filePath);
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("package.json")) {
                    try (InputStream inputStream = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(file.getParent() + FILE_SEPARATOR + name)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new PlatformException(500, "解压zip文件失败");
        }
        File packageJson = new File(file.getParent() + FILE_SEPARATOR + "package.json");
        if (!packageJson.exists()) {
            throw new PlatformException(500, "zip文件中未找到package.json文件");
        }
    }

    /**
     * 解析包名，提取namespace和name
     *
     * @param packageName 包名
     * @return String[] namespace和name
     */
    public String[] parsePackageName(String packageName) {
        String regex = "^(@[^/]+)/(.+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(packageName);
        if (matcher.find()) {
            String namespace = matcher.group(1);
            String name = matcher.group(2);
            return new String[]{namespace, name};
        }
        return null;
    }

    /**
     * 根据packageLockDTO生成ComponentDependencyTreeDO
     *
     * @param packageLockDTO package-lock.json依赖信息
     * @param type           组件类型
     * @return ComponentDependencyTreeDO 依赖信息
     */
    private JsComponentDependencyTreeDO convertNode(PackageLockDTO packageLockDTO, String type) {
        JsComponentDependencyTreeDO root = new JsComponentDependencyTreeDO();
        root.setName(packageLockDTO.getName());
        root.setVersion(packageLockDTO.getVersion());
        root.setDepth(0);
        root.setType(type);
        root.setDependencies(convertDependencies(packageLockDTO.getDependencies(), 1));
        return root;
    }

    /**
     * 根据packageLockDTO生成子依赖信息
     *
     * @param dependencies 依赖信息
     * @param depth        深度
     * @return List<ComponentDependencyTreeDO> 子依赖信息
     */
    private List<JsComponentDependencyTreeDO> convertDependencies(Map<String, PackageLockDependencyDTO> dependencies, int depth) {
        if (dependencies == null) {
            return null;
        }
        List<JsComponentDependencyTreeDO> children = new ArrayList<>();
        for (Map.Entry<String, PackageLockDependencyDTO> entry : dependencies.entrySet()) {
            JsComponentDependencyTreeDO child = new JsComponentDependencyTreeDO();
            child.setName(entry.getKey());
            child.setVersion(entry.getValue().getVersion());
            //增量更新机制
            JsComponentDO jsComponentDO = null;
            jsComponentDO = jsComponentDao.findByNameAndVersion(entry.getKey(), entry.getValue().getVersion());
            if (jsComponentDO == null) {
                jsComponentDO = jsSpiderService.crawlByNV(entry.getKey(), entry.getValue().getVersion());
                if (jsComponentDO != null) {
                    child.setType("opensource");
                    child.setLicenses(String.join(",", jsComponentDO.getLicenses()));
                    child.setVulnerabilities(String.join(",", jsComponentDO.getVulnerabilities()));
                    try {
                        jsComponentDao.save(jsComponentDO);
                    } catch (Exception e){
                        // save组件时出现错误，跳过该组件，仍继续执行
                        log.error("组件存入数据库失败：" + jsComponentDO.toString());
                        e.printStackTrace();
                    }

                } else {
                    // 如果爬虫没有爬到则打印报错信息，仍继续执行
                    child.setType("opensource");
                    log.error("存在未识别的组件：" + entry.getKey() + ":" + entry.getValue().getVersion());
                }
            } else {
                child.setLicenses(String.join(",", jsComponentDO.getLicenses()));
                child.setVulnerabilities(String.join(",", jsComponentDO.getVulnerabilities()));
                child.setType(jsComponentDO.getType());
            }
            child.setDepth(depth);
            child.setDependencies(convertDependencies(entry.getValue().getDependencies(), depth + 1));
            children.add(child);
        }
        return children;
    }
}
