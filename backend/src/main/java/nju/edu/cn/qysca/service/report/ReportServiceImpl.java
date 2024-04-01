package nju.edu.cn.qysca.service.report;


import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.dao.component.PythonComponentDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import nju.edu.cn.qysca.exception.PlatformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import com.itextpdf.html2pdf.HtmlConverter;
import org.xhtmlrenderer.pdf.ITextRenderer;
import com.lowagie.text.DocumentException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService{

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private JavaComponentDao javaComponentDao;

    @Autowired
    private PythonComponentDao pythonComponentDao;

    @Value("${tempReportFolder}")
    private String reportFolder;

    public void exportHtml(String applicationId){
        ApplicationDO applicationDO = applicationDao.findOneById(applicationId);
        if (applicationDO.getLanguage().equals("java")){
            List<ComponentDO> componentDOList = new ArrayList<>();
        }

    }

    @Override
    public void exportHtml(String template, ApplicationDO applicationDO, List<ComponentDO> componentDOList){
        Context context = new Context();
        context.setVariable("application", applicationDO);
        context.setVariable("components", componentDOList);
        File tempFolder= new File(reportFolder);
        if (!tempFolder.exists()){
            tempFolder.mkdirs();
        }
        String tempOutputPath = reportFolder + applicationDO.getName() + "-report.html";
        try (FileWriter writer = new FileWriter(tempOutputPath)) {
            templateEngine.process(template, context, writer);
            System.out.println("HTML report exported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new PlatformException(500, "导出html文件失败");
        }
    }

    @Override
    public void exportPdf(String template, ApplicationDO applicationDO, List<ComponentDO> componentDOList){
        // 先生成html文件，再转换成pdf
        exportHtml(template, applicationDO, componentDOList);

        String inputFile = reportFolder + applicationDO.getName() + "-report.html"; // 输入的HTML文件路径
        String outputFile = reportFolder + applicationDO.getName() + "-report.pdf"; // 输出的PDF文件路径

        // 使用 iText 将 HTML 转换为 PDF
        try ( FileInputStream htmlFileStream = new FileInputStream(inputFile);
              FileOutputStream pdfFileStream = new FileOutputStream(outputFile);){
            HtmlConverter.convertToPdf(htmlFileStream, pdfFileStream);
        } catch (IOException e){
            e.printStackTrace();
            throw new PlatformException(500, "导出pdf文件失败");
        }

        // 使用该库的效果似乎不太好
//        // 使用 xhtmlrenderer 将 HTML 转换为 PDF
//        ITextRenderer renderer = new ITextRenderer();
//        renderer.setDocument(inputFile);
//        renderer.layout();
//        try (OutputStream os = new FileOutputStream(outputFile)) {
//            renderer.createPDF(os);
//        } catch (IOException | DocumentException e){
//            e.printStackTrace();
//            throw new PlatformException(500, "导出pdf文件失败");
//        }

    }

}
