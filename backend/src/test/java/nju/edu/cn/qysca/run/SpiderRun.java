package nju.edu.cn.qysca.run;

import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.dao.spider.NpmVisitedPackagesDao;
import nju.edu.cn.qysca.dao.spider.PythonVisitedPackagesDao;
import nju.edu.cn.qysca.domain.application.dos.AppDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.spider.dos.NpmVisitedPackagesDO;
import nju.edu.cn.qysca.domain.spider.dos.PythonVisitedPackagesDO;
import nju.edu.cn.qysca.service.python.PythonService;
import nju.edu.cn.qysca.service.spider.GoSpiderService;
import nju.edu.cn.qysca.service.spider.JavaSpiderService;
import nju.edu.cn.qysca.service.spider.JsSpiderService;
import nju.edu.cn.qysca.service.spider.PythonSpiderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 批量爬取入口
 */
@SpringBootTest
public class SpiderRun {
    @Autowired
    private AppDependencyTableDao appDependencyTableDao;
    @Autowired
    private JavaSpiderService javaSpiderService;
    @Autowired
    private PythonSpiderService pythonSpiderService;
    @Autowired
    private JsSpiderService jsSpiderService;
    @Autowired
    private GoSpiderService goSpiderService;
    @Autowired
    private PythonVisitedPackagesDao pythonVisitedPackagesDao;
    @Autowired
    private NpmVisitedPackagesDao npmVisitedPackagesDao;
    @Autowired
    private JavaComponentDao javaComponentDao;
    @Autowired
    private JsComponentDao jsComponentDao;
    @Autowired
    private PythonService pythonService;
    @Autowired
    private PythonComponentDao pythonComponentDao;
    @Autowired
    private PythonDependencyTreeDao pythonDependencyTreeDao;
    @Autowired
    private PythonDependencyTableDao pythonDependencyTableDao;
    @Value("${targetUrlsPath}")
    private String TARGET_URLS_PATH;


    /**
     * 批量爬取java组件
     */
    @Test
    public void executeJavaSpiderTask(){
        List<String> targetUrls = new ArrayList<>();

        // 从target_urls.txt文件中读取目标URL
        try (InputStream inputStream = new FileInputStream(TARGET_URLS_PATH);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = reader.readLine()) != null) {
                targetUrls.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 开始爬取
        for (String targetUrl : targetUrls) {
            if (targetUrl.isEmpty() || targetUrl.startsWith("//"))
                continue;
            System.out.println("开始按目录爬取：" + targetUrl);
            javaSpiderService.crawlDirectory(targetUrl);
            System.out.println("该目录爬取完毕: " + targetUrl);
        }
    }


    /**
     * 批量爬取python包
     */
    @Test
    public void executePythonSpiderTask(){
        List<PythonVisitedPackagesDO> packagesDOList = pythonVisitedPackagesDao.findAll();
        for (PythonVisitedPackagesDO pythonVisitedPackagesDO : packagesDOList){
            synchronized (this) {
                // 如果该包被成功访问过，则跳过
                if (pythonVisitedPackagesDO.getVisited() && pythonVisitedPackagesDO.getIsSuccess())
                    continue;

                // 爬取组件信息
                if (pythonVisitedPackagesDO.getVersion().equals("-")){
                    boolean isSuccess = true;
                    String name = pythonVisitedPackagesDO.getName();
                    // 获取所有版本
                    List<String> versions = pythonSpiderService.getVersions(name);
                    if (null == versions){
                        isSuccess = false;
                    } else {
                        for (String version : versions) {
                            // 爬取每个版本
                            if (pythonComponentDao.findByNameAndVersion(name, version) != null) {
                                continue;
                            }
                            PythonComponentDO pythonComponentDO = pythonSpiderService.crawlByNV(name, version);
                            if (pythonComponentDO == null) {
                                isSuccess = false;
                                continue;
                            }
                            pythonComponentDao.save(pythonComponentDO);

                            Date date = new Date();
                            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                            String timeStamp = dateFormat.format(date);
                            System.out.println(timeStamp + " 成功爬取：" + pythonComponentDO.getName() + ":" + pythonComponentDO.getVersion());
                        }
                    }

                }


            }
        }
    }


    /**
     * 批量爬取js组件
     */
    @Test
    public void executeJsSpiderTask(){
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
                            jsComponentDao.save(jsComponentDO);

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


    /**
     * 批量爬取go组件
     */
    @Test
    public void executeGoSpiderTask(){
        // todo
    }


//    /**
//     * 批量爬取java组件（还会构造并存储该组件的依赖树、依赖表，并爬取该组件的所以依赖）
//     */
//    @Test
//    public void executeJavaSpiderWithDependencyTask(){
//        List<String> targetUrls = new ArrayList<>();
//
//        // 从target_urls.txt文件中读取目标URL
//        try (InputStream inputStream = new FileInputStream(TARGET_URLS_PATH);
//             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
//            String line;
//            while ((line = reader.readLine()) != null) {
//                targetUrls.add(line);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        // 开始爬取
//        for (String targetUrl : targetUrls) {
//            if (targetUrl.isEmpty() || targetUrl.startsWith("//"))
//                continue;
//            System.out.println("开始按目录爬取：" + targetUrl);
//            javaSpiderService.crawlDirectoryWithDependency(targetUrl);
//            System.out.println("该目录爬取完毕: " + targetUrl);
//        }
//    }


//    /**
//     * 批量爬取python包（还会构造并存储该组件的依赖树、依赖表，并爬取该组件的所以依赖）
//     */
//    @Test
//    public void executePythonSpiderWithDependencyTask(){
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
//    }

//    @Test
//    public void tempFix(){
//        // 用来爬缺失的组件的，请勿调用
//        List<AppDependencyTableDO> appDependencyTableDOList = appDependencyTableDao.findAllByNameAndVersion("bu:java-app","1.0.0");
//        for (AppDependencyTableDO tableDO : appDependencyTableDOList){
//            if (javaComponentDao.findByNameAndVersion(tableDO.getCName(), tableDO.getCVersion()) == null){
//                JavaComponentDO javaComponentDO = javaSpiderService.crawlByGav(tableDO.getCName().split(":")[0], tableDO.getCName().split(":")[1], tableDO.getCVersion());
//                javaComponentDao.save(javaComponentDO);
//            }
//        }
//    }
}
