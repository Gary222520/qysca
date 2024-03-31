package nju.edu.cn.qysca.service.report;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;

import java.util.List;

public interface ReportService {
    void exportHtml(ApplicationDO applicationDO, List<ComponentDO> componentDOList);
}
