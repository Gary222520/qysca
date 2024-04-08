package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.component.PythonComponentDao;
import nju.edu.cn.qysca.dao.component.PythonDependencyTableDao;
import nju.edu.cn.qysca.dao.component.PythonDependencyTreeDao;
import nju.edu.cn.qysca.dao.spider.PythonVisitedPackagesDao;
import nju.edu.cn.qysca.domain.component.dos.PythonComponentDO;
import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTableDO;
import nju.edu.cn.qysca.domain.component.dos.PythonDependencyTreeDO;
import nju.edu.cn.qysca.domain.spider.dos.PythonVisitedPackagesDO;
import nju.edu.cn.qysca.service.python.PythonService;
import nju.edu.cn.qysca.service.spider.JavaSpiderService;
import nju.edu.cn.qysca.service.spider.PythonSpiderService;
import org.hibernate.procedure.spi.ParameterRegistrationImplementor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 批量爬取入口
 * 在需要调用的方法前添加@PostConstruct标签
 * 一次只添加一个@PostConstruct
 */
@SpringBootApplication
public class SpiderRunApplication {

    @Autowired
    private JavaSpiderService javaSpiderService;
    @Autowired
    private PythonSpiderService pythonSpiderService;
    @Autowired
    private PythonVisitedPackagesDao pythonVisitedPackagesDao;
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

    public static void main(String[] args) {
        SpringApplication.run(SpiderRunApplication.class);
    }

    /**
     * 批量爬取java组件
     */
    //@PostConstruct
    private void executeJavaSpiderTask(){
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
            javaSpiderService.crawlDirectoryWithDependency(targetUrl);
            System.out.println("该目录爬取完毕: " + targetUrl);
        }
    }

    /**
     * 批量爬取python包
     */
    @PostConstruct
    private void executePythonSpiderTask(){
        List<PythonVisitedPackagesDO> packagesDOList = pythonVisitedPackagesDao.findAll();
        for (PythonVisitedPackagesDO pythonVisitedPackagesDO : packagesDOList){
            synchronized (this) {
                // 如果该包被成功访问过，则跳过
                if (pythonVisitedPackagesDO.getVisited() && pythonVisitedPackagesDO.getIsSuccess())
                    continue;

                // 爬取组件信息
                PythonComponentDO pythonComponentDO = pythonSpiderService.crawlByNV(pythonVisitedPackagesDO.getName(), pythonVisitedPackagesDO.getVersion());
                if (pythonComponentDO == null) {
                    pythonVisitedPackagesDO.setVisited(true);
                    pythonVisitedPackagesDO.setIsSuccess(false);
                    pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
                    pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
                    continue;
                }

                if (pythonComponentDao.findByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion()) == null)
                    pythonComponentDao.save(pythonComponentDO);

                // 检查依赖树是否存在，存在则跳过
                if (pythonDependencyTreeDao.findByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion()) != null){
                    pythonVisitedPackagesDO.setVisited(true);
                    pythonVisitedPackagesDO.setIsSuccess(true);
                    pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
                    pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
                    continue;
                }

                // 生成依赖树和依赖表
                PythonDependencyTreeDO pythonDependencyTreeDO = pythonService.spiderDependency(pythonComponentDO.getName(), pythonComponentDO.getVersion());
                // 生成失败则标记
                if (pythonDependencyTreeDO == null) {
                    pythonVisitedPackagesDO.setVisited(true);
                    pythonVisitedPackagesDO.setIsSuccess(false);
                    pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
                    pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
                    continue;
                }
                List<PythonDependencyTableDO> pythonDependencyTableDOList = pythonService.dependencyTableAnalysis(pythonDependencyTreeDO);

                try {
                    pythonDependencyTreeDao.save(pythonDependencyTreeDO);
                    pythonDependencyTableDao.saveAll(pythonDependencyTableDOList);
                } catch (Exception e){
                    System.err.println("插入数据时出错：" + pythonComponentDO.getName() + ":" + pythonComponentDO.getVersion());
                    e.printStackTrace();
                }


                // 标记成功爬取过
                pythonVisitedPackagesDO.setVisited(true);
                pythonVisitedPackagesDO.setIsSuccess(true);
                pythonVisitedPackagesDao.deleteById(pythonVisitedPackagesDO.getId());
                pythonVisitedPackagesDao.save(pythonVisitedPackagesDO);
                System.out.println("成功爬取：" + pythonComponentDO.getName() + ":" + pythonComponentDO.getVersion());
            }
        }


    }


}
