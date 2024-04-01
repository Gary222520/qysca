package nju.edu.cn.qysca.service.sbom;

import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.bu.BuAppDao;
import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.dao.component.JavaDependencyTableDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.component.JavaDependencyTreeDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.ApplicationSearchDTO;
import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.domain.component.dos.JavaComponentDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.JavaDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.sbom.SbomComponentDTO;
import nju.edu.cn.qysca.domain.sbom.SbomExternalReferenceDTO;
import nju.edu.cn.qysca.domain.sbom.SbomApplicationDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.application.ApplicationService;
import nju.edu.cn.qysca.service.maven.MavenService;
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
    private ApplicationService applicationService;

    @Autowired
    private MavenService mavenService;

    @Autowired
    private JavaDependencyTableDao javaDependencyTableDao;

    @Autowired
    private JavaDependencyTreeDao javaDependencyTreeDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private BuAppDao buAppDao;

    @Autowired
    private BuDao buDao;

    @Autowired
    private JavaComponentDao javaComponentDao;


    /**
     * 导出app的SBOM
     * @param response HttpServletResponse
     * @param applicationSearchDTO ApplicationSearchDTO
     */
    public void exportSBOM(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response) {
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
