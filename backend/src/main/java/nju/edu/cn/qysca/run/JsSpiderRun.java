package nju.edu.cn.qysca.run;

import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.dao.component.JsComponentDao;
import nju.edu.cn.qysca.dao.spider.NpmVisitedPackagesDao;
import nju.edu.cn.qysca.domain.component.dos.JsComponentDO;
import nju.edu.cn.qysca.domain.spider.dos.NpmVisitedPackagesDO;
import nju.edu.cn.qysca.service.spider.JsSpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class JsSpiderRun {
    @Autowired
    private JsComponentDao jsComponentDao;
    @Autowired
    private JsSpiderService jsSpiderService;
    @Autowired
    private NpmVisitedPackagesDao npmVisitedPackagesDao;

    public void executeSpiderTask(){
        List<NpmVisitedPackagesDO> npmVisitedPackagesDOList = npmVisitedPackagesDao.findAll();
        Collections.shuffle(npmVisitedPackagesDOList);
        for (NpmVisitedPackagesDO npmVisitedPackagesDO : npmVisitedPackagesDOList) {
            synchronized (this) {
                // 如果该包被成功访问过，则跳过
                if (npmVisitedPackagesDO.getVisited() && npmVisitedPackagesDO.getIsSuccess())
                    continue;

                // 爬取组件信息
                if (npmVisitedPackagesDO.getVersion().equals("-")) {
                    boolean isSuccess = true;
                    String name = npmVisitedPackagesDO.getName();
                    // 获取所有版本
                    List<String> versions = jsSpiderService.getVersions(name);
                    if (null == versions) {
                        isSuccess = false;
                    } else {
                        for (String version : versions) {
                            // 爬取每个版本
                            if (jsComponentDao.findByNameAndVersion(name, version) != null) {
                                continue;
                            }
                            JsComponentDO jsComponentDO = jsSpiderService.crawlByNV(name, version);
                            if (jsComponentDO == null) {
                                isSuccess = false;
                                continue;
                            }
                            try {
                                jsComponentDao.save(jsComponentDO);
                            } catch (Exception e){
                                log.error("组件存入数据库失败：" + jsComponentDO.toString());
                                e.printStackTrace();
                            }
                        }
                    }

                    npmVisitedPackagesDO.setVisited(true);
                    npmVisitedPackagesDO.setIsSuccess(isSuccess);
                    npmVisitedPackagesDao.deleteById(npmVisitedPackagesDO.getId());
                    npmVisitedPackagesDao.save(npmVisitedPackagesDO);
                }

            }
        }
    }
}
