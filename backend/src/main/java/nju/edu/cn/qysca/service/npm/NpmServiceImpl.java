package nju.edu.cn.qysca.service.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.npm.PackageJsonDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDependencyDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class NpmServiceImpl implements NpmService {


    private final String FILE_SEPARATOR = "/";

    /**
     * 根据package.json生成ComponentDO
     * @param filePath package.json文件路径
     * @param type 组件类型
     * @return JsComponentDO Js组件信息
     */
    @Override
    public JsComponentDO componentAnalysis(String filePath, String type) {
        PackageJsonDTO packageJsonDTO = parsePackageJson(filePath);
        JsComponentDO jsComponentDO = new JsComponentDO();
        String name = packageJsonDTO.getName();
        if(name.contains("@")) {
            String[] temp = parsePackageName(name);
            jsComponentDO.setNamespace(temp[0]);
            jsComponentDO.setArtifactId(temp[1]);
        }else {
            jsComponentDO.setNamespace("-");
            jsComponentDO.setArtifactId(name);
        }
        jsComponentDO.setLanguage("javaScript");
        jsComponentDO.setVersion(packageJsonDTO.getVersion());
        jsComponentDO.setDescription(packageJsonDTO.getDescription());
        jsComponentDO.setLicense(packageJsonDTO.getLicense());
        jsComponentDO.setType(type);
        // TODO: 剩余部分信息
        return jsComponentDO;

    }

    /**
     *
     * @param filePath package-lock.json文件路径
     * @param type 组件类型
     * @return JsDependencyTreeDO 依赖树信息
     */
    @Override
    public JsDependencyTreeDO dependencyTreeAnalysis(String filePath, String type) {
        PackageLockDTO packageLockDTO = parsePackageLock(filePath);
        JsDependencyTreeDO jsDependencyTreeDO = new JsDependencyTreeDO();
        jsDependencyTreeDO.setName(packageLockDTO.getName());
        jsDependencyTreeDO.setVersion(packageLockDTO.getVersion());
        ComponentDependencyTreeDO componentDependencyTreeDO = convertNode(packageLockDTO, type);
        jsDependencyTreeDO.setTree(componentDependencyTreeDO);
        return jsDependencyTreeDO;
    }

    /**
     * 根据依赖树信息生成依赖平铺列表
     * @param jsDependencyTreeDO 依赖树信息
     * @return List<JsDependencyTableDO> 依赖平铺列表
     */
    @Override
    public List<JsDependencyTableDO> dependencyTableAnalysis(JsDependencyTreeDO jsDependencyTreeDO) {
        return null;
    }

    /**
     * 生成package-lock.json文件
     *
     * @param filePath package.json文件路径
     */
    public void generatePackageLock(String filePath) {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(filePath);
            List<String> command = List.of("cmd.exe", "/c", "npm install");
            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(file);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
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
     * @param filePath package.json文件路径
     * @return PackageJsonDTO package.json信息
     */
    public PackageJsonDTO parsePackageJson(String filePath) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            PackageJsonDTO packageJsonDTO = objectMapper.readValue(new File(filePath), PackageJsonDTO.class);
            return  packageJsonDTO;
        } catch (Exception e) {
            throw new PlatformException(500, "解析package.json失败");
        }
    }

    /**
     * 递归解析packLockDTO, 返回根节点
     * @param packageLockDTO
     * @return ComponentDependencyTreeDO 组件依赖信息树
     */
    private ComponentDependencyTreeDO convertPackageLock(PackageLockDTO packageLockDTO) {
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setArtifactId(packageLockDTO.getName());
        componentDependencyTreeDO.setVersion(packageLockDTO.getVersion());
        componentDependencyTreeDO.setType("-");
        componentDependencyTreeDO.setScope("-");
        return componentDependencyTreeDO;
    }

    /**
     * 提取上传的zip文件夹中的package.json文件和package-lock.json文件
     * @param filePath zip文件路径
     * @throws Exception
     */
    private void extractFile(String filePath) throws Exception{
        File file = new File(filePath);
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("package.json") || name.contains("package-lock.json")) {
                    try (InputStream inputStream = zipFile.getInputStream(entry);
                         FileOutputStream fos = new FileOutputStream(file.getParent() + FILE_SEPARATOR + "package.json")) {
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
        File packageLockJson = new File(file.getParent() + FILE_SEPARATOR + "package-lock.json");
        if (!packageLockJson.exists()) {
            throw new PlatformException(500, "zip文件中未找到package-lock.json文件");
        }
    }

    /**
     * 解析包名，提取namespace和name
     * @param packageName 包名
     * @return String[] namespace和name
     */
    private String[] parsePackageName(String packageName){
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
     * @param packageLockDTO package-lock.json依赖信息
     * @param type 组件类型
     * @return ComponentDependencyTreeDO 依赖信息
     */
    private ComponentDependencyTreeDO convertNode(PackageLockDTO packageLockDTO, String type) {
        ComponentDependencyTreeDO root = new ComponentDependencyTreeDO();
        root.setArtifactId(packageLockDTO.getName());
        root.setVersion(packageLockDTO.getVersion());
        root.setDepth(0);
        root.setType(type);
        root.setDependencies(convertDependencies(packageLockDTO.getDependencies(), 1));
        return root;
    }

    /**
     * 根据packageLockDTO生成子依赖信息
     * @param dependencies 依赖信息
     * @param depth 深度
     * @return List<ComponentDependencyTreeDO> 子依赖信息
     */
    private List<ComponentDependencyTreeDO> convertDependencies(Map<String, PackageLockDependencyDTO> dependencies, int depth) {
        if(dependencies == null) {
            return null;
        }
        List<ComponentDependencyTreeDO> children = new ArrayList<>();
        for(Map.Entry<String, PackageLockDependencyDTO> entry : dependencies.entrySet()) {
            ComponentDependencyTreeDO child = new ComponentDependencyTreeDO();
            child.setArtifactId(entry.getKey());
            child.setVersion(entry.getValue().getVersion());
            //增量更新机制
            child.setType("opensource");
            child.setDepth(depth);
            child.setDependencies(convertDependencies(entry.getValue().getDependencies(), depth + 1));
            children.add(child);
        }
        return children;
    }
}
