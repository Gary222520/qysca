package nju.edu.cn.qysca.run;

import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.dao.component.GoComponentDao;
import nju.edu.cn.qysca.dao.component.GoDependencyTableDao;
import nju.edu.cn.qysca.dao.component.GoDependencyTreeDao;
import nju.edu.cn.qysca.dao.spider.GoVisitedPackagesDao;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDO;
import nju.edu.cn.qysca.domain.component.dos.GoComponentDependencyTreeDO;
import nju.edu.cn.qysca.domain.component.dos.GoDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.GoDependencyTreeDO;
import nju.edu.cn.qysca.domain.spider.dos.GoVisitedPackagesDO;
import nju.edu.cn.qysca.service.go.GoService;
import nju.edu.cn.qysca.service.spider.GoSpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class GoSpiderRun {

    @Autowired
    private GoSpiderService goSpiderService;
    @Autowired
    private GoService goService;
    @Autowired
    private GoComponentDao goComponentDao;
    @Autowired
    private GoDependencyTreeDao goDependencyTreeDao;
    @Autowired
    private GoDependencyTableDao goDependencyTableDao;
    @Autowired
    private GoVisitedPackagesDao goVisitedPackagesDao;

    public void executeSpiderTask(){
        List<GoVisitedPackagesDO> packagesDOList = goVisitedPackagesDao.findAll();
        for (GoVisitedPackagesDO goVisitedPackagesDO : packagesDOList) {
            synchronized (this) {
                // 如果该包被成功访问过，则跳过
                if (goVisitedPackagesDO.getVisited() && goVisitedPackagesDO.getIsSuccess())
                    continue;

                // 爬取组件信息
                if (goVisitedPackagesDO.getVersion().equals("-")) {
                    boolean isSuccess = true;
                    String name = goVisitedPackagesDO.getName();
                    // 获取所有版本
                    List<String> versions = goSpiderService.getVersions(name);
                    if (null == versions) {
                        isSuccess = false;
                    } else {
                        for (String version : versions) {
                            // 爬取每个版本
                            if (goComponentDao.findByNameAndVersion(name, version) != null) {
                                continue;
                            }
                            GoComponentDO goComponentDO = goSpiderService.crawlByNV(name, version);
                            if (goComponentDO == null) {
                                isSuccess = false;
                                continue;
                            }
                            try {
                                goComponentDao.save(goComponentDO);
                            } catch (Exception e){
                                log.error("保存组件时出错：" + goComponentDO.getName() + " " + goComponentDO.getVersion());
                                log.error(e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }

                    goVisitedPackagesDO.setVisited(true);
                    goVisitedPackagesDO.setIsSuccess(isSuccess);
                    goVisitedPackagesDao.deleteById(goVisitedPackagesDO.getId());
                    goVisitedPackagesDao.save(goVisitedPackagesDO);
                }


            }
        }
    }

    public void executeSpiderTaskWithDependency(){
//        List<GoVisitedPackagesDO> packagesDOList = goVisitedPackagesDao.findAll();
//        for (GoVisitedPackagesDO goVisitedPackagesDO : packagesDOList) {
//            synchronized (this) {
//                // 如果该包被成功访问过，则跳过
//                if (goVisitedPackagesDO.getVisited() && goVisitedPackagesDO.getIsSuccess())
//                    continue;
//
//                // 爬取组件信息
//                if (goVisitedPackagesDO.getVersion().equals("-")) {
//                    boolean isSuccess = true;
//                    String name = goVisitedPackagesDO.getName();
//                    // 获取所有版本
//                    List<String> versions = goSpiderService.getVersions(name);
//                    if (null == versions) {
//                        isSuccess = false;
//                    } else {
//                        for (String version : versions) {
//                            // 爬取每个版本
//                            if (goDependencyTreeDao.findByNameAndVersion(name, version) != null) {
//                                continue;
//                            }
//                            GoComponentDO goComponentDO = goSpiderService.crawlByNV(name, version);
//                            if (goComponentDO == null) {
//                                isSuccess = false;
//                                continue;
//                            }
//
//                            try {
//                                if (goComponentDao.findByNameAndVersion(name, version) == null)
//                                    goComponentDao.save(goComponentDO);
//                            } catch (Exception e){
//                                isSuccess = false;
//                                log.error("保存组件时出错：" + goComponentDO.getName() + " " + goComponentDO.getVersion());
//                                log.error(e.getMessage());
//                                e.printStackTrace();
//                                continue;
//                            }
//
//                            GoDependencyTreeDO goDependencyTreeDO = goService.spiderDependency(name, version);
//                            if (goDependencyTreeDO == null){
//                                isSuccess = false;
//                                continue;
//                            }
//                            List<GoDependencyTableDO> goDependencyTableDOList = goService.dependencyTableAnalysis(goDependencyTreeDO);
//
//                            goDependencyTreeDao.save(goDependencyTreeDO);
//                            goDependencyTableDao.saveAll(goDependencyTableDOList);
//
//                        }
//                    }
//
//                    goVisitedPackagesDO.setVisited(true);
//                    goVisitedPackagesDO.setIsSuccess(isSuccess);
//                    goVisitedPackagesDao.deleteById(goVisitedPackagesDO.getId());
//                    goVisitedPackagesDao.save(goVisitedPackagesDO);
//                }
//
//
//            }
//        }
    }
}
