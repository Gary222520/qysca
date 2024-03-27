package nju.edu.cn.qysca.service.npm;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;
import nju.edu.cn.qysca.domain.npm.PackageJsonDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class NpmServiceImpl implements NpmService {


    /**
     * 根据package.json生成ComponentDO
     * @param filePath package.json文件路径
     * @return
     */
    @Override
    public JavaComponentDO componentAnalysis(String filePath) {
        PackageJsonDTO packageJsonDTO = parsePackageJson(filePath);
        JavaComponentDO javaComponentDO = new JavaComponentDO();
        javaComponentDO.setArtifactId(packageJsonDTO.getName());
        javaComponentDO.setVersion(packageJsonDTO.getVersion());
        javaComponentDO.setLanguage("javaScript");
        javaComponentDO.setName(packageJsonDTO.getName());
        javaComponentDO.setDescription(packageJsonDTO.getDescription());
        javaComponentDO.setDownloadUrl("-");
        javaComponentDO.setUrl("-");
        javaComponentDO.setPUrl("-");
        return javaComponentDO;
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
        JavaComponentDependencyTreeDO javaComponentDependencyTreeDO = new JavaComponentDependencyTreeDO();
        javaComponentDependencyTreeDO.setArtifactId(packageJsonDTO.getName());
        javaComponentDependencyTreeDO.setVersion(packageJsonDTO.getVersion());
        javaComponentDependencyTreeDO.setScope("-");
        javaComponentDependencyTreeDO.setDepth(0);
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
     * @return JavaComponentDependencyTreeDO 组件依赖信息树
     */
    private JavaComponentDependencyTreeDO convertPackageLock(PackageLockDTO packageLockDTO) {
        JavaComponentDependencyTreeDO javaComponentDependencyTreeDO = new JavaComponentDependencyTreeDO();
        javaComponentDependencyTreeDO.setArtifactId(packageLockDTO.getName());
        javaComponentDependencyTreeDO.setVersion(packageLockDTO.getVersion());
        javaComponentDependencyTreeDO.setType("-");
        javaComponentDependencyTreeDO.setScope("-");
        return javaComponentDependencyTreeDO;
    }
}
