package nju.edu.cn.qysca.service.report;

import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.component.JavaComponentDao;
import nju.edu.cn.qysca.dao.component.PythonComponentDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.FileWriter;
import java.io.IOException;
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

    public void exportHtml(String applicationId){
        ApplicationDO applicationDO = applicationDao.findOneById(applicationId);
        if (applicationDO.getLanguage().equals("java")){
            List<ComponentDO> componentDOList = new ArrayList<>();
        }

    }

    @Override
    public void exportHtml(ApplicationDO applicationDO, List<ComponentDO> componentDOList){
        Context context = new Context();
        context.setVariable("application", applicationDO);
        context.setVariable("components", componentDOList);

        String tempOutputPath = applicationDO.getName() + "-report.html";
        try (FileWriter writer = new FileWriter(tempOutputPath)) {
            templateEngine.process("reportTemplate", context, writer);
            System.out.println("HTML report exported successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
