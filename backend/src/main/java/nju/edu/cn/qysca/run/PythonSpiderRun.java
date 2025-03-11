package nju.edu.cn.qysca.run;

import lombok.extern.slf4j.Slf4j;
import nju.edu.cn.qysca.dao.component.PythonComponentDao;
import nju.edu.cn.qysca.dao.component.PythonDependencyTableDao;
import nju.edu.cn.qysca.dao.component.PythonDependencyTreeDao;
import nju.edu.cn.qysca.dao.spider.PythonVisitedPackagesDao;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import nju.edu.cn.qysca.domain.spider.dos.PythonVisitedPackagesDO;
import nju.edu.cn.qysca.service.python.PythonService;
import nju.edu.cn.qysca.service.spider.PythonSpiderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class PythonSpiderRun {

    @Autowired
    private PythonComponentDao pythonComponentDao;
    @Autowired
    private PythonSpiderService pythonSpiderService;
    @Autowired
    private PythonVisitedPackagesDao pythonVisitedPackagesDao;
    @Autowired
    private PythonDependencyTreeDao pythonDependencyTreeDao;
    @Autowired
    private PythonDependencyTableDao pythonDependencyTableDao;
    @Autowired
    private PythonService pythonService;

    public void executeSpiderTask() {
        List<PythonVisitedPackagesDO> packagesDOList = pythonVisitedPackagesDao.findAll();
        for (PythonVisitedPackagesDO pythonVisitedPackagesDO : packagesDOList) {
            synchronized (this) {
                // 如果该包被成功访问过，则跳过
                if (pythonVisitedPackagesDO.getVisited() && pythonVisitedPackagesDO.getIsSuccess())
                    continue;

                // 爬取组件信息
                if (pythonVisitedPackagesDO.getVersion().equals("-")) {
                    boolean isSuccess = true;
                    String name = pythonVisitedPackagesDO.getName();
                    // 获取所有版本
                    List<String> versions = pythonSpiderService.getVersions(name);
                    if (null == versions) {
                        isSuccess = false;
                    } else {
                        for (String version : versions) {
                            // 爬取每个版本
                            if (pythonComponentDao.findByNameAndVersion(name, version) != null) {
                                continue;
                            }
                            log.info("开始爬取: " + "python组件 " + name + ":" + version);
                            PythonComponentDO pythonComponentDO = pythonSpiderService.crawlByNV(name, version);
                            if (pythonComponentDO == null) {
                                isSuccess = false;
                                continue;
                            }
                            try {
                                pythonComponentDao.save(pythonComponentDO);
                            } catch (Exception e) {
                                log.error("python组件存入数据库失败：" + pythonComponentDO.toString());
                                e.printStackTrace();
                            }

                        }
                    }

                    pythonVisitedPackagesDO.setVisited(true);
                    pythonVisitedPackagesDO.setIsSuccess(isSuccess);
                    pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
                    pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
                }


            }
        }
    }


    /**
     * 批量爬取python包（还会构造并存储该组件的依赖树、依赖表，并爬取该组件的所以依赖）
     * （暂时废弃, 无法使用）
     */
    public void executePythonSpiderWithDependencyTask(){
//        List<PythonVisitedPackagesDO> packagesDOList = pythonVisitedPackagesDao.findAll();
//        for (PythonVisitedPackagesDO pythonVisitedPackagesDO : packagesDOList){
//            synchronized (this) {
//                // 如果该包被成功访问过，则跳过
//                if (pythonVisitedPackagesDO.getVisited() && pythonVisitedPackagesDO.getIsSuccess())
//                    continue;
//
//                // 爬取组件信息
//                PythonComponentDO pythonComponentDO = pythonSpiderService.crawlByNV(pythonVisitedPackagesDO.getName(), pythonVisitedPackagesDO.getVersion());
//                if (pythonComponentDO == null) {
//                    pythonVisitedPackagesDO.setVisited(true);
//                    pythonVisitedPackagesDO.setIsSuccess(false);
//                    pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
//                    pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
//                    continue;
//                }
//
//                if (pythonComponentDao.findByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion()) == null)
//                    pythonComponentDao.save(pythonComponentDO);
//
//                // 检查依赖树是否存在，存在则跳过
//                if (pythonDependencyTreeDao.findByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion()) != null){
//                    pythonVisitedPackagesDO.setVisited(true);
//                    pythonVisitedPackagesDO.setIsSuccess(true);
//                    pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
//                    pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
//                    continue;
//                }
//
//                // 生成依赖树和依赖表
//                PythonDependencyTreeDO pythonDependencyTreeDO = pythonService.spiderDependency(pythonComponentDO.getName(), pythonComponentDO.getVersion());
//                // 生成失败则标记
//                if (pythonDependencyTreeDO == null) {
//                    pythonVisitedPackagesDO.setVisited(true);
//                    pythonVisitedPackagesDO.setIsSuccess(false);
//                    pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
//                    pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
//                    continue;
//                }
//                List<PythonDependencyTableDO> pythonDependencyTableDOList = pythonService.dependencyTableAnalysis(pythonDependencyTreeDO);
//
//                try {
//                    pythonDependencyTreeDao.save(pythonDependencyTreeDO);
//                    pythonDependencyTableDao.saveAll(pythonDependencyTableDOList);
//                } catch (Exception e){
//                    System.err.println("插入数据时出错：" + pythonComponentDO.getName() + ":" + pythonComponentDO.getVersion());
//                    e.printStackTrace();
//                }
//
//
//                // 标记成功爬取过
//                pythonVisitedPackagesDO.setVisited(true);
//                pythonVisitedPackagesDO.setIsSuccess(true);
//                pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
//                pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
//                System.out.println("成功爬取：" + pythonComponentDO.getName() + ":" + pythonComponentDO.getVersion());
//            }
//        }
    }
}
