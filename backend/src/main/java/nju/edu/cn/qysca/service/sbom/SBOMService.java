package nju.edu.cn.qysca.service.sbom;

import javax.servlet.http.HttpServletResponse;

public interface SBOMService {
    void exportSBOM(HttpServletResponse response, String applicationGroupId, String applicationArtifactId, String applicationVersion);
}
