package nju.edu.cn.qysca.service.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.npm.PackageJsonDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class NpmServiceImpl implements NpmService {


    private final String FILE_SEPARATOR = "/";
    /**
     * 根据package.json生成ComponentDO
     * @param filePath package.json文件路径
     * @return
     */
    @Override
    public JavaComponentDO componentAnalysis(String filePath) {
        return null;
    }

    /**
     *
     * @param packagePath package.json文件路径
     * @param filePath package-lock.json文件路径
     * @return JavaDependencyTreeDO 依赖树信息
     */
    @Override
    public JavaDependencyTreeDO dependencyTreeAnalysis(String packagePath, String filePath) {
        PackageJsonDTO packageJsonDTO = parsePackageJson(packagePath);
        JavaDependencyTreeDO javaDependencyTreeDO = new JavaDependencyTreeDO();
        javaDependencyTreeDO.setArtifactId(packageJsonDTO.getName());
        javaDependencyTreeDO.setVersion(packageJsonDTO.getVersion());
        PackageLockDTO packageLockDTO = parsePackageLock(filePath);
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setArtifactId(packageJsonDTO.getName());
        componentDependencyTreeDO.setVersion(packageJsonDTO.getVersion());
        componentDependencyTreeDO.setScope("-");
        componentDependencyTreeDO.setDepth(0);
        return javaDependencyTreeDO;
    }

    /**
     * 根据依赖树信息生成依赖平铺列表
     * @param javaDependencyTreeDO 依赖树信息
     * @return List<JavaDependencyTableDO> 依赖平铺列表
     */
    @Override
    public List<JavaDependencyTableDO> dependencyTableAnalysis(JavaDependencyTreeDO javaDependencyTreeDO) {
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
}
