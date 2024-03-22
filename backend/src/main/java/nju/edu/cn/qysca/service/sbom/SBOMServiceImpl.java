package nju.edu.cn.qysca.service.sbom;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.bu.BuAppDao;
import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.dao.component.DependencyTableDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.sbom.SbomComponentDTO;
import nju.edu.cn.qysca.domain.sbom.SbomExternalReferenceDTO;
import nju.edu.cn.qysca.domain.sbom.SbomApplicationDTO;
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
    private DependencyTableDao dependencyTableDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private BuAppDao buAppDao;

    @Autowired
    private BuDao buDao;

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
        // 找到application的所有application，一个application为一个json文件
        List<ApplicationDO> applicationDOList = null;

        // 创建临时SBOM文件夹
        File sbomFolder = new File("SBOM");
        if (!sbomFolder.exists()) {
            sbomFolder.mkdirs();
        }

        for (ApplicationDO applicationDO : applicationDOList){
            // 找到application所有的直接依赖组件
            BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
            BuDO buDO = buDao.findByBid(buAppDO.getBid());
            List<ComponentTableDTO> componentTableDTOS = dependencyTableDao.findDirectDependenciesByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());

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

            // 封装为SbomapplicationDTO
            SbomApplicationDTO sbomApplicationDTO = new SbomApplicationDTO();
            sbomApplicationDTO.setGroupId(buDO.getName());
            sbomApplicationDTO.setArtifactId(applicationDO.getName());
            sbomApplicationDTO.setVersion(applicationDO.getVersion());
            sbomApplicationDTO.setComponents(sbomComponentDTOs);

            String json = null;
            ObjectMapper objectMapper = new ObjectMapper();
            try{
                // 将sbomapplicationDTO变为json
                json = objectMapper.writeValueAsString(sbomApplicationDTO);

                // 创建应用类型对应的子文件夹
                String typeFolderName = applicationDO.getType();
                File typeFolder = new File(sbomFolder, typeFolderName);
                if (!typeFolder.exists()) {
                    typeFolder.mkdirs();
                }

                // 将 SBOM JSON 写入临时文件
                String fileName = "sbom-" + applicationDO.getName() + ".json";
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
