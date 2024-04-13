package nju.edu.cn.qysca.service.component;


import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.dao.application.AppComponentDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTableDao;
import nju.edu.cn.qysca.dao.application.AppDependencyTreeDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.domain.application.dos.AppComponentDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.go.GoService;
import nju.edu.cn.qysca.service.gradle.GradleService;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.service.npm.NpmService;
import nju.edu.cn.qysca.service.python.PythonService;
import nju.edu.cn.qysca.utils.FolderUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class ComponentServiceImpl implements ComponentService {

    @Autowired
    private JavaComponentDao javaComponentDao;

    @Autowired
    private AppComponentDao appComponentDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private JsComponentDao jsComponentDao;

    @Autowired
    private GoComponentDao goComponentDao;

    @Autowired
    private PythonComponentDao pythonComponentDao;


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
    private JavaDependencyTableDao javaDependencyTableDao;

    @Autowired
    private JsDependencyTableDao jsDependencyTableDao;

    @Autowired
    private GoDependencyTableDao goDependencyTableDao;

    @Autowired
    private PythonDependencyTableDao pythonDependencyTableDao;

    @Autowired
    private AppDependencyTableDao appDependencyTableDao;

    @Autowired
    private MavenService mavenService;

    @Autowired
    private GradleService gradleService;

    @Autowired
    private NpmService npmService;

    @Autowired
    private PythonService pythonService;

    @Autowired
    private GoService goService;

    /**
     * 分页查询组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaComponentDO> 查询结果
     */
    @Override
    public Page<? extends ComponentDO> findComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        Page<? extends ComponentDO> result = null;
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        switch (searchComponentDTO.getLanguage()) {
            case "java":
                result = javaComponentDao.findComponentsPage(searchComponentDTO.getName(), searchComponentDTO.getVersion(), searchComponentDTO.getType(), pageable);
                break;
            case "javaScript":
                result = jsComponentDao.findComponentsPage(searchComponentDTO.getName(), searchComponentDTO.getVersion(), searchComponentDTO.getType(), pageable);
                break;
            case "golang":
                result = goComponentDao.findComponentsPage(searchComponentDTO.getName(), searchComponentDTO.getVersion(), searchComponentDTO.getType(), pageable);
                break;
            case "python":
                result = pythonComponentDao.findComponentsPage(searchComponentDTO.getName(), searchComponentDTO.getVersion(), searchComponentDTO.getType(), pageable);
                break;
            case "app":
                result = appComponentDao.findComponentsPage(searchComponentDTO.getName(), searchComponentDTO.getVersion(), searchComponentDTO.getType(), pageable);
                break;
        }
        return result;
    }


    /**
     * 模糊查询组件名称
     *
     * @param name     组件名称
     * @param language 组件语言
     * @return List<String> 模糊查询组件名称结果
     */
    @Override
    public List<ComponentSearchNameDTO> searchComponentName(String name, String language) {
        List<ComponentSearchNameDTO> componentSearchNameDTOS = new ArrayList<>();
        switch (language) {
            case "java":
                componentSearchNameDTOS = javaComponentDao.searchComponentName(name);
                break;
            case "javaScript":
                componentSearchNameDTOS = jsComponentDao.searchComponentName(name);
                break;
            case "golang":
                componentSearchNameDTOS = goComponentDao.searchComponentName(name);
                break;
            case "python":
                componentSearchNameDTOS = pythonComponentDao.searchComponentName(name);
                break;
            case "app":
                componentSearchNameDTOS = appComponentDao.searchComponentName(name);
                break;
        }
        return componentSearchNameDTOS;
    }

    /**
     * 存储闭源组件信息
     *
     * @param saveCloseComponentDTO 保存闭源组件接口信息
     */
    @Transactional
    @Override
    public void saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO) {
        UserDO userDO = ContextUtil.getUserDO();
        //接口获得详细信息
        if (saveCloseComponentDTO.getLanguage().equals("java")) {
            JavaComponentDO javaComponentDO = null;
            if (saveCloseComponentDTO.getBuilder().equals("gradle")) {
                javaComponentDO = gradleService.componentAnalysis(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion(), saveCloseComponentDTO.getType());
            } else {
                javaComponentDO = mavenService.componentAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
            }
            JavaComponentDO temp = javaComponentDao.findByNameAndVersion(javaComponentDO.getName(), javaComponentDO.getVersion());
            if (temp != null) {
                throw new PlatformException(500, "该组件已存在");
            }
            //存储闭源组件详细信息
            javaComponentDO.setCreator(userDO.getUid());
            javaComponentDO.setState("RUNNING");
            javaComponentDao.save(javaComponentDO);
        } else if (saveCloseComponentDTO.getLanguage().equals("javaScript")) {
            JsComponentDO jsComponentDO = npmService.componentAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
            JsComponentDO temp = jsComponentDao.findByNameAndVersion(jsComponentDO.getName(), jsComponentDO.getVersion());
            if (temp != null) {
                throw new PlatformException(500, "该组件已存在");
            }
            jsComponentDO.setCreator(userDO.getUid());
            jsComponentDO.setState("RUNNING");
            jsComponentDao.save(jsComponentDO);
        } else if (saveCloseComponentDTO.getLanguage().equals("go")) {
            GoComponentDO goComponentDO = goService.componentAnalysis(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion(), saveCloseComponentDTO.getType());
            GoComponentDO temp = goComponentDao.findByNameAndVersion(goComponentDO.getName(), goComponentDO.getVersion());
            if (temp != null) {
                throw new PlatformException(500, "该组件已存在");
            }
            goComponentDO.setCreator(userDO.getUid());
            goComponentDO.setState("RUNNING");
            goComponentDao.save(goComponentDO);
        } else if (saveCloseComponentDTO.getLanguage().equals("python")) {
            PythonComponentDO pythonComponentDO = pythonService.componentAnalysis(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion(), saveCloseComponentDTO.getType());
            PythonComponentDO temp = pythonComponentDao.findByNameAndVersion(pythonComponentDO.getName(), pythonComponentDO.getVersion());
            if (temp != null) {
                throw new PlatformException(500, "该组件已存在");
            }
            pythonComponentDO.setCreator(userDO.getUid());
            pythonComponentDao.save(pythonComponentDO);
        }
    }

    @Transactional
    @Override
    @Async("taskExecutor")
    public void saveCloseComponentDependency(SaveCloseComponentDTO saveCloseComponentDTO) {
        switch (saveCloseComponentDTO.getLanguage()) {
            case "java":
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion());
                try {
                    //存储闭源组件树状依赖信息
                    JavaDependencyTreeDO closeJavaDependencyTreeDO = null;
                    if (saveCloseComponentDTO.getBuilder().equals("gradle")) {
                        closeJavaDependencyTreeDO = gradleService.dependencyTreeAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion(), saveCloseComponentDTO.getType());
                    } else {
                        closeJavaDependencyTreeDO = mavenService.dependencyTreeAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
                    }
                    javaDependencyTreeDao.save(closeJavaDependencyTreeDO);
                    //存储闭源组件平铺依赖信息
                    List<JavaDependencyTableDO> javaDependencyTableDOList = mavenService.dependencyTableAnalysis(closeJavaDependencyTreeDO);
                    javaDependencyTableDao.saveAll(javaDependencyTableDOList);
                    javaComponentDO.setState("SUCCESS");
                    javaComponentDao.save(javaComponentDO);

                } catch (Exception e) {
                    javaComponentDO.setState("FAILED");
                    javaComponentDao.save(javaComponentDO);
                }
                break;
            case "javaScript":
                JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion());
                try {
                    JsDependencyTreeDO closeJsDependencyTreeDO = npmService.dependencyTreeAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
                    jsDependencyTreeDao.save(closeJsDependencyTreeDO);
                    List<JsDependencyTableDO> jsDependencyTableDOList = npmService.dependencyTableAnalysis(closeJsDependencyTreeDO);
                    jsDependencyTableDao.saveAll(jsDependencyTableDOList);
                    jsComponentDO.setState("SUCCESS");
                    jsComponentDao.save(jsComponentDO);
                } catch (Exception e) {
                    jsComponentDO.setState("FAILED");
                    jsComponentDao.save(jsComponentDO);
                }
                break;
            case "golang":
                GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion());
                try {
                    GoDependencyTreeDO closeGoDependencyTreeDO = goService.dependencyTreeAnalysis(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion(), saveCloseComponentDTO.getType(), saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder());
                    goDependencyTreeDao.save(closeGoDependencyTreeDO);
                    List<GoDependencyTableDO> goDependencyTableDOList = goService.dependencyTableAnalysis(closeGoDependencyTreeDO);
                    goDependencyTableDao.saveAll(goDependencyTableDOList);
                    goComponentDO.setState("SUCCESS");
                    goComponentDao.save(goComponentDO);
                } catch (Exception e) {
                    goComponentDO.setState("FAILED");
                    goComponentDao.save(goComponentDO);
                }
                break;
            case "python":
                PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion());
                try {
                    PythonDependencyTreeDO pythonDependencyTreeDO = pythonService.dependencyTreeAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getName(), saveCloseComponentDTO.getVersion(), saveCloseComponentDTO.getType());
                    pythonDependencyTreeDao.save(pythonDependencyTreeDO);
                    List<PythonDependencyTableDO> pythonDependencyTableDOS = pythonService.dependencyTableAnalysis(pythonDependencyTreeDO);
                    pythonDependencyTableDao.saveAll(pythonDependencyTableDOS);
                    pythonComponentDO.setState("SUCCESS");
                    pythonComponentDao.save(pythonComponentDO);
                } catch (Exception e) {
                    pythonComponentDO.setState("FAILED");
                    pythonComponentDao.save(pythonComponentDO);
                }
                break;
        }
        File file = new File(saveCloseComponentDTO.getFilePath());
        FolderUtil.deleteFolder(file.getParent());
    }


    /**
     * 将闭源组建状态设置为RUNNING
     *
     * @param updateCloseComponentDTO 更新闭源组件信息接口
     */
    @Override
    @Transactional
    public void changeCloseComponentState(UpdateCloseComponentDTO updateCloseComponentDTO) {
        UserDO userDO = ContextUtil.getUserDO();
        if (updateCloseComponentDTO.getFilePath() != null) {
            ApplicationDO applicationDO = applicationDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
            if (applicationDO != null && (applicationDO.getChildApplication().length > 0 || applicationDO.getChildComponent().size() > 0)) {
                throw new PlatformException(500, "该组件已关联应用或组件，无法修改");
            }
        }
        switch (updateCloseComponentDTO.getLanguage()) {
            case "java":
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                if (!userDO.getUid().equals(javaComponentDO.getCreator())) {
                    throw new PlatformException(500, "您没有权限修改该组件信息");
                }
                if (updateCloseComponentDTO.getFilePath() != null) {
                    //gradle方式无法检查
                    if (updateCloseComponentDTO.getBuilder().equals("maven")) {
                        JavaComponentDO temp = mavenService.componentAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
                        if (!temp.getName().equals(updateCloseComponentDTO.getName()) || !temp.getVersion().equals(updateCloseComponentDTO.getVersion())) {
                            throw new PlatformException(500, "组件信息与文件信息不匹配");
                        }
                    }
                }
                javaComponentDO.setState("RUNNING");
                javaComponentDao.save(javaComponentDO);
                break;
            case "javaScript":
                JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                if (!userDO.getUid().equals(jsComponentDO.getCreator())) {
                    throw new PlatformException(500, "您没有权限修改该组件信息");
                }
                if (updateCloseComponentDTO.getFilePath() != null) {
                    JsComponentDO temp = npmService.componentAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
                    if (!temp.getName().equals(updateCloseComponentDTO.getName()) || !temp.getVersion().equals(updateCloseComponentDTO.getVersion())) {
                        throw new PlatformException(500, "组件信息与文件信息不匹配");
                    }
                }
                jsComponentDO.setState("RUNNING");
                jsComponentDao.save(jsComponentDO);
                break;
            case "golang":
                GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                if (!userDO.getUid().equals(goComponentDO.getCreator())) {
                    throw new PlatformException(500, "您没有权限修改该组件信息");
                }
                //Go语言无法检查
                goComponentDO.setState("RUNNING");
                goComponentDao.save(goComponentDO);
                break;
            case "python":
                PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                if (!userDO.getUid().equals(pythonComponentDO.getCreator())) {
                    throw new PlatformException(500, "您没有权限修改该组件信息");
                }
                //Python语言无法检查
                pythonComponentDO.setState("RUNNING");
                pythonComponentDao.save(pythonComponentDO);
                break;
        }
    }

    /**
     * 更新闭源组件信息
     *
     * @param updateCloseComponentDTO 更新闭源组件接口信息
     */
    @Override
    @Transactional
    public void updateCloseComponent(UpdateCloseComponentDTO updateCloseComponentDTO) {
        switch (updateCloseComponentDTO.getLanguage()) {
            case "java":
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                javaComponentDO.setType(updateCloseComponentDTO.getType());
                javaDependencyTreeDao.deleteByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                JavaDependencyTreeDO closeJavaDependencyTreeDO = null;
                if (updateCloseComponentDTO.getBuilder().equals("gradle")) {
                    closeJavaDependencyTreeDO = gradleService.dependencyTreeAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion(), updateCloseComponentDTO.getType());
                } else {
                    closeJavaDependencyTreeDO = mavenService.dependencyTreeAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
                }
                javaDependencyTreeDao.save(closeJavaDependencyTreeDO);
                //存储闭源组件平铺依赖信息
                javaDependencyTableDao.deleteAllByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                List<JavaDependencyTableDO> javaDependencyTableDOList = mavenService.dependencyTableAnalysis(closeJavaDependencyTreeDO);
                javaDependencyTableDao.saveAll(javaDependencyTableDOList);
                javaComponentDO.setState("SUCCESS");
                javaComponentDao.save(javaComponentDO);
                break;
            case "javaScript":
                JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                jsComponentDO.setType(updateCloseComponentDTO.getType());
                jsDependencyTreeDao.deleteByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                JsDependencyTreeDO jsDependencyTreeDO = npmService.dependencyTreeAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
                jsDependencyTreeDao.save(jsDependencyTreeDO);
                jsDependencyTableDao.deleteAllByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                List<JsDependencyTableDO> jsDependencyTableDOList = npmService.dependencyTableAnalysis(jsDependencyTreeDO);
                jsDependencyTableDao.saveAll(jsDependencyTableDOList);
                jsComponentDO.setState("SUCCESS");
                jsComponentDao.save(jsComponentDO);
                break;
            case "golang":
                GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                goComponentDO.setType(updateCloseComponentDTO.getType());
                goDependencyTreeDao.deleteByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                GoDependencyTreeDO goDependencyTreeDO = goService.dependencyTreeAnalysis(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion(), updateCloseComponentDTO.getType(), updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder());
                goDependencyTreeDao.save(goDependencyTreeDO);
                goDependencyTableDao.deleteAllByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                List<GoDependencyTableDO> goDependencyTableDOList = goService.dependencyTableAnalysis(goDependencyTreeDO);
                goDependencyTableDao.saveAll(goDependencyTableDOList);
                goComponentDO.setState("SUCCESS");
                goComponentDao.save(goComponentDO);
                break;
            case "python":
                PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                pythonComponentDO.setType(updateCloseComponentDTO.getType());
                pythonDependencyTreeDao.deleteByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                PythonDependencyTreeDO pythonDependencyTreeDO = pythonService.dependencyTreeAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion(), updateCloseComponentDTO.getType());
                pythonDependencyTreeDao.save(pythonDependencyTreeDO);
                pythonDependencyTableDao.deleteAllByNameAndVersion(updateCloseComponentDTO.getName(), updateCloseComponentDTO.getVersion());
                List<PythonDependencyTableDO> pythonDependencyTableDOList = pythonService.dependencyTableAnalysis(pythonDependencyTreeDO);
                pythonDependencyTableDao.saveAll(pythonDependencyTableDOList);
                pythonComponentDO.setState("SUCCESS");
                pythonComponentDao.save(pythonComponentDO);
                break;
        }
        File file = new File(updateCloseComponentDTO.getFilePath());
        FolderUtil.deleteFolder(file.getParent());
    }


    /**
     * 删除闭源组件
     *
     * @param deleteCloseComponentDTO 删除闭源组件信息接口
     * @return List<ApplicationDO> 被依赖的应用
     */
    @Override
    @Transactional
    public List<ApplicationDO> deleteCloseComponent(DeleteCloseComponentDTO deleteCloseComponentDTO) {
        List<ApplicationDO> parentApplicationDOList = null;
        switch (deleteCloseComponentDTO.getLanguage()) {
            case "java":
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                if (javaComponentDO.getCreator().equals("")) {
                    throw new PlatformException(500, "开源组件不可删除");
                }
                parentApplicationDOList = applicationDao.findParentApplication("java", javaComponentDO.getId());
                if (!parentApplicationDOList.isEmpty()) {
                    return parentApplicationDOList;
                }
                javaComponentDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                javaDependencyTreeDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                javaDependencyTableDao.deleteAllByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                break;
            case "javaScript":
                JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                if (jsComponentDO.getCreator().equals("")) {
                    throw new PlatformException(500, "开源组件不可删除");
                }
                parentApplicationDOList = applicationDao.findParentApplication("javaScript", jsComponentDO.getId());
                if (!parentApplicationDOList.isEmpty()) {
                    return parentApplicationDOList;
                }
                jsComponentDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                jsDependencyTreeDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                jsDependencyTableDao.deleteAllByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                break;
            case "golang":
                GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                if (goComponentDO.getCreator().equals("")) {
                    throw new PlatformException(500, "开源组件不可删除");
                }
                parentApplicationDOList = applicationDao.findParentApplication("go", goComponentDO.getId());
                if (!parentApplicationDOList.isEmpty()) {
                    return parentApplicationDOList;
                }
                goComponentDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                goDependencyTreeDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                goDependencyTableDao.deleteAllByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                break;
            case "python":
                PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                if (pythonComponentDO.getCreator().equals("")) {
                    throw new PlatformException(500, "开源组件不可删除");
                }
                parentApplicationDOList = applicationDao.findParentApplication("python", pythonComponentDO.getId());
                if (!parentApplicationDOList.isEmpty()) {
                    return parentApplicationDOList;
                }
                pythonComponentDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                pythonDependencyTreeDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                pythonDependencyTableDao.deleteAllByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                break;
            case "app":
                ApplicationDO applicationDO = applicationDao.findByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                parentApplicationDOList = applicationDao.findParentApplication(applicationDO.getId());
                if (!parentApplicationDOList.isEmpty()) {
                    return parentApplicationDOList;
                }
                appComponentDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                appDependencyTreeDao.deleteByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                appDependencyTableDao.deleteAllByNameAndVersion(deleteCloseComponentDTO.getName(), deleteCloseComponentDTO.getVersion());
                break;
        }
        return null;
    }

    /**
     * 查询组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return JavaDependencyTreeDO 依赖树信息
     */
    @Transactional
    @Override
    public DependencyTreeDO findComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        DependencyTreeDO dependencyTreeDO = null;
        switch (componentGavDTO.getLanguage()) {
            case "java":
                dependencyTreeDO = javaDependencyTreeDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                if (dependencyTreeDO == null) {
                    String[] temp = componentGavDTO.getName().split(":");
                    dependencyTreeDO = mavenService.spiderDependency(temp[0], temp[1], componentGavDTO.getVersion());
                    javaDependencyTreeDao.save((JavaDependencyTreeDO) dependencyTreeDO);
                    List<JavaDependencyTableDO> javaDependencyTableDOList = mavenService.dependencyTableAnalysis((JavaDependencyTreeDO) dependencyTreeDO);
                    javaDependencyTableDao.saveAll(javaDependencyTableDOList);
                }
                break;
            case "javaScript":
                dependencyTreeDO = jsDependencyTreeDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                if (dependencyTreeDO == null) {
                    dependencyTreeDO = npmService.spiderDependency(componentGavDTO.getName(), componentGavDTO.getVersion());
                    jsDependencyTreeDao.save((JsDependencyTreeDO) dependencyTreeDO);
                    List<JsDependencyTableDO> jsDependencyTableDOList = npmService.dependencyTableAnalysis((JsDependencyTreeDO) dependencyTreeDO);
                    jsDependencyTableDao.saveAll(jsDependencyTableDOList);
                }
                break;
            case "golang":
                dependencyTreeDO = goDependencyTreeDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                if (dependencyTreeDO == null) {
                    dependencyTreeDO = goService.spiderDependency(componentGavDTO.getName(), componentGavDTO.getVersion());
                    goDependencyTreeDao.save((GoDependencyTreeDO) dependencyTreeDO);
                    List<GoDependencyTableDO> goDependencyTableDOList = goService.dependencyTableAnalysis((GoDependencyTreeDO) dependencyTreeDO);
                    goDependencyTableDao.saveAll(goDependencyTableDOList);
                }
                break;
            case "python":
                dependencyTreeDO = pythonDependencyTreeDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                if (dependencyTreeDO == null) {
                    dependencyTreeDO = pythonService.spiderDependency(componentGavDTO.getName(), componentGavDTO.getVersion());
                    pythonDependencyTreeDao.save((PythonDependencyTreeDO) dependencyTreeDO);
                    List<PythonDependencyTableDO> pythonDependencyTableDOList = pythonService.dependencyTableAnalysis((PythonDependencyTreeDO) dependencyTreeDO);
                    pythonDependencyTableDao.saveAll(pythonDependencyTableDOList);
                }
                break;
            case "app":
                dependencyTreeDO = appDependencyTreeDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                break;
        }
        return dependencyTreeDO;
    }


    /**
     * 分页查询组件依赖平铺信息
     *
     * @param componentGavPageDTO 带分页组件gav信息
     * @return Page<ComponentTableDTO> 分页查询结果
     */
    @Override
    public Page<ComponentTableDTO> findComponentDependencyTable(ComponentGavPageDTO componentGavPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        Page<ComponentTableDTO> result = null;
        switch (componentGavPageDTO.getLanguage()) {
            case "java":
                result = javaDependencyTableDao.findByNameAndVersion(componentGavPageDTO.getName(), componentGavPageDTO.getVersion(), pageable);
                break;
            case "javaScript":
                result = jsDependencyTableDao.findByNameAndVersion(componentGavPageDTO.getName(), componentGavPageDTO.getVersion(), pageable);
                break;
            case "golang":
                result = goDependencyTableDao.findByNameAndVersion(componentGavPageDTO.getName(), componentGavPageDTO.getVersion(), pageable);
                break;
            case "python":
                result = pythonDependencyTableDao.findByNameAndVersion(componentGavPageDTO.getName(), componentGavPageDTO.getVersion(), pageable);
                break;
            case "app":
                result = appDependencyTableDao.findByNameAndVersion(componentGavPageDTO.getName(), componentGavPageDTO.getVersion(), pageable);
                break;
        }
        return result;
    }


    /**
     * 查询指定组件的详细信息
     *
     * @param componentGavDTO 组件gav
     * @return ComponentDetailDTO 组件详细信息
     */
    @Override
    public ComponentDetailDTO findComponentDetail(ComponentGavDTO componentGavDTO) {
        ComponentDetailDTO componentDetailDTO = new ComponentDetailDTO();
        switch (componentGavDTO.getLanguage()) {
            case "java":
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                BeanUtils.copyProperties(javaComponentDO, componentDetailDTO);
                break;
            case "javaScript":
                JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                BeanUtils.copyProperties(jsComponentDO, componentDetailDTO);
                break;
            case "golang":
                GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                BeanUtils.copyProperties(goComponentDO, componentDetailDTO);
                break;
            case "python":
                PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                BeanUtils.copyProperties(pythonComponentDO, componentDetailDTO);
                break;
            case "app":
                AppComponentDO appComponentDO = appComponentDao.findByNameAndVersion(componentGavDTO.getName(), componentGavDTO.getVersion());
                BeanUtils.copyProperties(appComponentDO, componentDetailDTO);
                componentDetailDTO.setLanguage(String.join(",", appComponentDO.getLanguage()));
                break;
        }
        return componentDetailDTO;
    }


    /**
     * 修改发布组件的详细信息
     */
    @Override
    @Transactional
    public void changeComponentDetail(ChangeComponentDetailDTO changeComponentDetailDTO) {
        switch (changeComponentDetailDTO.getLanguage()) {
            case "java":
                JavaComponentDO javaComponentDO = javaComponentDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                JavaDependencyTreeDO javaDependencyTreeDO = javaDependencyTreeDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                if (javaDependencyTreeDO == null) {
                    throw new PlatformException(500, "开源组件不可修改");
                }
                javaComponentDO.setUrl(changeComponentDetailDTO.getUrl());
                javaComponentDO.setSourceUrl(changeComponentDetailDTO.getSourceUrl());
                javaComponentDO.setDownloadUrl(changeComponentDetailDTO.getDownloadUrl());
                javaComponentDO.setPUrl(changeComponentDetailDTO.getPackageUrl());
                javaComponentDao.save(javaComponentDO);
                break;
            case "golang":
                GoComponentDO goComponentDO = goComponentDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                GoDependencyTreeDO goDependencyTreeDO = goDependencyTreeDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                if (goDependencyTreeDO == null) {
                    throw new PlatformException(500, "开源组件不可修改");
                }
                goComponentDO.setUrl(changeComponentDetailDTO.getUrl());
                goComponentDO.setSourceUrl(changeComponentDetailDTO.getSourceUrl());
                goComponentDO.setDownloadUrl(changeComponentDetailDTO.getDownloadUrl());
                goComponentDO.setPUrl(changeComponentDetailDTO.getPackageUrl());
                goComponentDao.save(goComponentDO);
                break;
            case "javaScript":
                JsComponentDO jsComponentDO = jsComponentDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                JsDependencyTreeDO jsDependencyTreeDO = jsDependencyTreeDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                if (jsDependencyTreeDO == null) {
                    throw new PlatformException(500, "开源组件不可修改");
                }
                jsComponentDO.setUrl(changeComponentDetailDTO.getUrl());
                jsComponentDO.setSourceUrl(changeComponentDetailDTO.getSourceUrl());
                jsComponentDO.setDownloadUrl(changeComponentDetailDTO.getDownloadUrl());
                jsComponentDO.setPUrl(changeComponentDetailDTO.getPackageUrl());
                jsComponentDao.save(jsComponentDO);
                break;
            case "python":
                PythonComponentDO pythonComponentDO = pythonComponentDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                PythonDependencyTreeDO pythonDependencyTreeDO = pythonDependencyTreeDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                if (pythonDependencyTreeDO == null) {
                    throw new PlatformException(500, "开源组件不可修改");
                }
                pythonComponentDO.setUrl(changeComponentDetailDTO.getUrl());
                pythonComponentDO.setSourceUrl(changeComponentDetailDTO.getSourceUrl());
                pythonComponentDO.setDownloadUrl(changeComponentDetailDTO.getDownloadUrl());
                pythonComponentDO.setPUrl(changeComponentDetailDTO.getPackageUrl());
                pythonComponentDao.save(pythonComponentDO);
                break;
            case "app":
                AppComponentDO appComponentDO = appComponentDao.findByNameAndVersion(changeComponentDetailDTO.getName(), changeComponentDetailDTO.getVersion());
                appComponentDO.setUrl(changeComponentDetailDTO.getUrl());
                appComponentDO.setSourceUrl(changeComponentDetailDTO.getSourceUrl());
                appComponentDO.setDownloadUrl(changeComponentDetailDTO.getDownloadUrl());
                appComponentDO.setPUrl(changeComponentDetailDTO.getPackageUrl());
                appComponentDao.save(appComponentDO);
                break;
        }
    }
}
