package nju.edu.cn.qysca.service.sbom;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.application.ApplicationProjectDao;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.dao.component.DependencyTableDao;
import nju.edu.cn.qysca.dao.project.ProjectDao;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import nju.edu.cn.qysca.domain.sbom.SbomComponentDTO;
import nju.edu.cn.qysca.domain.sbom.SbomExternalReferenceDTO;
import nju.edu.cn.qysca.domain.sbom.SbomProjectDTO;
import nju.edu.cn.qysca.utils.ZipUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SBOMServiceImpl implements SBOMService {


    @Autowired
    private ApplicationProjectDao applicationProjectDao;

    @Autowired
    private DependencyTableDao dependencyTableDao;

    @Autowired
    private ProjectDao projectDao;

    @Autowired
    private ComponentDao componentDao;


    /**
     * app 导出SBOM
     * @param response HttpServletResponse
     * @param applicationGroupId 应用组织id
     * @param applicationArtifactId 应用工件id
     * @param applicationVersion 应用版本号
     */
    public void exportSBOM(HttpServletResponse response, String applicationGroupId, String applicationArtifactId, String applicationVersion){
        // 找到application的所有project，一个project为一个json文件
        List<ProjectDO> projectDOList = applicationProjectDao.findProjectByApplicationGAV(applicationGroupId, applicationArtifactId, applicationVersion);

        // 创建临时SBOM文件夹
        File sbomFolder = new File("SBOM");
        if (!sbomFolder.exists()) {
            sbomFolder.mkdirs();
        }

        for (ProjectDO projectDO : projectDOList){
            // 找到project所有的直接依赖组件
            List<ComponentTableDTO> componentTableDTOS = dependencyTableDao.findDirectDependenciesByGroupIdAndArtifactIdAndVersion(projectDO.getGroupId(), projectDO.getArtifactId(), projectDO.getVersion());

            List<SbomComponentDTO> sbomComponentDTOs = new ArrayList<>();
            for (ComponentTableDTO componentTableDTO : componentTableDTOS){
                ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(componentTableDTO.getCGroupId(), componentTableDTO.getCArtifactId(), componentTableDTO.getCVersion());
                // 将componentDO转换为sbomComponentDTO
                SbomComponentDTO sbomComponentDTO = new SbomComponentDTO();
                BeanUtils.copyProperties(componentDO, sbomComponentDTO);
                sbomComponentDTO.setPurl(componentDO.getPUrl());
                sbomComponentDTO.setExternalReferences(
                        Stream.of(
                                        new SbomExternalReferenceDTO("website", componentDO.getUrl()),
                                        new SbomExternalReferenceDTO("distribution", componentDO.getDownloadUrl()),
                                        new SbomExternalReferenceDTO("sourceUrl", componentDO.getSourceUrl())
                                )
                                .filter(dto -> dto.getUrl() != null) // 过滤掉为空的外部链接
                                .collect(Collectors.toList())
                );
                sbomComponentDTOs.add(sbomComponentDTO);
            }

            // 封装为SbomProjectDTO
            SbomProjectDTO sbomProjectDTO = new SbomProjectDTO();
            sbomProjectDTO.setGroupId(projectDO.getGroupId());
            sbomProjectDTO.setArtifactId(projectDO.getArtifactId());
            sbomProjectDTO.setVersion(projectDO.getVersion());
            sbomProjectDTO.setComponents(sbomComponentDTOs);

            String json = null;
            ObjectMapper objectMapper = new ObjectMapper();
            try{
                // 将sbomProjectDTO变为json
                json = objectMapper.writeValueAsString(sbomProjectDTO);

                // 创建项目类型对应的子文件夹
                String typeFolderName = projectDO.getType();
                File typeFolder = new File(sbomFolder, typeFolderName);
                if (!typeFolder.exists()) {
                    typeFolder.mkdirs();
                }

                // 将 SBOM JSON 写入临时文件
                String fileName = "sbom-" + projectDO.getArtifactId() + ".json";
                File sbomFile = new File(typeFolder, fileName);
                FileWriter writer = new FileWriter(sbomFile);
                writer.write(json);
                writer.close();
            } catch (IOException e){
                e.printStackTrace();
                return;
            }
        }


        // 将临时文件夹打包为 zip 文件
        String zipFileName = "SBOM.zip";
        File zipFile = new File(zipFileName);
        try {
            ZipUtil.zipDirectory(sbomFolder, zipFile);
            // 设置HTTP响应头
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + zipFileName);

            // 将压缩文件内容写入响应体
            try (InputStream inputStream = new FileInputStream(zipFile)) {
                OutputStream outputStream = response.getOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // 删除临时文件夹及其内容
            ZipUtil.deleteDirectory(sbomFolder);
            ZipUtil.deleteDirectory(zipFile);
        }

    }
}
