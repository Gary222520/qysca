package nju.edu.cn.qysca.service.sbom;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import nju.edu.cn.qysca.dao.application.ApplicationProjectDao;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.dao.component.DependencyTableDao;
import nju.edu.cn.qysca.dao.project.ProjectDao;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.project.dos.ProjectDO;
import nju.edu.cn.qysca.domain.project.dtos.SBOMDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

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


    public void exportSBOM(String applicationGroupId, String applicationArtifactId, String applicationVersion){
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

            List<ComponentDO> componentDOList = new ArrayList<>();
            for (ComponentTableDTO componentTableDTO : componentTableDTOS){
                ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(componentTableDTO.getCGroupId(), componentTableDTO.getCArtifactId(), componentTableDTO.getCVersion());
                componentDOList.add(componentDO);
            }

            // 封装为SBOMDTO
            SBOMDTO sbomDTO = new SBOMDTO();
            sbomDTO.setGroupId(projectDO.getGroupId());
            sbomDTO.setArtifactId(projectDO.getArtifactId());
            sbomDTO.setVersion(projectDO.getVersion());
            sbomDTO.setComponents(componentDOList);

            String json = null;
            ObjectMapper objectMapper = new ObjectMapper();
            try{
                // 将sbomDTO变为json
                json = objectMapper.writeValueAsString(sbomDTO);

                // 将 SBOM JSON 写入临时文件
                String fileName = "sbom-" + projectDO.getName() + ".json";
                File sbomFile = new File(sbomFolder, fileName);

                FileWriter writer = new FileWriter(sbomFile);
                writer.write(json);
            } catch (IOException e){
                e.printStackTrace();
                return;
            }
        }

        // 将临时文件夹打包为 zip 文件
        String zipFileName = "sbom.zip";
        File zipFile = new File(zipFileName);
        //ZipUtil.zipDirectory(sbomFolder, zipFile);

    }
}
