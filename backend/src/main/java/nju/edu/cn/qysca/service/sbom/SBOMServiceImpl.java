package nju.edu.cn.qysca.service.sbom;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.application.AppComponentDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTreeDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDO;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.ApplicationSearchDTO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.sbom.*;
import nju.edu.cn.qysca.exception.PlatformException;
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
            // 生成sbom文件
            File dir = new File(tempDir, "SBOM");
            exportSBOM(applicationDO.getId(), dir);

            // todo 打包文件夹
            ZipUtil.zipDirectory(tempDir, dir);
            File zip = new File(tempDir,dir.getName() + ".zip");
            // 写入HttpServletResponse请求
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + zip.getName());
            try (InputStream inputStream = new FileInputStream(zip);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.flush();
            }
        } catch (IOException e){
            throw new PlatformException(500, "SBOM导出失败");
        }
    }

    /**
     * 在指定dir下，导出SBOM（递归的）
     * @param appId 应用id
     * @param dir 目录
     */
    private void exportSBOM(String appId, File dir){
        ApplicationDO applicationDO = applicationDao.findOneById(appId);
        if (applicationDO.getChildApplication() == null && applicationDO.getChildComponent() == null) {
            // 如果应用没有子组件和子应用了，那么生成一个json文件
            AppComponentDO appComponentDO = appComponentDao.findByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
            SbomDTO sbomDTO = getSbomDTO("app", appComponentDO.getId());
            File file = new File(dir, sbomDTO.getName() + "-" + sbomDTO.getVersion() + ".json");
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
        } else {
            // 如果应用还有子组件和子应用，那么先生成一个文件夹
            File sonDir = new File(dir, applicationDO.getName());
            // 为每个子组件生成一个json
            for (Map.Entry<String, List<String>> entry : applicationDO.getChildComponent().entrySet()){
                for (String childComponentId: entry.getValue()){
                   SbomDTO sbomDTO =  getSbomDTO(entry.getKey(), childComponentId);

                    File file = new File(sonDir, sbomDTO.getName() + "-" + sbomDTO.getVersion() + ".json");
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
                }
            }
            // 继续递归导出子应用
            for (String childAppId : applicationDO.getChildApplication()){
                exportSBOM(childAppId, sonDir);
            }
        }
    }

    /**
     * 生成SbomDTO， 相当于一个json文件
     * @param language 语言
     * @param componentId 组件id
     * @return SbomDTO
     */
    private SbomDTO getSbomDTO(String language, String componentId){
        SbomDTO sbomDTO = new SbomDTO();
        List<SbomComponentDTO> sbomComponentDTOList = new ArrayList<>();


        switch (language){
            case "app":{
                // 在这里认为，能进入这里的都是单一语言app，并且已经发布过，能在appComponentDao中找到
                AppComponentDO appComponentDO = appComponentDao.getById(componentId);
                sbomDTO.setName(appComponentDO.getName());
                sbomDTO.setVersion(appComponentDO.getVersion());
                List<AppDependencyTableDO> appDependencyTableDOS = appDependencyTableDao.findAllByNameAndVersion(appComponentDO.getName(), appComponentDO.getVersion());

                for (AppDependencyTableDO dependencyTable : appDependencyTableDOS){
                    // todo 语言问题
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), dependencyTable.getLanguage()));
                }
                break;
            }
            case "java":{
                JavaComponentDO javaComponentDO = javaComponentDao.getById(componentId);
                sbomDTO.setName(javaComponentDO.getName());
                sbomDTO.setVersion(javaComponentDO.getVersion());
                List<JavaDependencyTableDO> javaDependencyTableDOS = javaDependencyTableDao.findAllByNameAndVersion(javaComponentDO.getName(), javaComponentDO.getVersion());

                for (JavaDependencyTableDO dependencyTable : javaDependencyTableDOS){
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "java"));
                }
                break;
            }
            case "golang":{
                GoComponentDO goComponentDO = goComponentDao.getById(componentId);
                sbomDTO.setName(goComponentDO.getName());
                sbomDTO.setVersion(goComponentDO.getVersion());
                List<GoDependencyTableDO> goDependencyTableDOS = goDependencyTableDao.findAllByNameAndVersion(goComponentDO.getName(), goComponentDO.getVersion());

                for (GoDependencyTableDO dependencyTable: goDependencyTableDOS){
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "golang"));
                }
                break;
            }
            case "javaScript":{
                JsComponentDO jsComponentDO = jsComponentDao.getById(componentId);
                sbomDTO.setName(jsComponentDO.getName());
                sbomDTO.setVersion(jsComponentDO.getVersion());
                List<JsDependencyTableDO> jsDependencyTableDOS = jsDependencyTableDao.findAllByNameAndVersion(jsComponentDO.getName(), jsComponentDO.getVersion());

                for (JsDependencyTableDO dependencyTable: jsDependencyTableDOS){
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "javaScript"));
                }
                break;
            }
            case "python":{
                PythonComponentDO pythonComponentDO = pythonComponentDao.getById(componentId);
                sbomDTO.setName(pythonComponentDO.getName());
                sbomDTO.setVersion(pythonComponentDO.getVersion());
                List<PythonDependencyTableDO> pythonDependencyTableDOS = pythonDependencyTableDao.findAllByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion());

                for (PythonDependencyTableDO dependencyTable : pythonDependencyTableDOS){
                    sbomComponentDTOList.add(getComponentDTO(dependencyTable.getCName(), dependencyTable.getCVersion(), dependencyTable.getDirect(), "python"));
                }
                break;
            }
        }
        sbomDTO.setComponents(sbomComponentDTOList);
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss UTC");
        String timeStamp = dateFormat.format(now);
        sbomDTO.setTimestamp(timeStamp);

        return sbomDTO;
    }

    /**
     * 生成ComponentDTO，相当于json文件中的一个component
     * @param cName 组件名
     * @param cVersion 组件版本
     * @param direct 是否直接依赖
     * @param cLanguage 组件语言
     * @return SbomComponentDTO
     */
    private SbomComponentDTO getComponentDTO(String cName, String cVersion, boolean direct, String cLanguage){
        switch (cLanguage){
            case "java": {
                JavaComponentDO component = javaComponentDao.findByNameAndVersion(cName, cVersion);

                SbomJavaComponentDTO sbomJavaComponentDTO = new SbomJavaComponentDTO();
                sbomJavaComponentDTO.setGroup(component.getName().split(":")[0]);
                sbomJavaComponentDTO.setName(component.getName().split(":")[1]);
                sbomJavaComponentDTO.setVersion(component.getVersion());
                sbomJavaComponentDTO.setLanguage(component.getLanguage());
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
                return sbomJavaComponentDTO;
            }
            case "golang": {
                GoComponentDO component = goComponentDao.findByNameAndVersion(cName, cVersion);

                SbomGoComponentDTO sbomGoComponentDTO = new SbomGoComponentDTO();
                sbomGoComponentDTO.setNamespace(component.getName().split("/")[0]);
                sbomGoComponentDTO.setArtifactId(component.getName().split("/")[component.getName().split("/").length-1]);
                sbomGoComponentDTO.setVersion(component.getVersion());
                sbomGoComponentDTO.setPrimaryLanguage(component.getLanguage());
                sbomGoComponentDTO.setOrigin(component.getType());
                sbomGoComponentDTO.setDirectDependency(direct);
                sbomGoComponentDTO.setDescription(component.getDescription());
                sbomGoComponentDTO.setPUrl(component.getPUrl());
                sbomGoComponentDTO.setLicenses(Arrays.asList(component.getLicenses()));
                sbomGoComponentDTO.setWebsite(component.getUrl());
                sbomGoComponentDTO.setDownloadUrl(component.getDownloadUrl());
                sbomGoComponentDTO.setRepoUrl(component.getSourceUrl());
                return sbomGoComponentDTO;
            }
            case "javaScript": {
                JsComponentDO component = jsComponentDao.findByNameAndVersion(cName, cVersion);

                SbomJsComponentDTO sbomJsComponentDTO = new SbomJsComponentDTO();
                sbomJsComponentDTO.setNamespace(component.getName().split("/")[0]);
                sbomJsComponentDTO.setArtifactId(component.getName().split("/")[component.getName().split("/").length-1]);
                sbomJsComponentDTO.setVersion(component.getVersion());
                sbomJsComponentDTO.setPrimaryLanguage(component.getLanguage());
                sbomJsComponentDTO.setOrigin(component.getType());
                sbomJsComponentDTO.setDirectDependency(direct);
                sbomJsComponentDTO.setDescription(component.getDescription());
                sbomJsComponentDTO.setPUrl(component.getPurl());
                sbomJsComponentDTO.setLicenses(Arrays.asList(component.getLicenses()));
                sbomJsComponentDTO.setWebsite(component.getWebsite());
                sbomJsComponentDTO.setDownloadUrl(component.getDownloadUrl());
                sbomJsComponentDTO.setRepoUrl(component.getRepoUrl());
                sbomJsComponentDTO.setCopyrightStatements(component.getCopyrightStatements());

            }
            case "python": {
                PythonComponentDO component = pythonComponentDao.findByNameAndVersion(cName, cVersion);

                SbomPythonComponentDTO sbomPythonComponentDTO = new SbomPythonComponentDTO();
                sbomPythonComponentDTO.setName(component.getName());
                sbomPythonComponentDTO.setVersion(component.getVersion());
                sbomPythonComponentDTO.setLanguage(component.getLanguage());
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
        }
        return null;
    }


    /**
     * 导出app的SBOM
     * @param response HttpServletResponse
     * @param applicationSearchDTO ApplicationSearchDTO
     */
    public void exportSBOMOld(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response) {
/*        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        if (applicationDO == null) {
            throw new PlatformException(500, "应用不存在: name= " + applicationSearchDTO.getName() + " ;version= " + applicationSearchDTO.getVersion());
        }
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());

        JavaDependencyTreeDO javaDependencyTreeDO = javaDependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
        List<SbomComponentDTO> sbomComponentDTOs = new ArrayList<>();
        if (javaDependencyTreeDO == null) {
            // 如果应用没有生成依赖树
            // 先调用ApplicationService里的generateDependencyTree方法，
            javaDependencyTreeDO = applicationService.generateDependencyTree(applicationDO, applicationDO.getType());
            // todo 多语言应用
            //if (applicationDO.getLanguage().equals("java")){
                // 根据依赖树生成依赖表
                List<JavaDependencyTableDO> javaDependencyTableDOS = mavenService.dependencyTableAnalysis(javaDependencyTreeDO);
                for (JavaDependencyTableDO javaDependencyTableDO : javaDependencyTableDOS){
                    // 查找依赖树中的每个组件
                    JavaComponentDO javaComponentDO = javaComponentDao.findByGroupIdAndArtifactIdAndVersion(javaDependencyTableDO.getCGroupId(), javaDependencyTableDO.getCArtifactId(), javaDependencyTableDO.getCVersion());
                    // 将componentDO转换为sbomComponentDTO
                    SbomComponentDTO sbomComponentDTO = new SbomComponentDTO();
                    BeanUtils.copyProperties(javaComponentDO, sbomComponentDTO);
                    sbomComponentDTO.setExternalReferences(
                            Stream.of(
                                            new SbomExternalReferenceDTO("website", javaComponentDO.getUrl()),
                                            new SbomExternalReferenceDTO("distribution", javaComponentDO.getDownloadUrl()),
                                            new SbomExternalReferenceDTO("sourceUrl", javaComponentDO.getSourceUrl())
                                    )
                                    .filter(dto -> dto.getUrl() != null) // 过滤掉为空的外部链接
                                    .collect(Collectors.toList())
                    );
                    sbomComponentDTOs.add(sbomComponentDTO);
                }
            //}
        } else {
            // 如果应用已生成了依赖树
            List<ComponentTableDTO> componentTableDTOS = javaDependencyTableDao.findDependenciesByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
            for (ComponentTableDTO componentTableDTO : componentTableDTOS) {
                // 查找依赖树中的每个组件
                JavaComponentDO javaComponentDO = javaComponentDao.findByGroupIdAndArtifactIdAndVersion(componentTableDTO.getCGroupId(), componentTableDTO.getCArtifactId(), componentTableDTO.getCVersion());
                // 将componentDO转换为sbomComponentDTO
                SbomComponentDTO sbomComponentDTO = new SbomComponentDTO();
                BeanUtils.copyProperties(javaComponentDO, sbomComponentDTO);
                sbomComponentDTO.setExternalReferences(
                        Stream.of(
                                        new SbomExternalReferenceDTO("website", javaComponentDO.getUrl()),
                                        new SbomExternalReferenceDTO("distribution", javaComponentDO.getDownloadUrl()),
                                        new SbomExternalReferenceDTO("sourceUrl", javaComponentDO.getSourceUrl())
                                )
                                .filter(dto -> dto.getUrl() != null) // 过滤掉为空的外部链接
                                .collect(Collectors.toList())
                );
                sbomComponentDTOs.add(sbomComponentDTO);
            }
        }
        // 设置sbomApplicationDTO信息
        SbomApplicationDTO sbomApplicationDTO = new SbomApplicationDTO();
        sbomApplicationDTO.setGroupId(buDO.getName());
        sbomApplicationDTO.setArtifactId(applicationDO.getName());
        sbomApplicationDTO.setVersion(applicationDO.getVersion());
        sbomApplicationDTO.setComponents(sbomComponentDTOs);

        // 创建临时json文件
        String sbomFileName = "sbom-" + applicationDO.getName() + ".json";
        File sbomFile = new File(sbomFileName);
        try {
            // 将sbomApplicationDTO变为json
            ObjectMapper objectMapper = new ObjectMapper();
            String json = objectMapper.writeValueAsString(sbomApplicationDTO);

            // 将 SBOM JSON 写入临时文件
            FileWriter writer = new FileWriter(sbomFile);
            writer.write(json);
            writer.close();

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + sbomFileName);
            try (InputStream inputStream = new FileInputStream(sbomFile);
                 OutputStream outputStream = response.getOutputStream()) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
	outputStream.flush();
            }
        } catch (IOException e) {
            throw new PlatformException(500, "SBOM导出失败");
        } finally {
            // 删除临时文件
            sbomFile.delete();
        }*/
    }
}
