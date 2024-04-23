package nju.edu.cn.qysca.service.sbom;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.application.AppComponentDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTreeDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.domain.application.dos.*;
import nju.edu.cn.qysca.domain.application.dtos.ApplicationSearchDTO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.ComponentSearchNameDTO;
import nju.edu.cn.qysca.domain.sbom.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.utils.FolderUtil;
import nju.edu.cn.qysca.utils.ZipUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SBOMServiceImpl implements SBOMService {
    @Autowired
    private ApplicationDao applicationDao;
    @Autowired
    private AppComponentDao appComponentDao;
    @Autowired
    private AppDependencyTreeDao appDependencyTreeDao;
    @Autowired
    private AppDependencyTableDao appDependencyTableDao;
    @Autowired
    private JavaComponentDao javaComponentDao;
    @Autowired
    private JavaDependencyTreeDao javaDependencyTreeDao;
    @Autowired
    private JavaDependencyTableDao javaDependencyTableDao;
    @Autowired
    private GoComponentDao goComponentDao;
    @Autowired
    private GoDependencyTreeDao goDependencyTreeDao;
    @Autowired
    private GoDependencyTableDao goDependencyTableDao;
    @Autowired
    private JsComponentDao jsComponentDao;
    @Autowired
    private JsDependencyTreeDao jsDependencyTreeDao;
    @Autowired
    private JsDependencyTableDao jsDependencyTableDao;
    @Autowired
    private PythonComponentDao pythonComponentDao;
    @Autowired
    private PythonDependencyTreeDao pythonDependencyTreeDao;
    @Autowired
    private PythonDependencyTableDao pythonDependencyTableDao;
    @Value("${tempSBOMPath}")
    private String tempFolder;

    /**
     * 导出应用SBOM
     *
     * @param applicationSearchDTO 应用搜索信息
     * @param response HttpServletResponse
     */
    @Override
    public void exportSBOM(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        if (applicationDO == null) {
            throw new PlatformException(500, "应用不存在: name= " + applicationSearchDTO.getName() + " ;version= " + applicationSearchDTO.getVersion());
        }

        // 生成临时文件夹
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);
        File tempDir = new File(tempFolder, timeStamp);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        try {
            // 底层应用时，生成一个json文件
            if (applicationDO.getChildApplication().length==0 && applicationDO.getChildComponent().isEmpty()) {
                AppDependencyTreeDO appDependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
                if (appDependencyTreeDO == null)
                    throw new PlatformException(500, "应用缺少组件信息");
                // 准备sbomDTO
                SbomDTO sbomDTO = new SbomDTO();
                List<SbomComponentDTO> sbomComponentDTOList = new ArrayList<>();
                List<SbomDependencyDTO> sbomDependencyDTOList= new ArrayList<>();
                List<AppDependencyTableDO> appDependencyTableDOS = appDependencyTableDao.findAllByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
                for (AppDependencyTableDO dependencyTable : appDependencyTableDOS) {
                    // 最底层的app的依赖组件应该都是四张语言表里的
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), dependencyTable.getLanguage()));
                }

                AppComponentDependencyTreeDO root = appDependencyTreeDO.getTree();
                Queue<AppComponentDependencyTreeDO> queue = new LinkedList<>();
                queue.offer(root);
                while (!queue.isEmpty()) {
                    AppComponentDependencyTreeDO node = queue.poll();
                    if (!node.equals(root)) {
                        ComponentSearchNameDTO component = new ComponentSearchNameDTO(node.getName(), node.getVersion());
                        List<ComponentSearchNameDTO> dependencies = node.getDependencies().stream()
                                .map(dependencyNode -> new ComponentSearchNameDTO(dependencyNode.getName(), dependencyNode.getVersion()))
                                .collect(Collectors.toList());

                        SbomDependencyDTO sbomDependencyDTO = getDependencyDTO(component, dependencies, node.getLanguage());
                        if (sbomDependencyDTO != null)
                            sbomDependencyDTOList.add(sbomDependencyDTO);

                    }
                    for (AppComponentDependencyTreeDO dependencyNode : node.getDependencies()) {
                        queue.offer(dependencyNode);
                    }
                }

                sbomDTO.setName(applicationDO.getName());
                sbomDTO.setVersion(applicationDO.getVersion());
                sbomDTO.setComponents(sbomComponentDTOList);
                sbomDTO.setDependencies(sbomDependencyDTOList);

                Date now2 = new Date();
                SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat2.setTimeZone(TimeZone.getTimeZone("UTC"));
                String timeStamp2 = dateFormat2.format(now2) + " UTC";
                sbomDTO.setTimestamp(timeStamp2);

                // 生成json文件
                File file = makeSbomFile(tempDir, sbomDTO);
                // 写入HttpServletResponse请求
                export(response, file);
            } else {
                // 非底层应用，打包为zip
                // 生成sbom
                File dir = new File(tempDir, "SBOM");
                if (!dir.exists()){
                    dir.mkdirs();
                }

                exportSBOM(applicationDO.getId(), dir);
                // 打包文件夹
                File zip = new File(tempDir, dir.getName() + ".zip");
                ZipUtil.zipDirectory(dir, zip);

                // 写入HttpServletResponse请求
                export(response, zip);
            }
        } catch (PlatformException e){
            throw e;
        } catch (IOException e) {
            throw new PlatformException(500, "SBOM导出失败");
        } finally {
            FolderUtil.deleteFolder(tempDir.getPath());
        }
    }

    /**
     * 将文件写入请求中
     *
     * @param response HttpServletResponse
     * @param file 待传输的文件
     * @throws IOException
     */
    private void export(HttpServletResponse response, File file) throws IOException {
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + file.getName());
        try (InputStream inputStream = new FileInputStream(file);
             OutputStream outputStream = response.getOutputStream()) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();
        }
    }

    /**
     * 根据sbomDTO在指定目录下生成json文件
     *
     * @param dir 指定目录
     * @param sbomDTO
     * @return 生成的json文件
     */
    private File makeSbomFile(File dir, SbomDTO sbomDTO) {
        File file = new File(dir, sbomDTO.getName().replaceAll(":","-") + "-" + sbomDTO.getVersion() + ".json");
        try {
            // 将sbomDTO变为json
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(sbomDTO);

            // 将 SBOM JSON 写入临时文件
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            file.delete();
            throw new PlatformException(500, "SBOM导出失败");
        }
        return file;
    }

    /**
     * 在指定dir下，导出SBOM（递归的）
     *
     * @param appId 应用id
     * @param dir   目录
     */
    private void exportSBOM(String appId, File dir) {
        ApplicationDO applicationDO = applicationDao.findOneById(appId);
        if (applicationDO.getChildApplication().length==0 && applicationDO.getChildComponent().isEmpty()) {
            // 如果应用没有子组件和子应用了，那么生成一个json文件
            AppComponentDO appComponentDO = appComponentDao.findByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
            SbomDTO sbomDTO = getSbomDTO("app", appComponentDO.getId());
            makeSbomFile(dir, sbomDTO);
        } else {
            // 如果应用还有子组件和子应用，那么先生成一个文件夹
            File sonDir = new File(dir, applicationDO.getName().replaceAll(":","-"));
            if (!sonDir.exists()){
                sonDir.mkdirs();
            }
            // 为每个子组件生成一个json
            for (Map.Entry<String, List<String>> entry : applicationDO.getChildComponent().entrySet()) {
                for (String childComponentId : entry.getValue()) {
                    SbomDTO sbomDTO = getSbomDTO(entry.getKey(), childComponentId);
                    makeSbomFile(sonDir, sbomDTO);
                }
            }
            // 继续递归导出子应用
            for (String childAppId : applicationDO.getChildApplication()) {
                exportSBOM(childAppId, sonDir);
            }
        }
    }

    /**
     * 生成SbomDTO， 相当于一个json文件
     *
     * @param language    语言
     * @param componentId 组件id
     * @return SbomDTO
     */
    private SbomDTO getSbomDTO(String language, String componentId) {
        SbomDTO sbomDTO = new SbomDTO();
        List<SbomComponentDTO> sbomComponentDTOList = new ArrayList<>();
        List<SbomDependencyDTO> sbomDependencyDTOList = new ArrayList<>();

        switch (language) {
            case "app": {
                // 在这里认为，能进入这里的都是单一语言app(并且是最底层），并且已经发布过，能在appComponentDao中找到
                AppComponentDO appComponentDO = appComponentDao.getById(componentId);
                AppDependencyTreeDO appDependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(appComponentDO.getName(), appComponentDO.getVersion());
                if (appDependencyTreeDO == null)
                    throw new PlatformException(500, "应用缺少组件信息");
                sbomDTO.setName(appComponentDO.getName());
                sbomDTO.setVersion(appComponentDO.getVersion());
                List<AppDependencyTableDO> appDependencyTableDOS = appDependencyTableDao.findAllByNameAndVersion(appComponentDO.getName(), appComponentDO.getVersion());

                for (AppDependencyTableDO dependencyTable : appDependencyTableDOS) {
                    // 最底层的app的依赖组件应该都是四张语言表里的
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), dependencyTable.getLanguage()));
                }

                AppComponentDependencyTreeDO root = appDependencyTreeDO.getTree();
                Queue<AppComponentDependencyTreeDO> queue = new LinkedList<>();
                queue.offer(root);
                while (!queue.isEmpty()) {
                    AppComponentDependencyTreeDO node = queue.poll();
                    if (!node.equals(root)) {
                        ComponentSearchNameDTO component = new ComponentSearchNameDTO(node.getName(), node.getVersion());
                        List<ComponentSearchNameDTO> dependencies = node.getDependencies().stream()
                                .map(dependencyNode -> new ComponentSearchNameDTO(dependencyNode.getName(), dependencyNode.getVersion()))
                                .collect(Collectors.toList());

                        SbomDependencyDTO sbomDependencyDTO = getDependencyDTO(component, dependencies, node.getLanguage());
                        if (sbomDependencyDTO != null)
                            sbomDependencyDTOList.add(sbomDependencyDTO);

                    }
                    for (AppComponentDependencyTreeDO dependencyNode : node.getDependencies()) {
                        queue.offer(dependencyNode);
                    }
                }

                break;
            }
            case "java": {
                JavaComponentDO javaComponentDO = javaComponentDao.getById(componentId);
                JavaDependencyTreeDO javaDependencyTreeDO = javaDependencyTreeDao.findByNameAndVersion(javaComponentDO.getName(), javaComponentDO.getVersion());

                sbomDTO.setName(javaComponentDO.getName());
                sbomDTO.setVersion(javaComponentDO.getVersion());

                List<JavaDependencyTableDO> javaDependencyTableDOS = javaDependencyTableDao.findAllByNameAndVersion(javaComponentDO.getName(), javaComponentDO.getVersion());
                for (JavaDependencyTableDO dependencyTable : javaDependencyTableDOS) {
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "java"));
                }

                JavaComponentDependencyTreeDO root = javaDependencyTreeDO.getTree();
                Queue<JavaComponentDependencyTreeDO> queue = new LinkedList<>();
                queue.offer(root);
                while (!queue.isEmpty()) {
                    JavaComponentDependencyTreeDO node = queue.poll();
                    ComponentSearchNameDTO component = new ComponentSearchNameDTO(node.getName(), node.getVersion());
                    List<ComponentSearchNameDTO> dependencies = node.getDependencies().stream()
                            .map(dependencyNode -> new ComponentSearchNameDTO(dependencyNode.getName(), dependencyNode.getVersion()))
                            .collect(Collectors.toList());

                    SbomDependencyDTO sbomDependencyDTO = getDependencyDTO(component, dependencies, "java");
                    if (sbomDependencyDTO != null)
                        sbomDependencyDTOList.add(sbomDependencyDTO);

                    for (JavaComponentDependencyTreeDO dependencyNode : node.getDependencies()) {
                        queue.offer(dependencyNode);
                    }
                }

                break;
            }
            case "golang": {
                GoComponentDO goComponentDO = goComponentDao.getById(componentId);
                GoDependencyTreeDO goDependencyTreeDO = goDependencyTreeDao.findByNameAndVersion(goComponentDO.getName(), goComponentDO.getVersion());

                sbomDTO.setName(goComponentDO.getName());
                sbomDTO.setVersion(goComponentDO.getVersion());

                List<GoDependencyTableDO> goDependencyTableDOS = goDependencyTableDao.findAllByNameAndVersion(goComponentDO.getName(), goComponentDO.getVersion());
                for (GoDependencyTableDO dependencyTable : goDependencyTableDOS) {
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "golang"));
                }

                GoComponentDependencyTreeDO root = goDependencyTreeDO.getTree();
                Queue<GoComponentDependencyTreeDO> queue = new LinkedList<>();
                queue.offer(root);
                while (!queue.isEmpty()) {
                    GoComponentDependencyTreeDO node = queue.poll();
                    ComponentSearchNameDTO component = new ComponentSearchNameDTO(node.getName(), node.getVersion());
                    List<ComponentSearchNameDTO> dependencies = node.getDependencies().stream()
                            .map(dependencyNode -> new ComponentSearchNameDTO(dependencyNode.getName(), dependencyNode.getVersion()))
                            .collect(Collectors.toList());

                    SbomDependencyDTO sbomDependencyDTO = getDependencyDTO(component, dependencies, "golang");
                    if (sbomDependencyDTO != null)
                        sbomDependencyDTOList.add(sbomDependencyDTO);

                    for (GoComponentDependencyTreeDO dependencyNode : node.getDependencies()) {
                        queue.offer(dependencyNode);
                    }
                }

                break;
            }
            case "javaScript": {
                JsComponentDO jsComponentDO = jsComponentDao.getById(componentId);
                JsDependencyTreeDO jsDependencyTreeDO = jsDependencyTreeDao.findByNameAndVersion(jsComponentDO.getName(), jsComponentDO.getVersion());

                sbomDTO.setName(jsComponentDO.getName());
                sbomDTO.setVersion(jsComponentDO.getVersion());

                List<JsDependencyTableDO> jsDependencyTableDOS = jsDependencyTableDao.findAllByNameAndVersion(jsComponentDO.getName(), jsComponentDO.getVersion());
                for (JsDependencyTableDO dependencyTable : jsDependencyTableDOS) {
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "javaScript"));
                }

                JsComponentDependencyTreeDO root = jsDependencyTreeDO.getTree();
                Queue<JsComponentDependencyTreeDO> queue = new LinkedList<>();
                queue.offer(root);
                while (!queue.isEmpty()) {
                    JsComponentDependencyTreeDO node = queue.poll();
                    ComponentSearchNameDTO component = new ComponentSearchNameDTO(node.getName(), node.getVersion());
                    List<ComponentSearchNameDTO> dependencies = node.getDependencies().stream()
                            .map(dependencyNode -> new ComponentSearchNameDTO(dependencyNode.getName(), dependencyNode.getVersion()))
                            .collect(Collectors.toList());

                    SbomDependencyDTO sbomDependencyDTO = getDependencyDTO(component, dependencies, "javaScript");
                    if (sbomDependencyDTO != null)
                        sbomDependencyDTOList.add(sbomDependencyDTO);

                    for (JsComponentDependencyTreeDO dependencyNode : node.getDependencies()) {
                        queue.offer(dependencyNode);
                    }
                }

                break;
            }
            case "python": {
                PythonComponentDO pythonComponentDO = pythonComponentDao.getById(componentId);
                PythonDependencyTreeDO pythonDependencyTreeDO = pythonDependencyTreeDao.findByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion());

                sbomDTO.setName(pythonComponentDO.getName());
                sbomDTO.setVersion(pythonComponentDO.getVersion());

                List<PythonDependencyTableDO> pythonDependencyTableDOS = pythonDependencyTableDao.findAllByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion());
                for (PythonDependencyTableDO dependencyTable : pythonDependencyTableDOS) {
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "python"));
                }

                PythonComponentDependencyTreeDO root = pythonDependencyTreeDO.getTree();
                Queue<PythonComponentDependencyTreeDO> queue = new LinkedList<>();
                queue.offer(root);
                while (!queue.isEmpty()) {
                    PythonComponentDependencyTreeDO node = queue.poll();
                    ComponentSearchNameDTO component = new ComponentSearchNameDTO(node.getName(), node.getVersion());
                    List<ComponentSearchNameDTO> dependencies = node.getDependencies().stream()
                            .map(dependencyNode -> new ComponentSearchNameDTO(dependencyNode.getName(), dependencyNode.getVersion()))
                            .collect(Collectors.toList());

                    SbomDependencyDTO sbomDependencyDTO = getDependencyDTO(component, dependencies, "python");
                    if (sbomDependencyDTO != null)
                        sbomDependencyDTOList.add(sbomDependencyDTO);

                    for (PythonComponentDependencyTreeDO dependencyNode : node.getDependencies()) {
                        queue.offer(dependencyNode);
                    }
                }

                break;
            }
        }
        sbomDTO.setComponents(sbomComponentDTOList);
        sbomDTO.setDependencies(sbomDependencyDTOList);
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timeStamp = dateFormat.format(now) + " UTC";
        sbomDTO.setTimestamp(timeStamp);

        return sbomDTO;
    }

    /**
     * 生成ComponentDTO，相当于json文件中的一个component
     *
     * @param cName     组件名
     * @param cVersion  组件版本
     * @param direct    是否直接依赖
     * @param cLanguage 组件语言
     * @return SbomComponentDTO
     */
    private SbomComponentDTO getComponentDTO(String cName, String cVersion, boolean direct, String cLanguage) {
        switch (cLanguage) {
            case "java": {
                JavaComponentDO component = javaComponentDao.findByNameAndVersion(cName, cVersion);
                SbomJavaComponentDTO sbomJavaComponentDTO = new SbomJavaComponentDTO();
                sbomJavaComponentDTO.setGroup(cName.split(":")[0]);
                sbomJavaComponentDTO.setName(cName.split(":")[1]);
                sbomJavaComponentDTO.setVersion(cVersion);
                sbomJavaComponentDTO.setLanguage("java");
                if (component != null){
                    sbomJavaComponentDTO.setOrigin(component.getType());
                    sbomJavaComponentDTO.setDescription(component.getDescription());
                    sbomJavaComponentDTO.setPUrl(component.getPUrl());
                    sbomJavaComponentDTO.setLicenses(Arrays.asList(component.getLicenses()));
                    sbomJavaComponentDTO.setExternalReferences(Stream.of(
                                    new SbomExternalReferenceDTO("website", component.getUrl()),
                                    new SbomExternalReferenceDTO("distribution", component.getDownloadUrl()),
                                    new SbomExternalReferenceDTO("sourceUrl", component.getSourceUrl())
                            )
                            .filter(dto -> dto.getUrl() != null) // 过滤掉为空的外部链接
                            .collect(Collectors.toList()));
                    sbomJavaComponentDTO.setHashes(component.getHashes());
                }
                return sbomJavaComponentDTO;
            }
            case "golang": {
                GoComponentDO component = goComponentDao.findByNameAndVersion(cName, cVersion);

                SbomGoComponentDTO sbomGoComponentDTO = new SbomGoComponentDTO();
                sbomGoComponentDTO.setNamespace(cName.split("/")[0]);
                sbomGoComponentDTO.setArtifactId(cName.split("/")[cName.split("/").length - 1]);
                sbomGoComponentDTO.setVersion(cVersion);
                sbomGoComponentDTO.setPrimaryLanguage("golang");
                if (component != null) {
                    sbomGoComponentDTO.setOrigin(component.getType());
                    sbomGoComponentDTO.setDirectDependency(direct);
                    sbomGoComponentDTO.setDescription(component.getDescription());
                    sbomGoComponentDTO.setPUrl(component.getPUrl());
                    sbomGoComponentDTO.setLicenses(Arrays.asList(component.getLicenses()));
                    sbomGoComponentDTO.setWebsite(component.getUrl());
                    sbomGoComponentDTO.setDownloadUrl(component.getDownloadUrl());
                    sbomGoComponentDTO.setRepoUrl(component.getSourceUrl());
                }
                return sbomGoComponentDTO;
            }
            case "javaScript": {
                JsComponentDO component = jsComponentDao.findByNameAndVersion(cName, cVersion);

                SbomJsComponentDTO sbomJsComponentDTO = new SbomJsComponentDTO();
                if (cName.contains("/")) {
                    sbomJsComponentDTO.setNamespace(cName.split("/")[0]);
                    sbomJsComponentDTO.setArtifactId(cName.split("/")[1]);
                } else {
                    sbomJsComponentDTO.setArtifactId(cName);
                }
                sbomJsComponentDTO.setVersion(cVersion);
                sbomJsComponentDTO.setPrimaryLanguage("javaScript");
                if (component != null) {
                    sbomJsComponentDTO.setOrigin(component.getType());
                    sbomJsComponentDTO.setDirectDependency(direct);
                    sbomJsComponentDTO.setDescription(component.getDescription());
                    sbomJsComponentDTO.setPUrl(component.getPUrl());
                    sbomJsComponentDTO.setLicenses(Arrays.asList(component.getLicenses()));
                    sbomJsComponentDTO.setWebsite(component.getUrl());
                    sbomJsComponentDTO.setDownloadUrl(component.getDownloadUrl());
                    sbomJsComponentDTO.setRepoUrl(component.getSourceUrl());
                    sbomJsComponentDTO.setCopyrightStatements(component.getCopyrightStatements());
                }
                return sbomJsComponentDTO;
            }
            case "python": {
                PythonComponentDO component = pythonComponentDao.findByNameAndVersion(cName, cVersion);

                SbomPythonComponentDTO sbomPythonComponentDTO = new SbomPythonComponentDTO();
                sbomPythonComponentDTO.setName(cName);
                sbomPythonComponentDTO.setVersion(cVersion);
                sbomPythonComponentDTO.setLanguage("python");
                if (component != null) {
                    sbomPythonComponentDTO.setOrigin(component.getType());
                    sbomPythonComponentDTO.setDescription(component.getDescription());
                    sbomPythonComponentDTO.setPUrl(component.getPUrl());
                    sbomPythonComponentDTO.setLicenses(Arrays.asList(component.getLicenses()));
                    sbomPythonComponentDTO.setExternalReferences(Stream.of(
                                    new SbomExternalReferenceDTO("website", component.getUrl()),
                                    new SbomExternalReferenceDTO("distribution", component.getDownloadUrl()),
                                    new SbomExternalReferenceDTO("sourceUrl", component.getSourceUrl())
                            )
                            .filter(dto -> dto.getUrl() != null) // 过滤掉为空的外部链接
                            .collect(Collectors.toList()));
                }
                return sbomPythonComponentDTO;
            }
        }
        return null;
    }


    /**
     * 生成DependencyDTO，相当于json文件中的一个dependency
     *
     * @param component 组件
     * @param dependencies 依赖列表
     * @param language 语言
     * @return SbomComponentDTO
     */
    private SbomDependencyDTO getDependencyDTO(ComponentSearchNameDTO component, List<ComponentSearchNameDTO> dependencies, String language){
        SbomDependencyDTO sbomDependencyDTO = new SbomDependencyDTO();
        switch (language) {
            case "java": {
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(component.getName(), component.getVersion());
                if (null == javaComponentDO)
                    return null;
                sbomDependencyDTO.setRef(javaComponentDO.getPUrl());
                sbomDependencyDTO.setDependsOn(new ArrayList<>());
                for (ComponentSearchNameDTO dependency : dependencies) {
                    JavaComponentDO javaComponentDO1 = javaComponentDao.findByNameAndVersion(dependency.getName(), dependency.getVersion());
                    if (javaComponentDO1 != null)
                        sbomDependencyDTO.getDependsOn().add(javaComponentDO1.getPUrl());
                }
                break;
            }
            case "golang": {
                GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(component.getName(), component.getVersion());
                if (null == goComponentDO)
                    return null;
                sbomDependencyDTO.setRef(goComponentDO.getPUrl());
                sbomDependencyDTO.setDependsOn(new ArrayList<>());
                for (ComponentSearchNameDTO dependency : dependencies){
                    GoComponentDO goComponentDO1 = goComponentDao.findByNameAndVersion(dependency.getName(), dependency.getVersion());
                    if (goComponentDO1 != null)
                        sbomDependencyDTO.getDependsOn().add(goComponentDO1.getPUrl());
                }
                break;
            }
            case "javaScript": {
                JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(component.getName(), component.getVersion());
                if (null == jsComponentDO)
                    return null;
                sbomDependencyDTO.setRef(jsComponentDO.getPUrl());
                sbomDependencyDTO.setDependsOn(new ArrayList<>());
                for (ComponentSearchNameDTO dependency : dependencies){
                    JsComponentDO jsComponentDO1 = jsComponentDao.findByNameAndVersion(dependency.getName(), dependency.getVersion());
                    if (jsComponentDO1 != null)
                        sbomDependencyDTO.getDependsOn().add(jsComponentDO1.getPUrl());
                }
                break;
            }
            case "python": {
                PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(component.getName(), component.getVersion());
                if (null == pythonComponentDO)
                    return null;
                sbomDependencyDTO.setRef(pythonComponentDO.getPUrl());
                sbomDependencyDTO.setDependsOn(new ArrayList<>());
                for (ComponentSearchNameDTO dependency : dependencies){
                    PythonComponentDO pythonComponentDO1 = pythonComponentDao.findByNameAndVersion(dependency.getName(), dependency.getVersion());
                    if (pythonComponentDO1 != null)
                        sbomDependencyDTO.getDependsOn().add(pythonComponentDO1.getPUrl());
                }
                break;
            }
        }
        return sbomDependencyDTO;
    }
}
