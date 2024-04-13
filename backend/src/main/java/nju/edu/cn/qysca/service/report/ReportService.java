package nju.edu.cn.qysca.service.report;

import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.ApplicationSearchDTO;
import nju.edu.cn.qysca.domain.component.dos.ComponentDO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface ReportService {
    /**
     * 导出应用html报告
     *
     * @param applicationSearchDTO 应用搜索信息
     * @param response HttpServletResponse
     */
    void exportHtml(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response);
}
