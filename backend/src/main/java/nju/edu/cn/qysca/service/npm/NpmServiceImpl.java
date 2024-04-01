package nju.edu.cn.qysca.service.npm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.component.JsComponentDao;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.npm.PackageJsonDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDependencyDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.vulnerability.VulnerabilityService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
public class NpmServiceImpl implements NpmService {

    @Autowired
    private JsComponentDao jsComponentDao;

    @Autowired
    private LicenseService licenseService;

    @Autowired
    private VulnerabilityService vulnerabilityService;

    private final String FILE_SEPARATOR = "/";

    @Value("${tempPomFolder}")
    private String tempFolder;


    /**
     * 根据package.json生成ComponentDO
     *
     * @param filePath zip文件路径
     * @param type     组件类型
     * @return JsComponentDO Js组件信息
     */
    @Override
    public JsComponentDO componentAnalysis(String filePath, String type) {
        try {
            extractFile(filePath);
            File file = new File(filePath);
            PackageJsonDTO packageJsonDTO = parsePackageJson(file.getParent() + FILE_SEPARATOR + "package.json");
            JsComponentDO jsComponentDO = new JsComponentDO();
            String name = packageJsonDTO.getName();
            jsComponentDO.setName(name);
            jsComponentDO.setLanguage("javaScript");
            jsComponentDO.setVersion(packageJsonDTO.getVersion());
            jsComponentDO.setDescription(packageJsonDTO.getDescription());
            List<String> licenses = new ArrayList<>();
            licenses.addAll(licenseService.searchLicense(packageJsonDTO.getLicense()));
            jsComponentDO.setLicenses(licenses.toArray(new String[0]));
            jsComponentDO.setType(type);
            return jsComponentDO;
        }catch (Exception e){
            throw new PlatformException(500, "解析package.json失败");
        }

    }

