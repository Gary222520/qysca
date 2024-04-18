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

        // 当1000个爬完后，进行下一步
        executeSpiderTaskWithDependency();
    }

    /**
     * 为数据库中的所有go组件生成依赖树和依赖表，并对这个过程中新爬取的go组件同样生成依赖树和依赖表，直到表中所有go组件都有了依赖树
     */
    public void executeSpiderTaskWithDependency(){
        boolean finished = false;
        while (!finished) {
            finished = true;
            List<GoComponentDO> goComponentDOList = goComponentDao.findAll();
            for (GoComponentDO goComponentDO : goComponentDOList) {
                synchronized (this) {
                    String name = goComponentDO.getName();
                    String version = goComponentDO.getVersion();
                    GoDependencyTreeDO goDependencyTreeDO = goDependencyTreeDao.findByNameAndVersion(name, version);
                    if (null == goDependencyTreeDO) {
                        // 只要有组件没有生成依赖树，则不停止该过程
                        finished = false;
                        // 生成组件依赖树和依赖表
                        try {
                            goDependencyTreeDO = goService.spiderDependency(name, version);
                        } catch (Exception e) {
                            goDependencyTreeDO = null;
                            log.error("组件生成依赖树失败: " + name + ":" + version);
                        }

                        if (null != goDependencyTreeDO) {
                            log.info("组件生成依赖树和依赖表: " + name + ":" + version);
                            List<GoDependencyTableDO> goDependencyTableDOList = goService.dependencyTableAnalysis(goDependencyTreeDO);
                            goDependencyTreeDao.save(goDependencyTreeDO);
                            goDependencyTableDao.saveAll(goDependencyTableDOList);
                        }
                    }
                }
            }
        }
        log.info("所有go组件都以及生成了依赖树，爬取过程结束。");
    }
}
