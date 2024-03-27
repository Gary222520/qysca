package nju.edu.cn.qysca.service.npm;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.component.JsComponentDao;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.npm.PackageJsonDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDTO;
import nju.edu.cn.qysca.domain.npm.PackageLockDependencyDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Service
public class NpmServiceImpl implements NpmService {

    @Autowired
    private JsComponentDao jsComponentDao;

    private final String FILE_SEPARATOR = "/";

    /**
     * 根据package.json生成ComponentDO
     *
     * @param filePath package.json文件路径
     * @param type     组件类型
     * @return JsComponentDO Js组件信息
     */
    @Override
    public JsComponentDO componentAnalysis(String filePath, String type) {
        PackageJsonDTO packageJsonDTO = parsePackageJson(filePath);
        JsComponentDO jsComponentDO = new JsComponentDO();
        String name = packageJsonDTO.getName();
        if (name.contains("@")) {
            String[] temp = parsePackageName(name);
            jsComponentDO.setNamespace(temp[0]);
            jsComponentDO.setArtifactId(temp[1]);
        } else {
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
     * @param filePath package-lock.json文件路径
     * @param type     组件类型
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
     *
     * @param jsDependencyTreeDO 依赖树信息
     * @return List<JsDependencyTableDO> 依赖平铺列表
     */
    @Override
    public List<JsDependencyTableDO> dependencyTableAnalysis(JsDependencyTreeDO jsDependencyTreeDO) {
        List<JsDependencyTableDO> jsDependencyTableDOS = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(jsDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            JsDependencyTableDO jsDependencyTableDO = new JsDependencyTableDO();
            jsDependencyTableDO.setName(jsDependencyTreeDO.getName());
            jsDependencyTableDO.setVersion(jsDependencyTreeDO.getVersion());
            ComponentDependencyTreeDO componentDependencyTree = queue.poll();
            jsDependencyTableDO.setCName(componentDependencyTree.getArtifactId());
            jsDependencyTableDO.setCVersion(componentDependencyTree.getVersion());
            jsDependencyTableDO.setDepth(componentDependencyTree.getDepth());
            jsDependencyTableDO.setDirect(jsDependencyTableDO.getDepth() == 1);
            jsDependencyTableDO.setType(componentDependencyTree.getType());
            jsDependencyTableDO.setLanguage("javaScript");
            queue.addAll(componentDependencyTree.getDependencies());
            jsDependencyTableDOS.add(jsDependencyTableDO);
        }
        return jsDependencyTableDOS;
    }

    /**
     * 根据爬虫获得依赖信息
     * @param name 组件名称
     * @param version 组件版本
     * @return JsDependencyTreeDO 组件依赖信息
     */
    @Override
    public JsDependencyTreeDO spiderDependencyTree(String name, String version) {
        JsDependencyTreeDO jsDependencyTreeDO = new JsDependencyTreeDO();
        jsDependencyTreeDO.setName(name);
        jsDependencyTreeDO.setVersion(version);
        ComponentDependencyTreeDO componentDependencyTreeDO = buildDependencyTree(name, version, 0);
        jsDependencyTreeDO.setTree(componentDependencyTreeDO);
        return jsDependencyTreeDO;
    }

    /**
     * 爬虫构建树状依赖信息
     * @param name 组件名称
     * @param version 组件版本
     * @param depth 深度
     * @return ComponentDependencyTreeDO 依赖树信息
     */
    private ComponentDependencyTreeDO buildDependencyTree(String name, String version, int depth){
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        componentDependencyTreeDO.setArtifactId(name);
        componentDependencyTreeDO.setVersion(version);
        componentDependencyTreeDO.setDepth(depth);
        componentDependencyTreeDO.setType("opensource");
        List<ComponentDependencyTreeDO> dependencyTreeDOS = new ArrayList<>();
        try {
            String url = "https://registry.npmjs.org/" + name + FILE_SEPARATOR + version;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                throw new PlatformException(500, "存在未识别的组件");
            }
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject jsonObject = JSON.parseObject(content);
            JSONObject dependencies = jsonObject.getJSONObject("dependencies");
            JSONObject devDependencies = jsonObject.getJSONObject("devDependencies");
            if (dependencies != null) {
                for (Map.Entry<String, Object> entry : dependencies.entrySet()) {
                    String childName = entry.getKey();
                    String childVersion = (String) entry.getValue();
                    if (childVersion.contains("^")) {
                        childVersion = resolveVersion(childName, childVersion.substring(1));
                    }
                    ComponentDependencyTreeDO childComponentDependencyTreeDO = buildDependencyTree(childName, childVersion, depth + 1);
                    dependencyTreeDOS.add(childComponentDependencyTreeDO);
                }
            }
            if (devDependencies != null) {
                for (Map.Entry<String, Object> entry : devDependencies.entrySet()) {
                    String childName = entry.getKey();
                    String childVersion = (String) entry.getValue();
                    if (childVersion.contains("^")) {
                        childVersion = resolveVersion(childName, childVersion.substring(1));
                    }
                    ComponentDependencyTreeDO childComponentDependencyTreeDO = buildDependencyTree(childName, childVersion, depth + 1);
                    dependencyTreeDOS.add(childComponentDependencyTreeDO);
                }
            }
            componentDependencyTreeDO.setDependencies(dependencyTreeDOS);
        }catch (Exception e){
            throw new PlatformException(500, "存在未识别的组件");
        }
        return componentDependencyTreeDO;
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
     *
     * @param packageName 包名
     * @return String[] namespace和name
     */
    private String[] parsePackageName(String packageName) {
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
     *
     * @param dependencies 依赖信息
     * @param depth        深度
     * @return List<ComponentDependencyTreeDO> 子依赖信息
     */
    private List<ComponentDependencyTreeDO> convertDependencies(Map<String, PackageLockDependencyDTO> dependencies, int depth) {
        if (dependencies == null) {
            return null;
        }
        List<ComponentDependencyTreeDO> children = new ArrayList<>();
        for (Map.Entry<String, PackageLockDependencyDTO> entry : dependencies.entrySet()) {
            ComponentDependencyTreeDO child = new ComponentDependencyTreeDO();
            child.setArtifactId(entry.getKey());
            child.setVersion(entry.getValue().getVersion());
            //增量更新机制
            JsComponentDO jsComponentDO = null;
            if (entry.getKey().contains("@")) {
                String[] temp = parsePackageName(entry.getKey());
                jsComponentDO = jsComponentDao.findByNamespaceAndNameAndVersion(temp[0], temp[1], entry.getValue().getVersion());
            } else {
                jsComponentDO = jsComponentDao.findByNamespaceAndNameAndVersion("-", entry.getKey(), entry.getValue().getVersion());
            }
            if (jsComponentDO == null) {
                jsComponentDO = spiderComponentInfo(entry.getKey(), entry.getValue().getVersion());
                if (jsComponentDO != null) {
                    child.setType("opensource");
                    jsComponentDao.save(jsComponentDO);
                } else {
                    child.setType("opensource");
                    throw new PlatformException(500, "存在未识别的组件");
                }
            }
            child.setType(jsComponentDO.getType());
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
    private JsComponentDO spiderComponentInfo(String name, String version) {
        JsComponentDO jsComponentDO = new JsComponentDO();
        if (name.contains("@")) {
            String[] temp = parsePackageName(name);
            jsComponentDO.setNamespace(temp[0]);
            jsComponentDO.setArtifactId(temp[1]);
        } else {
            jsComponentDO.setNamespace("-");
            jsComponentDO.setArtifactId(name);
        }
        jsComponentDO.setVersion(version);
        jsComponentDO.setPurl("pkg:npm/" + name + "@" + version);
        jsComponentDO.setLanguage("javaScript");
        jsComponentDO.setType("opensource");
        jsComponentDO.setCreator("-");
        try {
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
            jsComponentDO.setRepoUrl(jsonObject.getJSONObject("repository").getString("url"));
            jsComponentDO.setLicense(jsonObject.getString("license"));
            jsComponentDO.setDownloadUrl(jsonObject.getJSONObject("dist").getString("tarball"));
            List<String> copyrightStatements = new ArrayList<>();
            copyrightStatements.add(jsonObject.getJSONObject("author").getString("name") + " ," + jsonObject.getJSONObject("author").getString("email"));
            JSONArray jsonArray = jsonObject.getJSONArray("contributors");
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
            jsComponentDO.setCopyrightStatements(copyrightStatements.toArray(new String[0]));
        } catch (Exception e) {
            return null;
        }
        return jsComponentDO;
    }

    /**
     * 解析版本约束，找到符合条件的最新版本
     *
     * @param name       项目名称
     * @param minVersion 最小版本
     * @return String 符合条件的最新版本
     */
    private String resolveVersion(String name, String minVersion) {
        String result = minVersion;
        try {
            String url = "https://registry.npmjs.org/" + name;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(url);
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            CloseableHttpResponse response = httpClient.execute(httpGet);
            if (response.getStatusLine().getStatusCode() != 200) {
                return null;
            }
            String content = EntityUtils.toString(response.getEntity(), "UTF-8");
            JSONObject jsonObject = JSON.parseObject(content);
            JSONObject versions = jsonObject.getJSONObject("versions");
            List<String> sortedVersions = new ArrayList<>(versions.keySet());
            Collections.sort(sortedVersions);
            String upLimitVersion = upLimitVersion(minVersion);
            for (int i = sortedVersions.size() - 1; i >= 0; i--) {
                if(sortedVersions.get(i).compareTo(upLimitVersion) < 0 &&sortedVersions.get(i).compareTo(minVersion) >= 0){
                    result =  sortedVersions.get(i);
                    return result;
                }
            }
        } catch (Exception e) {
            return null;
        }
        return result;
    }


    /**
     * 获取大于指定版本的整数版本
     * @param minVersion 最小版本
     * @return String 大于指定版本的整数版本
     */
    private String upLimitVersion(String minVersion) {
        Integer index = minVersion.indexOf(".");
        Integer  majorVersion = Integer.parseInt(minVersion.substring(0, index));
        StringBuilder sb = new StringBuilder();
        sb.append(majorVersion + 1).append(".0.0");
        return sb.toString();
    }
}
