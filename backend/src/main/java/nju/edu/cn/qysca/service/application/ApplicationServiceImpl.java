package nju.edu.cn.qysca.service.application;

import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.dao.application.AppComponentDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTreeDao;
import nju.edu.cn.qysca.dao.bu.BuAppDao;
import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.license.LicenseDao;
import nju.edu.cn.qysca.dao.user.UserRoleDao;
import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.ComponentCompareTreeDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentDetailDTO;
import nju.edu.cn.qysca.domain.application.dos.*;
import nju.edu.cn.qysca.domain.application.dtos.*;
import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
import nju.edu.cn.qysca.domain.user.dtos.UserBriefDTO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.go.GoService;
import nju.edu.cn.qysca.service.gradle.GradleService;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.service.npm.NpmService;
import nju.edu.cn.qysca.service.python.PythonService;
import nju.edu.cn.qysca.utils.excel.ExcelUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private BuDao buDao;

    @Autowired
    private BuAppDao buAppDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private JavaDependencyTreeDao javaDependencyTreeDao;

    @Autowired
    private JsDependencyTreeDao jsDependencyTreeDao;

    @Autowired
    private GoDependencyTreeDao goDependencyTreeDao;

    @Autowired
    private PythonDependencyTreeDao pythonDependencyTreeDao;

    @Autowired
    private AppDependencyTreeDao appDependencyTreeDao;

    @Autowired
    private AppDependencyTableDao appDependencyTableDao;

    @Autowired
    private JavaComponentDao javaComponentDao;

    @Autowired
    private JsComponentDao jsComponentDao;

    @Autowired
    private GoComponentDao goComponentDao;

    @Autowired
    private PythonComponentDao pythonComponentDao;

    @Autowired
    private AppComponentDao appComponentDao;

    @Autowired
    private LicenseDao licenseDao;

    @Autowired
    private MavenService mavenService;

    @Autowired
    private NpmService npmService;

    @Autowired
    private GoService goService;

    @Autowired
    private GradleService gradleService;

    @Autowired
    private PythonService pythonService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${tempPomFolder}")
    private String tempFolder;


    /**
     * 分页获取应用信息
     *
     * @param number 页码
     * @param size   页大小
     * @return Page<ApplicationDO> 应用信息分页结果
     */
    @Override
    public Page<ApplicationDO> findApplicationPage(int number, int size) {
        UserDO user = ContextUtil.getUserDO();
        Pageable pageable = PageRequest.of(number - 1, size);
        String bid = userRoleDao.findUserBu(user.getUid());
        return applicationDao.findApplicationPage(bid, pageable);
    }


    /**
     * 模糊查询应用名称
     *
     * @param name 应用名称
     * @return List<String> 模糊查询应用名称列表
     */
    @Override
    public List<String> searchApplicationName(String name) {
        UserDO user = ContextUtil.getUserDO();
        String bid = userRoleDao.findUserBu(user.getUid());
        return applicationDao.searchApplicationName(bid, name);
    }

    /**
     * 根据名称查询应用 并返回应用的最新版本
     *
     * @param name 应用名称
     * @return ApplicationDO 应用信息
     */
    @Override
    public ApplicationDO findApplication(String name) {
        UserDO user = ContextUtil.getUserDO();
        String bid = userRoleDao.findUserBu(user.getUid());
        return applicationDao.findApplication(bid, name);
    }


    /**
     * 根据应用信息返回子应用信息
     *
     * @param name    应用名称
     * @param version 版本
     * @return SubApplicationDTO 子应用信息
     */
    @Override
    public SubApplicationDTO findSubApplication(String name, String version) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(name, version);
        List<ApplicationDO> subApplication = applicationDao.findSubApplication(applicationDO.getId());
        //查询子组件
        Map<String, List<? extends ComponentDO>> subComponent = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : applicationDO.getChildComponent().entrySet()) {
            switch (entry.getKey()) {
                case "java":
                    List<JavaComponentDO> javaComponentDOList = javaComponentDao.findByIdIn(entry.getValue());
                    subComponent.put("java", javaComponentDOList);
                    break;
                case "javaScript":
                    List<JsComponentDO> jsComponentDOList = jsComponentDao.findByIdIn(entry.getValue());
                    subComponent.put("javaScript", jsComponentDOList);
                    break;
                case "golang":
                    List<GoComponentDO> goComponentDOList = goComponentDao.findByIdIn(entry.getValue());
                    subComponent.put("golang", goComponentDOList);
                    break;
                case "python":
                    List<PythonComponentDO> pythonComponentDOList = pythonComponentDao.findByIdIn(entry.getValue());
                    subComponent.put("python", pythonComponentDOList);
                    break;
            }
        }
        SubApplicationDTO subApplicationDTO = new SubApplicationDTO();
        subApplicationDTO.setSubApplication(subApplication);
        subApplicationDTO.setSubComponent(subComponent);
        return subApplicationDTO;
    }

    /**
     * 新增/更新应用信息
     *
     * @param saveApplicationDTO 保存应用接口信息
     * @return Boolean 新增应用是否成功
     */
    @Override
    @Transactional
    public Boolean saveApplication(SaveApplicationDTO saveApplicationDTO) {
        ApplicationDO applicationDO = null;
        UserDO user = ContextUtil.getUserDO();
        BuDO buDO = buDao.findBuDOByName(saveApplicationDTO.getBuName());
        if (buDO == null) {
            throw new PlatformException(500, "该部门不存在");
        }
        if (StringUtils.isEmpty(saveApplicationDTO.getId())) {
            applicationDO = new ApplicationDO();
            BeanUtils.copyProperties(saveApplicationDTO, applicationDO);
            applicationDO.setCreator(user.getUid());
            applicationDO.setState("CREATED");
            applicationDO.setLock(false);
            applicationDO.setRelease(false);
        } else {
            applicationDO = applicationDao.findByNameAndVersion(saveApplicationDTO.getName(), saveApplicationDTO.getVersion());
            applicationDO.setDescription(saveApplicationDTO.getDescription());
            applicationDO.setType(saveApplicationDTO.getType());
        }
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        applicationDO.setTime(timeStamp);
        applicationDao.save(applicationDO);
        //在部门应用表中增加信息
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        if (buAppDO == null) {
            buAppDO = new BuAppDO();
            buAppDO.setBid(buDO.getBid());
            buAppDO.setAid(applicationDO.getId());
            buAppDao.save(buAppDO);
        }
        return true;
    }


    /**
     * 在保存应用依赖时将应用状态改为RUNNING
     *
     * @param name    应用名称
     * @param version 版本
     */
    @Override
    @Transactional
    public void changeApplicationState(String name, String version) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(name, version);
        applicationDO.setState("RUNNING");
        applicationDao.save(applicationDO);
    }

    /**
     * 保存应用依赖关系
     *
     * @param saveApplicationDependencyDTO 保存应用依赖接口信息
     */
    @Async("taskExecutor")
    @Override
    @Transactional
    public void saveApplicationDependency(SaveApplicationDependencyDTO saveApplicationDependencyDTO) {
        try {
            //非叶子节点不能保存pom信息
            ApplicationDO applicationDO = applicationDao.findByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
            if (applicationDO.getChildApplication().length > 0 || applicationDO.getChildComponent().size() > 0) {
                throw new PlatformException(500, "该应用不能保存pom信息");
            }
            AppDependencyTreeDO appDependencyTreeDO = null;
            switch (saveApplicationDependencyDTO.getLanguage()) {
                case "java":
                    applicationDO.setLanguage(new String[]{"java"});
                    JavaComponentDO analyzedJavaComponentDO = null;
                    JavaDependencyTreeDO analyzedJavaDependencyTreeDO = null;
                    if (saveApplicationDependencyDTO.getBuilder().equals("gradle")) {
                        analyzedJavaDependencyTreeDO = gradleService.dependencyTreeAnalysis(saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder(), saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion(), "");
                    } else {
                        analyzedJavaComponentDO = mavenService.componentAnalysis(saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder(), "");
                        if (!analyzedJavaComponentDO.getName().equals(saveApplicationDependencyDTO.getName()) || !analyzedJavaComponentDO.getVersion().equals(saveApplicationDependencyDTO.getVersion())) {
                            throw new PlatformException(500, "上传文件非本项目");
                        }
                        analyzedJavaDependencyTreeDO = mavenService.dependencyTreeAnalysis(saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder(), "");
                    }
                    appDependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(analyzedJavaDependencyTreeDO.getName(), analyzedJavaDependencyTreeDO.getVersion());
                    if (appDependencyTreeDO == null) {
                        appDependencyTreeDO = new AppDependencyTreeDO();
                        appDependencyTreeDO.setName(analyzedJavaDependencyTreeDO.getName());
                        appDependencyTreeDO.setVersion(saveApplicationDependencyDTO.getVersion());
                    }
                    appDependencyTreeDO.setTree(mavenService.translateComponentDependency(analyzedJavaDependencyTreeDO.getTree()));
                    appDependencyTreeDao.save(appDependencyTreeDO);
                    appDependencyTableDao.deleteAllByNameAndVersion(appDependencyTreeDO.getName(), appDependencyTreeDO.getVersion());
                    List<JavaDependencyTableDO> applicationJavaDependencyTableDOS = mavenService.dependencyTableAnalysis(analyzedJavaDependencyTreeDO);
                    appDependencyTableDao.saveAll(mavenService.translateDependencyTable(applicationJavaDependencyTableDOS));
                    break;
                case "javaScript":
                    applicationDO.setLanguage(new String[] {"javaScript"});
                    JsDependencyTreeDO analyzedJsDependencyTreeDO = npmService.dependencyTreeAnalysis(saveApplicationDependencyDTO.getFilePath(), "");
                    if (!analyzedJsDependencyTreeDO.getName().equals(saveApplicationDependencyDTO.getName()) || !analyzedJsDependencyTreeDO.getVersion().equals(saveApplicationDependencyDTO.getVersion())) {
                        throw new PlatformException(500, "上传文件非本项目");
                    }
                    appDependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
                    if (appDependencyTreeDO == null) {
                        appDependencyTreeDO = new AppDependencyTreeDO();
                        appDependencyTreeDO.setName(saveApplicationDependencyDTO.getName());
                        appDependencyTreeDO.setVersion(saveApplicationDependencyDTO.getVersion());
                    }
                    appDependencyTreeDO.setTree(npmService.translateComponentDependencyTree(analyzedJsDependencyTreeDO.getTree()));
                    appDependencyTreeDao.save(appDependencyTreeDO);
                    appDependencyTableDao.deleteAllByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
                    List<JsDependencyTableDO> applicationJsDependencyTableDOS = npmService.dependencyTableAnalysis(analyzedJsDependencyTreeDO);
                    appDependencyTableDao.saveAll(npmService.translateDependencyTable(applicationJsDependencyTableDOS));
                    break;
                case "golang":
                    applicationDO.setLanguage(new String[]{"golang"});
                    GoDependencyTreeDO analyzedGoDependencyTreeDO = goService.dependencyTreeAnalysis(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion(), "", saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder());
                    //GO从实现来看不需要检查
                    appDependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
                    if (appDependencyTreeDO == null) {
                        appDependencyTreeDO = new AppDependencyTreeDO();
                        appDependencyTreeDO.setName(saveApplicationDependencyDTO.getName());
                        appDependencyTreeDO.setVersion(saveApplicationDependencyDTO.getVersion());
                    }
                    appDependencyTreeDO.setTree(goService.translateComponentDependency(analyzedGoDependencyTreeDO.getTree()));
                    appDependencyTreeDao.save(appDependencyTreeDO);
                    appDependencyTableDao.deleteAllByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
                    List<GoDependencyTableDO> applicationGoDependencyTableDOS = goService.dependencyTableAnalysis(analyzedGoDependencyTreeDO);
                    appDependencyTableDao.saveAll(goService.translateDependencyTable(applicationGoDependencyTableDOS));
                    break;
                case "python":
                    applicationDO.setLanguage(new String[]{"python"});
                    PythonDependencyTreeDO analyzedPythonDependencyTreeDO = pythonService.dependencyTreeAnalysis(saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder(), saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion(), "");
                    //Python从实现上来看不需要检查
                    appDependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
                    if (appDependencyTreeDO == null) {
                        appDependencyTreeDO = new AppDependencyTreeDO();
                        appDependencyTreeDO.setName(saveApplicationDependencyDTO.getName());
                        appDependencyTreeDO.setVersion(saveApplicationDependencyDTO.getVersion());
                    }
                    appDependencyTreeDO.setTree(pythonService.translateComponentDependency(analyzedPythonDependencyTreeDO.getTree()));
                    appDependencyTreeDao.save(appDependencyTreeDO);
                    appDependencyTableDao.deleteAllByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
                    List<PythonDependencyTableDO> applicationPythonDependencyTableDOS = pythonService.dependencyTableAnalysis(analyzedPythonDependencyTreeDO);
                    appDependencyTableDao.saveAll(pythonService.translateDependencyTable(applicationPythonDependencyTableDOS));
                    break;
            }
            // 更改状态为SUCCESS
            applicationDO.setBuilder(saveApplicationDependencyDTO.getBuilder());
            applicationDO.setScanner(saveApplicationDependencyDTO.getScanner());
            applicationDO.setLicenses(getUniqueLicenseNames(applicationDO.getName(), applicationDO.getVersion()));
            applicationDO.setVulnerabilities(getUniqueVulnerabilityNames(applicationDO.getName(), applicationDO.getVersion()));
            applicationDO.setState("SUCCESS");
            applicationDao.save(applicationDO);
            File file = new File(saveApplicationDependencyDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(saveApplicationDependencyDTO.getFilePath().substring(0, saveApplicationDependencyDTO.getFilePath().lastIndexOf("/")));
        } catch (Exception e) {
            ApplicationDO applicationDO = applicationDao.findByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
            applicationDO.setState("FAILED");
            applicationDao.save(applicationDO);
            File file = new File(saveApplicationDependencyDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(saveApplicationDependencyDTO.getFilePath().substring(0, saveApplicationDependencyDTO.getFilePath().lastIndexOf("/")));
        }
    }

    /**
     * 升级应用
     *
     * @param upgradeApplicationDTO 升级应用接口信息
     * @return 升级应用是否成功
     */
    @Override
    @Transactional
    public Boolean upgradeApplication(UpgradeApplicationDTO upgradeApplicationDTO) {
        //保存新应用
        ApplicationDO oldApplicationDO = applicationDao.findByNameAndVersion(upgradeApplicationDTO.getName(), upgradeApplicationDTO.getOldVersion());
        ApplicationDO newApplicationDO = new ApplicationDO();
        BeanUtils.copyProperties(oldApplicationDO, newApplicationDO);
        newApplicationDO.setId(null);
        newApplicationDO.setVersion(upgradeApplicationDTO.getNewVersion());
        newApplicationDO.setDescription(upgradeApplicationDTO.getDescription());
        newApplicationDO.setCreator(upgradeApplicationDTO.getCreator());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        newApplicationDO.setTime(timeStamp);
        //升级后的项目为CREATED状态
        newApplicationDO.setState("CREATED");
        newApplicationDO.setLock(false);
        newApplicationDO.setRelease(false);
        applicationDao.save(newApplicationDO);
        BuAppDO newBuAppDO = new BuAppDO();
        BuAppDO oldBuAppDO = buAppDao.findByAid(oldApplicationDO.getId());
        newBuAppDO.setBid(oldBuAppDO.getBid());
        newBuAppDO.setAid(newApplicationDO.getId());
        buAppDao.save(newBuAppDO);
        //copy 角色表
        List<UserRoleDO> userRoleDOS = userRoleDao.findAllByAid(oldApplicationDO.getId());
        List<UserRoleDO> newUserRoleDOS = new ArrayList<>();
        for (UserRoleDO userRoleDO : userRoleDOS) {
            UserRoleDO newUserRoleDO = new UserRoleDO();
            BeanUtils.copyProperties(userRoleDO, newUserRoleDO);
            newUserRoleDO.setId(null);
            newUserRoleDO.setAid(newApplicationDO.getId());
            newUserRoleDOS.add(newUserRoleDO);
        }
        userRoleDao.saveAll(newUserRoleDOS);
        //新增新应用关系 删除旧应用关系
        if (!StringUtils.isEmpty(upgradeApplicationDTO.getParentName()) && !StringUtils.isEmpty(upgradeApplicationDTO.getParentVersion())) {
            ApplicationDO parentApplicationDO = applicationDao.findByNameAndVersion(upgradeApplicationDTO.getParentName(), upgradeApplicationDTO.getParentVersion());
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildApplication()));
            temp.remove(oldApplicationDO.getId());
            temp.add(newApplicationDO.getId());
            parentApplicationDO.setChildApplication(temp.toArray(new String[temp.size()]));
            applicationDao.save(parentApplicationDO);
        }
        return true;
    }


    /**
     * 删除某个应用某个版本
     *
     * @param deleteApplicationDTO 删除应用DTO
     * @return 删除某个应用某个版本是否成功
     */
    @Override
    @Transactional
    public List<ApplicationDO> deleteApplicationVersion(DeleteApplicationDTO deleteApplicationDTO) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(deleteApplicationDTO.getName(), deleteApplicationDTO.getVersion());
        //如果该项目已被锁定无法删除
        if (applicationDO.getLock()) {
            throw new PlatformException(500, "该项目已被锁定无法删除");
        }
        //如果该项目已被发布则会返回依赖该项目的列表 如果列表为空则删除
        if (applicationDO.getRelease()) {
            List<ApplicationDO> applicationDOList = applicationDao.findParentApplication(applicationDO.getId());
            if (applicationDOList.size() != 0) {
                return applicationDOList;
            }
        }
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        appComponentDao.deleteByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
        appDependencyTreeDao.deleteByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
        appDependencyTableDao.deleteAllByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
        //删除BuApp中信息
        buAppDao.delete(buAppDO);
        //删除UserRole中信息
        userRoleDao.deleteAllByAid(applicationDO.getId());
        applicationDao.delete(applicationDO);
        return null;
    }


    /**
     * 在应用中增加组件
     *
     * @param applicationComponentDTO 应用组件信息接口
     * @return 在应用中增加组件是否成功
     */
    @Override
    @Transactional
    public Boolean saveApplicationComponent(ApplicationComponentDTO applicationComponentDTO) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
        ApplicationDO parentApplicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getParentName(), applicationComponentDTO.getParentVersion());
        //锁定和发布状态不能添加组件
        if (parentApplicationDO.getRelease() || parentApplicationDO.getLock()) {
            throw new PlatformException(500, "该应用已锁定或已发布，无法添加组件");
        }
        //叶子节点无法添加组件
        AppDependencyTreeDO appDependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(applicationComponentDTO.getParentName(), applicationComponentDTO.getParentVersion());
        if (appDependencyTreeDO != null) {
            throw new PlatformException(500, "该应用已添加组件，无法手动添加");
        }
        String[] licenses = null;
        String[] vulnerabilities = null;
        Set<String> language = new HashSet<>(Arrays.asList(parentApplicationDO.getLanguage()));
        //不是应用发布成的组件
        if (applicationDO == null) {
            List<String> childComponent = null;
            switch (applicationComponentDTO.getLanguage()) {
                case "java":
                    JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    licenses = javaComponentDO.getLicenses();
                    vulnerabilities = javaComponentDO.getVulnerabilities();
                    childComponent = parentApplicationDO.getChildComponent().get("java");
                    if (childComponent == null) {
                        childComponent = new ArrayList<>();
                        parentApplicationDO.getChildComponent().put("java", childComponent);
                    }
                    parentApplicationDO.getChildComponent().get("java").add(javaComponentDO.getId());
                    language.add("java");
                    break;
                case "javaScript":
                    JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    licenses = jsComponentDO.getLicenses();
                    vulnerabilities = jsComponentDO.getVulnerabilities();
                    childComponent = parentApplicationDO.getChildComponent().get("javaScript");
                    if (childComponent == null) {
                        childComponent = new ArrayList<>();
                        parentApplicationDO.getChildComponent().put("javaScript", childComponent);
                    }
                    parentApplicationDO.getChildComponent().get("javaScript").add(jsComponentDO.getId());
                    language.add("javaScript");
                    break;
                case "golang":
                    GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    licenses = goComponentDO.getLicenses();
                    vulnerabilities = goComponentDO.getVulnerabilities();
                    childComponent = parentApplicationDO.getChildComponent().get("go");
                    if (childComponent == null) {
                        childComponent = new ArrayList<>();
                        parentApplicationDO.getChildComponent().put("go", childComponent);
                    }
                    parentApplicationDO.getChildComponent().get("go").add(goComponentDO.getId());
                    language.add("golang");
                    break;
                case "python":
                    PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    licenses = pythonComponentDO.getLicenses();
                    vulnerabilities = pythonComponentDO.getVulnerabilities();
                    childComponent = parentApplicationDO.getChildComponent().get("python");
                    if (childComponent == null) {
                        childComponent = new ArrayList<>();
                        new ArrayList<>();
                        parentApplicationDO.getChildComponent().put("python", childComponent);
                    }
                    parentApplicationDO.getChildComponent().get("python").add(pythonComponentDO.getId());
                    language.add("python");
                    break;
            }
        } else {
            language.addAll(Arrays.asList(applicationDO.getLanguage()));
            licenses = applicationDO.getLicenses();
            vulnerabilities = applicationDO.getVulnerabilities();
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildApplication()));
            temp.add(applicationDO.getId());
            parentApplicationDO.setChildApplication(temp.toArray(new String[temp.size()]));
        }
        parentApplicationDO.setLanguage(language.toArray(new String[0]));
        Set<String> licenseSet = new HashSet<>(Arrays.asList(parentApplicationDO.getLicenses()));
        licenseSet.addAll(Arrays.asList(licenses));
        String[] updatedLicenses = licenseSet.toArray(new String[0]);
        parentApplicationDO.setLicenses(updatedLicenses);
        Set<String> vulnerabilitySet = new HashSet<>(Arrays.asList(parentApplicationDO.getVulnerabilities()));
        vulnerabilitySet.addAll(Arrays.asList(vulnerabilities));
        String[] updatedVulnerabilities = vulnerabilitySet.toArray(new String[0]);
        parentApplicationDO.setVulnerabilities(updatedVulnerabilities);
        applicationDao.save(parentApplicationDO);
        return Boolean.TRUE;
    }

    /**
     * 在应用中删除组件
     *
     * @param applicationComponentDTO 应用组件信息接口
     * @return 在应用中删除组件是否成功
     */
    @Override
    @Transactional
    public Boolean deleteApplicationComponent(ApplicationComponentDTO applicationComponentDTO) {
        ApplicationDO parentApplicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getParentName(), applicationComponentDTO.getParentVersion());
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
        //发布和锁定状态禁止删除
        if (parentApplicationDO.getLock() || parentApplicationDO.getRelease()) {
            throw new PlatformException(500, "该应用已发布或锁定，禁止删除");
        }
        //不是应用发布成的组件
        if (applicationDO == null) {
            switch (applicationComponentDTO.getLanguage()) {
                case "java":
                    JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    parentApplicationDO.getChildComponent().get("java").remove(javaComponentDO.getId());
                    break;
                case "javaScript":
                    JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    parentApplicationDO.getChildComponent().get("javaScript").remove(jsComponentDO.getId());
                    break;
                case "golang":
                    GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    parentApplicationDO.getChildComponent().get("go").remove(goComponentDO.getId());
                    break;
                case "python":
                    PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(applicationComponentDTO.getName(), applicationComponentDTO.getVersion());
                    parentApplicationDO.getChildComponent().get("python").remove(pythonComponentDO.getId());
                    break;
            }
        } else {
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildApplication()));
            temp.remove(applicationDO.getId());
            parentApplicationDO.setChildApplication(temp.toArray(new String[temp.size()]));
        }
        Set<String> licenseSet = new HashSet<>();
        Set<String> vulnerabilitySet = new HashSet<>();
        //拿到所有剩余组件的Licenses信息进行更新
        for (String subApplicationId : parentApplicationDO.getChildApplication()) {
            ApplicationDO subApplication = applicationDao.findOneById(subApplicationId);
            licenseSet.addAll(Arrays.asList(subApplication.getLicenses()));
            vulnerabilitySet.addAll(Arrays.asList(subApplication.getVulnerabilities()));
        }
        for (Map.Entry<String, List<String>> entry : parentApplicationDO.getChildComponent().entrySet()) {
            switch (entry.getKey()) {
                case "golang":
                    for (String subComponentId : entry.getValue()) {
                        GoComponentDO goComponentDO = goComponentDao.findGoComponentDOById(subComponentId);
                        licenseSet.addAll(Arrays.asList(goComponentDO.getLicenses()));
                        vulnerabilitySet.addAll(Arrays.asList(goComponentDO.getVulnerabilities()));
                    }
                    break;
                case "python":
                    for (String subComponentId : entry.getValue()) {
                        PythonComponentDO pythonComponentDO = pythonComponentDao.findPythonComponentDOById(subComponentId);
                        licenseSet.addAll(Arrays.asList(pythonComponentDO.getLicenses()));
                        vulnerabilitySet.addAll(Arrays.asList(pythonComponentDO.getVulnerabilities()));
                    }
                    break;
                case "java":
                    for (String subComponentId : entry.getValue()) {
                        JavaComponentDO javaComponentDO = javaComponentDao.findComponentDOById(subComponentId);
                        licenseSet.addAll(Arrays.asList(javaComponentDO.getLicenses()));
                        vulnerabilitySet.addAll(Arrays.asList(javaComponentDO.getVulnerabilities()));
                    }
                    break;
                case "javaScript":
                    for (String subComponentId : entry.getValue()) {
                        JsComponentDO jsComponentDO = jsComponentDao.findJsComponentDOById(subComponentId);
                        licenseSet.addAll(Arrays.asList(jsComponentDO.getLicenses()));
                        vulnerabilitySet.addAll(Arrays.asList(jsComponentDO.getVulnerabilities()));
                    }
                    break;
            }
        }
        parentApplicationDO.setLicenses(licenseSet.toArray(new String[0]));
        parentApplicationDO.setVulnerabilities(licenseSet.toArray(new String[0]));
        applicationDao.save(parentApplicationDO);
        return Boolean.TRUE;
    }

    /**
     * 改变应用的锁定状态
     *
     * @param name    应用名称
     * @param version 版本号
     */
    @Override
    @Transactional
    public void changeLockState(String name, String version) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(name, version);
        if (applicationDO.getLock()) {
            applicationDO.setLock(false);
        } else {
            applicationDO.setLock(true);
        }
        applicationDao.save(applicationDO);
    }

    /**
     * 改变应用的发布状态
     *
     * @param changeReleaseStateDTO 应用发布状态DTO
     */
    @Override
    @Transactional
    public List<ApplicationDO> changeReleaseState(ChangeReleaseStateDTO changeReleaseStateDTO) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(changeReleaseStateDTO.getName(), changeReleaseStateDTO.getVersion());
        if (applicationDO.getRelease()) {
            List<ApplicationDO> parentApplicationDOS = applicationDao.findParentApplication(applicationDO.getId());
            if (!parentApplicationDOS.isEmpty()) {
                return parentApplicationDOS;
            }
            applicationDO.setRelease(false);
            appComponentDao.deleteByNameAndVersion(changeReleaseStateDTO.getName(), changeReleaseStateDTO.getVersion());
            appDependencyTreeDao.deleteByNameAndVersion(changeReleaseStateDTO.getName(), changeReleaseStateDTO.getVersion());
            appDependencyTableDao.deleteAllByNameAndVersion(changeReleaseStateDTO.getName(), changeReleaseStateDTO.getVersion());
        } else {
            //发布应用成组件
            applicationDO.setRelease(true);
            AppComponentDO appComponentDO = new AppComponentDO();
            BeanUtils.copyProperties(applicationDO, appComponentDO);
            appComponentDO.setType(changeReleaseStateDTO.getType());
            Set<String> language = new HashSet<>();
            for (String id : applicationDO.getChildApplication()) {
                ApplicationDO child = applicationDao.findApplicationDOById(id);
                language.addAll(Arrays.asList(child.getLanguage()));
            }
            for (Map.Entry<String, List<String>> entry : applicationDO.getChildComponent().entrySet()) {
                if (entry.getValue().size() > 0) {
                    language.add(entry.getKey());
                }
            }
            appComponentDO.setLicenses(applicationDO.getLicenses());
            appComponentDO.setLanguage(language.toArray(new String[0]));
            appComponentDao.save(appComponentDO);
            AppDependencyTreeDO temp = appDependencyTreeDao.findByNameAndVersion(applicationDO.getName(), applicationDO.getVersion());
            //根据结构生成依赖信息并保存
            if (temp == null) {
                temp = generateDependencyTree(applicationDO, changeReleaseStateDTO.getType());
                appDependencyTreeDao.save(temp);
                List<AppDependencyTableDO> appDependencyTableDOS = dependencyTableAnalysis(temp);
                appDependencyTableDao.saveAll(appDependencyTableDOS);
            }
        }
        applicationDao.save(applicationDO);
        return null;
    }

    /**
     * 依赖树生成依赖平铺表
     *
     * @param appDependencyTreeDO 应用依赖树信息
     * @return List<AppDependencyTableDO> 应用依赖平铺表信息
     */
    private List<AppDependencyTableDO> dependencyTableAnalysis(AppDependencyTreeDO appDependencyTreeDO) {
        List<AppDependencyTableDO> appDependencyTableDOS = new ArrayList<>();
        Queue<AppComponentDependencyTreeDO> queue = new LinkedList<>(appDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            AppDependencyTableDO appDependencyTableDO = new AppDependencyTableDO();
            appDependencyTableDO.setName(appDependencyTreeDO.getName());
            appDependencyTableDO.setVersion(appDependencyTreeDO.getVersion());
            AppComponentDependencyTreeDO componentDependencyTree = queue.poll();
            appDependencyTableDO.setCName(componentDependencyTree.getName());
            appDependencyTableDO.setCVersion(componentDependencyTree.getVersion());
            appDependencyTableDO.setDepth(componentDependencyTree.getDepth());
            appDependencyTableDO.setDirect(componentDependencyTree.getDepth() == 1);
            appDependencyTableDO.setType(componentDependencyTree.getType());
            appDependencyTableDO.setLanguage(componentDependencyTree.getLanguage());
            appDependencyTableDO.setLicenses(componentDependencyTree.getLicenses() == null ? "-" : componentDependencyTree.getLicenses());
            appDependencyTableDO.setVulnerabilities(componentDependencyTree.getVulnerabilities() == null ? "-" : componentDependencyTree.getVulnerabilities());
            queue.addAll(componentDependencyTree.getDependencies());
            appDependencyTableDOS.add(appDependencyTableDO);
        }
        return appDependencyTableDOS;
    }

    /**
     * 根据结构生成依赖信息
     *
     * @param applicationDO 项目信息
     * @param type          组件类型
     * @return JavaDependencyTreeDO 依赖树信息
     */
    public AppDependencyTreeDO generateDependencyTree(ApplicationDO applicationDO, String type) {
        AppDependencyTreeDO appDependencyTreeDO = new AppDependencyTreeDO();
        appDependencyTreeDO.setName(applicationDO.getName());
        appDependencyTreeDO.setVersion(applicationDO.getVersion());
        AppComponentDependencyTreeDO appComponentDependencyTreeDO = new AppComponentDependencyTreeDO();
        BeanUtils.copyProperties(appDependencyTreeDO, appComponentDependencyTreeDO);
        appComponentDependencyTreeDO.setType(type);
        appComponentDependencyTreeDO.setDepth(0);
        List<AppComponentDependencyTreeDO> appComponentDependencyTreeDOS = new ArrayList<>();
        for (String id : applicationDO.getChildApplication()) {
            ApplicationDO tempApplicationDO = applicationDao.findApplicationDOById(id);

            AppComponentDependencyTreeDO temp = appDependencyTreeDao.findByNameAndVersion(tempApplicationDO.getName(), tempApplicationDO.getVersion()).getTree();
            addDepth(temp);
            appComponentDependencyTreeDOS.add(temp);
        }
        for (Map.Entry<String, List<String>> entry : applicationDO.getChildComponent().entrySet()) {
            switch (entry.getKey()) {
                case "java":
                    for (String id : entry.getValue()) {
                        JavaComponentDO tempJavaComponentDO = javaComponentDao.findComponentDOById(id);
                        JavaDependencyTreeDO tempJavaDependencyTreeDO = javaDependencyTreeDao.findByNameAndVersion(tempJavaComponentDO.getName(), tempJavaComponentDO.getVersion());
                        //采用增量更新的原则 如果没有则需爬取并构造
                        if (tempJavaDependencyTreeDO == null) {
                            // 调用爬虫获得pom文件
                            String[] temp = tempJavaComponentDO.getName().split(":");
                            tempJavaDependencyTreeDO = mavenService.spiderDependency(temp[0], temp[1], tempJavaComponentDO.getVersion());
                        }
                        AppComponentDependencyTreeDO temp = mavenService.translateComponentDependency(tempJavaDependencyTreeDO.getTree());
                        addDepth(temp);
                        appComponentDependencyTreeDOS.add(temp);
                    }
                    break;
                case "javaScript":
                    for (String id : entry.getValue()) {
                        JsComponentDO tempJsComponentDO = jsComponentDao.findJsComponentDOById(id);
                        JsDependencyTreeDO tempJsDependencyTreeDO = jsDependencyTreeDao.findByNameAndVersion(tempJsComponentDO.getName(), tempJsComponentDO.getVersion());
                        if (tempJsDependencyTreeDO == null) {
                            tempJsDependencyTreeDO = npmService.spiderDependencyTree(tempJsComponentDO.getName(), tempJsComponentDO.getVersion());
                        }
                        AppComponentDependencyTreeDO temp = npmService.translateComponentDependencyTree(tempJsDependencyTreeDO.getTree());
                        addDepth(temp);
                        appComponentDependencyTreeDOS.add(temp);
                    }
                    break;
                case "golang":
                    for (String id : entry.getValue()) {
                        GoComponentDO tempGoComponentDO = goComponentDao.findGoComponentDOById(id);
                        GoDependencyTreeDO tempGoDependencyTreeDO = goDependencyTreeDao.findByNameAndVersion(tempGoComponentDO.getName(), tempGoComponentDO.getVersion());
                        if (tempGoDependencyTreeDO == null) {
                            tempGoDependencyTreeDO = goService.spiderDependency(tempGoComponentDO.getName(), tempGoComponentDO.getVersion());
                        }
                        AppComponentDependencyTreeDO temp = goService.translateComponentDependency(tempGoDependencyTreeDO.getTree());
                        addDepth(temp);
                        appComponentDependencyTreeDOS.add(temp);
                    }
                    break;
                case "python":
                    for (String id : entry.getValue()) {
                        PythonComponentDO tempPythonComponentDO = pythonComponentDao.findPythonComponentDOById(id);
                        PythonDependencyTreeDO tempPythonDependencyTreeDO = pythonDependencyTreeDao.findByNameAndVersion(tempPythonComponentDO.getName(), tempPythonComponentDO.getVersion());
                        if (tempPythonDependencyTreeDO == null) {
                            tempPythonDependencyTreeDO = pythonService.spiderDependency(tempPythonComponentDO.getName(), tempPythonComponentDO.getVersion());
                        }
                        AppComponentDependencyTreeDO temp = pythonService.translateComponentDependency(tempPythonDependencyTreeDO.getTree());
                        addDepth(temp);
                        appComponentDependencyTreeDOS.add(temp);
                    }
                    break;
            }
        }
        appComponentDependencyTreeDO.setDependencies(appComponentDependencyTreeDOS);
        appDependencyTreeDO.setTree(appComponentDependencyTreeDO);
        return appDependencyTreeDO;
    }

    /**
     * 将树节点层数加1
     */
    private void addDepth(AppComponentDependencyTreeDO appComponentDependencyTreeDO) {
        if (appComponentDependencyTreeDO == null) {
            return;
        }
        appComponentDependencyTreeDO.setDepth(appComponentDependencyTreeDO.getDepth() + 1);
        for (AppComponentDependencyTreeDO temp : appComponentDependencyTreeDO.getDependencies()) {
            addDepth(temp);
        }
    }

    /**
     * 获取指定应用的所有版本列表
     *
     * @param name 应用名称
     * @return List<String> 版本列表
     */
    @Override
    public List<String> getVersionsList(String name) {
        return applicationDao.findVersionsByName(name);
    }

    /**
     * 获取指定应用指定版本的详细信息
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @return ApplicationDO 应用版本的详细信息
     */
    @Override
    public ApplicationDetailDTO findApplicationVersionInfo(ApplicationSearchDTO applicationSearchDTO) {
        ApplicationDetailDTO applicationDetailDTO = new ApplicationDetailDTO();
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        applicationDetailDTO.setApplicationDO(applicationDO);
        List<UserBriefDTO> userBriefDTOS = userRoleDao.listAppMember(applicationDO.getId());
        applicationDetailDTO.setUsers(userBriefDTOS);
        return applicationDetailDTO;
    }

    /**
     * 查询应用依赖树信息
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @return JavaDependencyTreeDO 应用依赖树信息
     */
    @Override
    public AppDependencyTreeDO findApplicationDependencyTree(ApplicationSearchDTO applicationSearchDTO) {
        return appDependencyTreeDao.findByNameAndVersion(
                applicationSearchDTO.getName(),
                applicationSearchDTO.getVersion());
    }

    /**
     * 分页查询应用依赖平铺信息
     *
     * @param applicationSearchPageDTO 带分页应用版本搜索信息
     * @return Page<ComponentTableDTO> 依赖平铺信息分页
     */
    @Override
    public Page<AppComponentTableDTO> findApplicationDependencyTable(ApplicationSearchPageDTO applicationSearchPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(applicationSearchPageDTO.getNumber() - 1, applicationSearchPageDTO.getSize(), Sort.by(orders));
        return appDependencyTableDao.findByNameAndVersion(
                applicationSearchPageDTO.getName(),
                applicationSearchPageDTO.getVersion(), pageable);
    }

    /**
     * 导出应用依赖平铺信息（简明）Excel
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @param response             Http服务响应
     */
    @Override
    public void exportTableExcelBrief(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response) {
        List<TableExcelBriefDTO> resList = appDependencyTableDao.findTableListByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        String fileName = applicationSearchDTO.getName().replace(":","-") + "-" + applicationSearchDTO.getVersion() + "-dependencyTable-brief";
        try {
            ExcelUtils.export(response, fileName, resList, TableExcelBriefDTO.class);
        } catch (Exception e) {
            throw new PlatformException(500, "导出Excel失败");
        }
    }

    /**
     * 导出应用依赖平铺信息（详细）Excel
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @param response             Http服务响应
     */
    @Override
    public void exportTableExcelDetail(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response) {
        List<TableExcelDetailDTO> resList = new ArrayList<>();
        String fileName = applicationSearchDTO.getName().replace(":","-") + "-" + applicationSearchDTO.getVersion() + "-dependencyTable-detail";
        // 先获取依赖平铺的简明信息
        List<TableExcelBriefDTO> briefList = appDependencyTableDao.findTableListByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        for (TableExcelBriefDTO brief : briefList) {
            TableExcelDetailDTO detail = new TableExcelDetailDTO();
            BeanUtils.copyProperties(brief, detail);
            ComponentDetailDTO componentDetailDTO = new ComponentDetailDTO();
            // 获取对应依赖组件的详细信息
            switch (detail.getLanguage()) {
                case "golang":
                    GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(detail.getCName(), detail.getCVersion());
                    BeanUtils.copyProperties(goComponentDO, componentDetailDTO);
                    break;
                case "java":
                    JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(detail.getCName(), detail.getCVersion());
                    BeanUtils.copyProperties(javaComponentDO, componentDetailDTO);
                    break;
                case "python":
                    PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(detail.getCName(), detail.getCVersion());
                    BeanUtils.copyProperties(pythonComponentDO, componentDetailDTO);
                    break;
                case "javaScript":
                    JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(detail.getCName(), detail.getCVersion());
                    BeanUtils.copyProperties(jsComponentDO, componentDetailDTO);
                    break;
            }
            detail.setCName(componentDetailDTO.getName());
            detail.setCVersion(componentDetailDTO.getVersion());
            detail.setDescription(componentDetailDTO.getDescription());
            detail.setUrl(componentDetailDTO.getUrl());
            detail.setDownloadUrl(componentDetailDTO.getDownloadUrl());
            detail.setSourceUrl(componentDetailDTO.getSourceUrl());
            // 拼接许可证信息和开发者信息的字符串
            StringBuilder liName = new StringBuilder();
            StringBuilder liUrl = new StringBuilder();
            StringBuilder devId = new StringBuilder();
            StringBuilder devName = new StringBuilder();
            StringBuilder devEmail = new StringBuilder();
            for (String license : componentDetailDTO.getLicenses()) {
                LicenseDO licenseDO = licenseDao.findByName(license);
                liName.append(licenseDO.getName()).append(";");
                liUrl.append(licenseDO.getUrl()).append(";");
            }
            for (DeveloperDO developerDO : componentDetailDTO.getDevelopers()) {
                devId.append(developerDO.getId()).append(";");
                devName.append(developerDO.getName()).append(";");
                devEmail.append(developerDO.getEmail()).append(";");
            }
            detail.setLicensesName(liName.toString());
            detail.setLicensesUrl(liUrl.toString());
            detail.setDevelopersId(devId.toString());
            detail.setDevelopersName(devName.toString());
            detail.setDevelopersEmail(devEmail.toString());
            resList.add(detail);
        }
        try {
            ExcelUtils.export(response, fileName, resList, TableExcelDetailDTO.class);
        } catch (Exception e) {
            throw new PlatformException(500, "导出Excel失败");
        }
    }

    /**
     * 生成应用版本对比树
     *
     * @param versionCompareReqDTO 需对比的应用版本
     * @return VersionCompareTreeDTO 对比树
     */
    @Override
    public VersionCompareTreeDTO getApplicationVersionCompareTree(VersionCompareReqDTO versionCompareReqDTO) {
        // 获取需对比的两个应用版本依赖树信息
        AppDependencyTreeDO fromDependencyTree = appDependencyTreeDao.findByNameAndVersion(versionCompareReqDTO.getName(), versionCompareReqDTO.getFromVersion());
        if (fromDependencyTree == null) {
            throw new PlatformException(500, "被对比的应用版本依赖树信息不存在");
        }
        AppDependencyTreeDO toDependencyTree = appDependencyTreeDao.findByNameAndVersion(versionCompareReqDTO.getName(), versionCompareReqDTO.getToVersion());
        if (toDependencyTree == null) {
            throw new PlatformException(500, "待对比的应用版本依赖树信息不存在");
        }
        // 打上对比标记，生成对比树
        VersionCompareTreeDTO versionCompareTreeDTO = new VersionCompareTreeDTO();
        BeanUtils.copyProperties(versionCompareReqDTO, versionCompareTreeDTO);
        // 递归处理
        versionCompareTreeDTO.setTree(recursionDealWithChange(
                fromDependencyTree.getTree(), toDependencyTree.getTree()));
        return versionCompareTreeDTO;
    }

    /**
     * 递归处理变更的组件
     *
     * @param from 被对比者
     * @param to   待对比者
     * @return ComponentCompareTreeDTO 对比树
     */
    private ComponentCompareTreeDTO recursionDealWithChange(AppComponentDependencyTreeDO from, AppComponentDependencyTreeDO to) {
        if (from == null || to == null) {
            return null;
        }
        // 变更的组件打上CHANGE标记
        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
        BeanUtils.copyProperties(to, root);
        root.setMark("CHANGE");
        // 分析各子树应属于何种标记
        Map<String, AppComponentDependencyTreeDO> fromMap = new HashMap<>();
        Map<String, AppComponentDependencyTreeDO> toMap = new HashMap<>();
        Set<String> intersection = new HashSet<>();
        for (AppComponentDependencyTreeDO fromChild : from.getDependencies()) {
            fromMap.put(fromChild.getName() + fromChild.getType(), fromChild);
        }
        for (AppComponentDependencyTreeDO toChild : to.getDependencies()) {
            toMap.put(toChild.getName() + toChild.getType(), toChild);
        }
        for (String key : toMap.keySet()) {
            // 求交集
            if (fromMap.containsKey(key)) {
                intersection.add(key);
            }
        }
        // 依次进行分析
        for (AppComponentDependencyTreeDO toChild : to.getDependencies()) {
            String key = toChild.getName() + toChild.getType();
            if (intersection.contains(key)) {
                if (fromMap.get(key).getVersion().equals(toMap.get(key).getVersion())) {
                    root.getDependencies().add(recursionMark(toMap.get(key), "SAME"));
                } else {
                    // CHANGE
                    root.getDependencies().add(recursionDealWithChange(fromMap.get(key), toMap.get(key)));
                }
            } else {
                root.getDependencies().add(recursionMark(toMap.get(key), "ADD"));
            }
        }
        for (AppComponentDependencyTreeDO fromChild : from.getDependencies()) {
            String key = fromChild.getName() + fromChild.getType();
            if (!intersection.contains(key)) {
                root.getDependencies().add(recursionMark(fromMap.get(key), "DELETE"));
            }
        }
        return root;
    }

    /**
     * 递归打上标记
     *
     * @param tree 树状信息
     * @param mark 标记符号（ADD,DELETE,SAME）
     * @return ComponentCompareTreeDTO 对比树
     */
    private ComponentCompareTreeDTO recursionMark(AppComponentDependencyTreeDO tree, String mark) {
        if (tree == null) {
            return null;
        }
        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
        BeanUtils.copyProperties(tree, root);
        root.setMark(mark);
        for (AppComponentDependencyTreeDO child : tree.getDependencies()) {
            ComponentCompareTreeDTO childAns = recursionMark(child, mark);
            if (childAns != null) {
                root.getDependencies().add(childAns);
            }
        }
        return root;
    }

    /**
     * 获取所有依赖的license
     *
     * @param name    应用名称
     * @param version 应用版本
     * @return String[] 所有依赖的license信息
     */
    private String[] getUniqueLicenseNames(String name, String version) {
        List<AppDependencyTableDO> dependencies = appDependencyTableDao.findAllByNameAndVersion(name, version);
        Set<String> uniqueLicenses = dependencies.stream().map(AppDependencyTableDO::getLicenses).filter(licenses -> !licenses.equals("-")).flatMap(licenses -> Arrays.stream(licenses.split(","))).map(String::trim).collect(Collectors.toSet());
        return uniqueLicenses.toArray(new String[0]);
    }

    /**
     *
     */
    private String[] getUniqueVulnerabilityNames(String name, String version) {
        List<AppDependencyTableDO> dependencies = appDependencyTableDao.findAllByNameAndVersion(name, version);
        Set<String> uniqueVulnerabilities = dependencies.stream().map(AppDependencyTableDO::getVulnerabilities).filter(vulnerabilities -> !vulnerabilities.equals("-")).flatMap(vulnerabilities -> Arrays.stream(vulnerabilities.split(","))).map(String::trim).collect(Collectors.toSet());
        return uniqueVulnerabilities.toArray(new String[0]);
    }

    /**
     * 根据文件路径删除文件夹
     *
     * @param filePath 文件路径
     */
    private void deleteFolder(String filePath) {
        File folder = new File(filePath);
        if (folder.exists()) {
            deleteFolderFile(folder);
        }
    }

    /**
     * 递归删除文件夹下的文件
     *
     * @param folder 文件夹
     */
    private void deleteFolderFile(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolderFile(file);
            }
            file.delete();
        }
        folder.delete();
    }
}