    /**
     * @param filePath package-lock.json文件路径
     * @param type     组件类型
     * @return JsDependencyTreeDO 依赖树信息
     */
    @Override
    public JsDependencyTreeDO dependencyTreeAnalysis(String filePath, String type) {
        try {
            File file = new File(filePath);
            File packageLock = new File(file.getParent() + FILE_SEPARATOR + "package-lock.json");
            if (!packageLock.exists()) {
                extractFile(filePath);
            }
            PackageLockDTO packageLockDTO = parsePackageLock(file.getParent() + FILE_SEPARATOR + "package-lock.json");
            JsDependencyTreeDO jsDependencyTreeDO = new JsDependencyTreeDO();
            jsDependencyTreeDO.setName(packageLockDTO.getName());
            jsDependencyTreeDO.setVersion(packageLockDTO.getVersion());
            JsComponentDependencyTreeDO componentDependencyTreeDO = convertNode(packageLockDTO, type);
            jsDependencyTreeDO.setTree(componentDependencyTreeDO);
            return jsDependencyTreeDO;
        }catch (Exception e){
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
    public JsDependencyTreeDO spiderDependencyTree(String name, String version) {
        String filePath = null;
        try {
            filePath = spiderContent(name, version);
            generatePackageLock(filePath);
            return dependencyTreeAnalysis(filePath + FILE_SEPARATOR + "package-lock.json", "");
        }catch (Exception e){
            throw new PlatformException(500, "未能识别组件信息");
        } finally {
            deleteFolder(filePath);
        }
    }


    /**
     * 将JsComponentDependencyTreeDO转换为AppComponentDependencyTreeDO
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
            List<String> command = List.of("cmd.exe", "/c", "npm install", "--package-lock-only");
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
     * 提取上传的zip文件夹中的package.json文件和package-lock.json文件
     *
     * @param filePath zip文件路径
     * @throws Exception
     */
    private void extractFile(String filePath) throws Exception {
        File file = new File(filePath);
        try (ZipFile zipFile = new ZipFile(file)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.contains("package.json") || name.contains("package-lock.json")) {
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
        File packageLockJson = new File(file.getParent() + FILE_SEPARATOR + "package-lock.json");
        if (!packageLockJson.exists()) {
            throw new PlatformException(500, "zip文件中未找到package-lock.json文件");
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
                jsComponentDO = spiderComponentInfo(entry.getKey(), entry.getValue().getVersion());
                if (jsComponentDO != null) {
                    child.setType("opensource");
                    child.setLicenses(String.join(",", jsComponentDO.getLicenses()));
                    child.setVulnerabilities(String.join(",", jsComponentDO.getVulnerabilities()));
                    jsComponentDao.save(jsComponentDO);
                } else {
                    child.setType("opensource");
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

    /**
     * 利用爬虫获取组件信息
     *
     * @param name    组件名称
     * @param version 组件版本
     * @return JsComponentDO 组件信息
     */
    public JsComponentDO spiderComponentInfo(String name, String version) {
        JsComponentDO jsComponentDO = new JsComponentDO();
        jsComponentDO.setName(name);
        jsComponentDO.setVersion(version);
        jsComponentDO.setPurl("pkg:npm/" + name + "@" + version);
        jsComponentDO.setLanguage("javaScript");
        jsComponentDO.setType("opensource");
        jsComponentDO.setCreator("-");
        try {
            if (version.contains("npm")) {
                String[] temp = parsePackageNameAndVersion(version);
                if (temp != null) {
                    name = temp[0];
                    version = temp[1];
                }
            }
            String url = "https://registry.npmjs.org/" + name + FILE_SEPARATOR + version;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject jsonObject = JSON.parseObject(content);
            jsComponentDO.setDescription(jsonObject.getString("description"));
            jsComponentDO.setWebsite(jsonObject.getString("homepage"));
            if (jsonObject.get("repository") instanceof JSONObject) {
                jsComponentDO.setRepoUrl(jsonObject.getJSONObject("repository").getString("url"));
            } else {
                jsComponentDO.setRepoUrl(jsonObject.getString("repository"));
            }
            jsComponentDO.setLicenses(licenseService.searchLicense(jsonObject.getString("license")).toArray(new String[0]));
            jsComponentDO.setVulnerabilities(vulnerabilityService.findVulnerabilities(name, version, "javaScript").toArray(new String[0]));
            jsComponentDO.setDownloadUrl(jsonObject.getJSONObject("dist") == null ? "" : jsonObject.getJSONObject("dist").getString("tarball"));
            List<String> copyrightStatements = new ArrayList<>();
            if (jsonObject.get("author") instanceof JSONObject) {
                if (jsonObject.getJSONObject("author") != null) {
                    copyrightStatements.add(jsonObject.getJSONObject("author").getString("name") + " ," + jsonObject.getJSONObject("author").getString("email"));
                }
            }
            if (jsonObject.get("contributors") instanceof JSONArray) {
                JSONArray jsonArray = jsonObject.getJSONArray("contributors");
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.size(); i++) {
                        JSONObject contributor = jsonArray.getJSONObject(i);
                        StringBuilder contributorStr = new StringBuilder();
                        contributorStr.append(contributor.getString("name"));
                        if (contributor.containsKey("email")) {
                            contributorStr.append(" ,").append(contributor.getString("email"));
                        }
                        if (contributor.containsKey("url")) {
                            contributorStr.append(" ,").append(contributor.getString("url"));
                        }
                        copyrightStatements.add(contributorStr.toString());
                    }
                }
            }
            jsComponentDO.setCopyrightStatements(copyrightStatements.toArray(new String[0]));
            jsComponentDO.setState("SUCCESS");
        } catch (Exception e) {
            return null;
        }
        return jsComponentDO;
    }


    /**
     * 解析版本号
     *
     * @param version 版本号
     * @return String[] 组件名称和版本
     */
    private String[] parsePackageNameAndVersion(String version) {
        Pattern pattern = Pattern.compile("npm:(.+)@(.+)");
        Matcher matcher = pattern.matcher(version);
        if (matcher.find()) {
            return new String[]{matcher.group(1), matcher.group(2)};
        }
        return null;
    }

    private String spiderContent(String name, String version) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String tempDirName = dateFormat.format(new Date());
        File tempDir = new File(tempFolder, tempDirName);
        if(!tempDir.exists()){
            tempDir.mkdirs();
        }
        try{
            String url = "https://registry.npmjs.org/" + name + FILE_SEPARATOR + version;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                System.out.println(response.getStatusLine().getStatusCode());
                throw new PlatformException(500, "存在未识别的组件");
            }
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            File file = new File(tempDir, "package.json");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        }catch (Exception e){
            throw new PlatformException(500, "存在未识别的组件");
        }
        return  tempDir.getPath();
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
