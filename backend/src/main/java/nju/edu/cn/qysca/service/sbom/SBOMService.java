package nju.edu.cn.qysca.service.sbom;

import nju.edu.cn.qysca.domain.application.dtos.ApplicationSearchDTO;

import javax.servlet.http.HttpServletResponse;

public interface SBOMService {
    void exportSBOM(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response);
}
