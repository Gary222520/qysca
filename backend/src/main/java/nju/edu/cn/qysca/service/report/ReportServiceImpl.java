package nju.edu.cn.qysca.service.report;


import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTreeDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.component.GoComponentDao;
import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.dao.component.JsComponentDao;
import nju.edu.cn.qysca.dao.component.PythonComponentDao;
import nju.edu.cn.qysca.dao.license.LicenseDao;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.ApplicationSearchDTO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseConflictInfoDTO;
import nju.edu.cn.qysca.domain.vulnerability.dtos.VulnerabilityBriefDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.license.LicenseService;
import nju.edu.cn.qysca.service.vulnerability.VulnerabilityService;
import nju.edu.cn.qysca.utils.FolderUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private ApplicationDao applicationDao;
    @Autowired
    private AppDependencyTreeDao appDependencyTreeDao;
    @Autowired
    private AppDependencyTableDao appDependencyTableDao;
    @Autowired
    private JavaComponentDao javaComponentDao;
    @Autowired
    private GoComponentDao goComponentDao;
    @Autowired
    private JsComponentDao jsComponentDao;
    @Autowired
    private PythonComponentDao pythonComponentDao;
    @Autowired
    private LicenseDao licenseDao;
    @Autowired
    private LicenseService licenseService;
    @Autowired
    private VulnerabilityService vulnerabilityService;
    @Value("${tempReportFolder}")
    private String tempFolder;

    /**
     * 导出应用html报告
     *
     * @param applicationSearchDTO 应用搜索信息
     * @param response HttpServletResponse
     */
    @Override
    public void exportHtml(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response){
        // 生成临时文件夹
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);
        File tempDir = new File(tempFolder, timeStamp);
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        try {
            String filePath = makeHtml(tempDir, applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
            export(response, new File(filePath));
        } catch (PlatformException e){
            throw e;
        } catch (IOException e){
            throw new PlatformException(500, "导出html报告失败");
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
     * 在指定文件夹下生成应用的html报告
     *
     * @param dir 指定文件夹
     * @param appName 应用名称
     * @param appVersion 应用版本
     * @return filePath 临时html报告的地址
     */
    public String makeHtml(File dir, String appName, String appVersion) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(appName, appVersion);

        if (applicationDO == null){
            throw new PlatformException(500, "应用不存在！");
        }

        if (applicationDO.getChildApplication().length != 0 || !applicationDO.getChildComponent().isEmpty()) {
            throw new PlatformException(500, "暂时只能导出底层应用的html报告");
        }

        // 准备数据
        List<ComponentDO> componentDOList = new ArrayList<>();
        List<AppDependencyTableDO> appDependencyTableDOList = appDependencyTableDao.findAllByNameAndVersion(appName, appVersion);
        for (AppDependencyTableDO appDependencyTableDO : appDependencyTableDOList) {
            switch (appDependencyTableDO.getLanguage()) {
                case "java":
                    componentDOList.add(javaComponentDao.findByNameAndVersion(appDependencyTableDO.getCName(), appDependencyTableDO.getCVersion()));
                    break;
                case "golang":
                    componentDOList.add(goComponentDao.findByNameAndVersion(appDependencyTableDO.getCName(), appDependencyTableDO.getCVersion()));
                    break;
                case "javaScript":
                    componentDOList.add(jsComponentDao.findByNameAndVersion(appDependencyTableDO.getCName(), appDependencyTableDO.getCVersion()));
                    break;
                case "python":
                    componentDOList.add(pythonComponentDao.findByNameAndVersion(appDependencyTableDO.getCName(), appDependencyTableDO.getCVersion()));
                    break;
            }
        }

        List<LicenseDO> licenseDOList = new ArrayList<>();
        for (String licenseName : applicationDO.getLicenses()) {
            LicenseDO licenseDO = licenseDao.findByNameIgnoreCase(licenseName);
            if (licenseDO != null)
                licenseDOList.add(licenseDO);
        }

        LicenseConflictInfoDTO licenseConflictInfoDTO = licenseService.getLicenseConflictInformation(appName, appVersion);

        List<VulnerabilityBriefDTO> vulnerabilityBriefDTOList = vulnerabilityService.getApplicationVulnerabilityList(appName, appVersion);

        // 生成html文件
        String filePath = makeHtml(dir, "report-template", applicationDO, componentDOList, licenseDOList, licenseConflictInfoDTO, vulnerabilityBriefDTOList);
        return filePath;
    }

    /**
     * 在指定文件夹生成html报告
     *
     * @param dir 指定文件夹
     * @param template 模板名称
     * @param applicationDO ApplicationDO
     * @param componentDOList  List<ComponentDO>
     * @param licenseDOList List<LicenseDO>
     * @param vulnerabilityBriefDTOList  List<VulnerabilityBriefDTO>
     * @return html报告地址
     */
    private String makeHtml(File dir, String template, ApplicationDO applicationDO, List<ComponentDO> componentDOList, List<LicenseDO> licenseDOList, LicenseConflictInfoDTO licenseConflictInfoDTO, List<VulnerabilityBriefDTO>  vulnerabilityBriefDTOList) {
        Context context = new Context();
        context.setVariable("application", applicationDO);
        context.setVariable("components", componentDOList);
        context.setVariable("licenses", licenseDOList);
        context.setVariable("licensesConflict", licenseConflictInfoDTO);
        context.setVariable("vulnerabilities", vulnerabilityBriefDTOList);

        File file = new File(dir, applicationDO.getName().replaceAll(":","-") + "-report.html");
        try (FileWriter writer = new FileWriter(file)) {
            templateEngine.process(template, context, writer);
            System.out.println("HTML report exported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new PlatformException(500, "导出html文件失败");
        }
        return file.getPath();
    }


//    public void exportPdf(String template, ApplicationDO applicationDO, List<ComponentDO> componentDOList) {
//        // 先生成html文件，再转换成pdf
//        exportHtml(template, applicationDO, componentDOList);
//
//        String inputFile = tempFolder + applicationDO.getName() + "-report.html"; // 输入的HTML文件路径
//        String outputFile = tempFolder + applicationDO.getName() + "-report.pdf"; // 输出的PDF文件路径
//
//        // 使用 iText 将 HTML 转换为 PDF
//        try (FileInputStream htmlFileStream = new FileInputStream(inputFile);
//             FileOutputStream pdfFileStream = new FileOutputStream(outputFile);) {
//            HtmlConverter.convertToPdf(htmlFileStream, pdfFileStream);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new PlatformException(500, "导出pdf文件失败");
//        }
//
//        // 使用该库的效果似乎不太好
////        // 使用 xhtmlrenderer 将 HTML 转换为 PDF
////        ITextRenderer renderer = new ITextRenderer();
////        renderer.setDocument(inputFile);
////        renderer.layout();
////        try (OutputStream os = new FileOutputStream(outputFile)) {
////            renderer.createPDF(os);
////        } catch (IOException | DocumentException e){
////            e.printStackTrace();
////            throw new PlatformException(500, "导出pdf文件失败");
////        }
//
//    }

}
