package nju.edu.cn.qysca.service.component;


import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.bu.BuAppDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.service.spider.SpiderService;
import org.springframework.beans.BeanUtils;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.*;

@Service
public class ComponentServiceImpl implements ComponentService {

    @Autowired
    private JavaComponentDao javaComponentDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private BuAppDao buAppDao;


    @Autowired
    private JavaDependencyTreeDao javaDependencyTreeDao;

    @Autowired
    private JavaDependencyTableDao javaDependencyTableDao;

    @Autowired
    private MavenService mavenService;

    @Autowired
    private SpiderService spiderService;

    @Value("${tempPomFolder}")
    private String tempFolder;

    /**
     * 分页查询组件
     *
     * @param searchComponentDTO 查询条件
     * @return Page<JavaComponentDO> 查询结果
     */
    @Override
    public Page<JavaComponentDO> findComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置查询条件
        JavaComponentDO searcher = new JavaComponentDO();
        searcher.setType(searchComponentDTO.getType());
        searcher.setGroupId(searchComponentDTO.getGroupId().equals("") ? null : searchComponentDTO.getGroupId());
        searcher.setArtifactId(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getArtifactId());
        searcher.setVersion(searchComponentDTO.getArtifactId().equals("") ? null : searchComponentDTO.getVersion());
        searcher.setName(searchComponentDTO.getName().equals("") ? null : searchComponentDTO.getName());
        searcher.setLanguage(searchComponentDTO.getLanguage().equals("") ? null : searchComponentDTO.getLanguage());
        // 设置模糊查询器
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "description", "url", "downloadUrl", "sourceUrl", "pUrl", "developers", "licenses", "hashes")
                .withIgnoreNullValues();
        Example<JavaComponentDO> example = Example.of(searcher, matcher);
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "language"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        return javaComponentDao.findAll(example, pageable);
    }


    /**
     * 模糊查询组件名称
     *
     * @param name 组件名称
     * @return List<String> 模糊查询组件名称结果
     */
    @Override
    public List<ComponentSearchNameDTO> searchComponentName(String name) {
        return javaComponentDao.searchComponentName(name);
    }

    /**
     * 存储闭源组件信息
     *
     * @param saveCloseComponentDTO 保存闭源组件接口信息
     * @return 存储闭源组件信息
     */
    @Transactional
    @Override
    public JavaComponentDO saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO) {
        UserDO userDO = ContextUtil.getUserDO();
        //接口获得详细信息
        if (saveCloseComponentDTO.getLanguage().equals("java")) {
            JavaComponentDO javaComponentDO = mavenService.componentAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
            JavaComponentDO temp = javaComponentDao.findByGroupIdAndArtifactIdAndVersion(javaComponentDO.getGroupId(), javaComponentDO.getArtifactId(), javaComponentDO.getVersion());
            if (temp != null) {
                throw new PlatformException(500, "该组件已存在");
            }
            //存储闭源组件详细信息
            javaComponentDO.setCreator(userDO.getUid());
            javaComponentDO.setState("RUNNING");
            javaComponentDao.save(javaComponentDO);
            return javaComponentDO;
        }
        return null;
    }

    @Transactional
    @Override
    @Async("taskExecutor")
    public void saveCloseComponentDependency(JavaComponentDO javaComponentDO, SaveCloseComponentDTO saveCloseComponentDTO) {
        try {
            //存储闭源组件树状依赖信息
            JavaDependencyTreeDO closeJavaDependencyTreeDO = mavenService.dependencyTreeAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
            javaDependencyTreeDao.save(closeJavaDependencyTreeDO);
            //存储闭源组件平铺依赖信息
            List<JavaDependencyTableDO> javaDependencyTableDOList = mavenService.dependencyTableAnalysis(closeJavaDependencyTreeDO);
            javaDependencyTableDao.saveAll(javaDependencyTableDOList);
            javaComponentDO.setState("SUCCESS");
            javaComponentDao.save(javaComponentDO);
            File file = new File(saveCloseComponentDTO.getFilePath());
            deleteFolder(file.getParent());
        } catch (Exception e) {
            javaComponentDO.setState("FAILED");
            javaComponentDao.save(javaComponentDO);
        }
    }


    /**
     * 将闭源组建状态设置为RUNNING
     *
     * @param updateCloseComponentDTO 更新闭源组件信息接口
     * @return 设置闭源组件状态是否成功
     */
    @Override
    @Transactional
    public Boolean changeCloseComponentState(UpdateCloseComponentDTO updateCloseComponentDTO) {
        UserDO userDO = ContextUtil.getUserDO();
        JavaComponentDO javaComponentDO = javaComponentDao.findByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
        if (!userDO.getUid().equals(javaComponentDO.getCreator())) {
            throw new PlatformException(500, "您没有权限修改该组件信息");
        }
        if (updateCloseComponentDTO.getFilePath() != null) {
            ApplicationDO applicationDO = applicationDao.findByNameAndVersion(updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
            if (applicationDO != null && (applicationDO.getChildApplication().length > 0 || applicationDO.getChildComponent().size() > 0)) {
                throw new PlatformException(500, "该组件已关联应用或组件，无法修改");
            }
            JavaComponentDO temp = mavenService.componentAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
            if (!temp.getGroupId().equals(updateCloseComponentDTO.getGroupId()) || !temp.getArtifactId().equals(updateCloseComponentDTO.getArtifactId()) || !temp.getVersion().equals(updateCloseComponentDTO.getVersion())) {
                throw new PlatformException(500, "组件信息与文件信息不匹配");
            }
        }
        javaComponentDO.setState("RUNNING");
        javaComponentDao.save(javaComponentDO);
        return true;
    }

    /**
     * 更新闭源组件信息
     *
     * @param updateCloseComponentDTO 更新闭源组件接口信息
     * @return 更新闭源组件是否成功
     */
    @Override
    @Transactional
    public void updateCloseComponent(UpdateCloseComponentDTO updateCloseComponentDTO) {
        //更新基础信息
        JavaComponentDO javaComponentDO = javaComponentDao.findByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
        if (updateCloseComponentDTO.getFilePath() == null) {
            javaComponentDO.setType(updateCloseComponentDTO.getType());
            javaComponentDao.save(javaComponentDO);
        } else {
            if (updateCloseComponentDTO.getLanguage().equals("java")) {
                javaDependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
                JavaDependencyTreeDO closeJavaDependencyTreeDO = mavenService.dependencyTreeAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
                javaDependencyTreeDao.save(closeJavaDependencyTreeDO);
                //存储闭源组件平铺依赖信息
                javaDependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
                List<JavaDependencyTableDO> javaDependencyTableDOList = mavenService.dependencyTableAnalysis(closeJavaDependencyTreeDO);
                javaDependencyTableDao.saveAll(javaDependencyTableDOList);
            }
            javaComponentDO.setState("SUCCESS");
            javaComponentDao.save(javaComponentDO);
            File file = new File(updateCloseComponentDTO.getFilePath());
            deleteFolder(file.getParent());
        }
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
        //确定是否是闭源组件
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(deleteCloseComponentDTO.getArtifactId(), deleteCloseComponentDTO.getVersion());
        if (applicationDO == null) {
            throw new PlatformException(500, "该组件不可删除");
        }
        List<ApplicationDO> parentApplicationDOList = applicationDao.findParentApplication(applicationDO.getId());
        if (parentApplicationDOList.size() == 0) {
            javaComponentDao.deleteByGroupIdAndArtifactIdAndVersion(deleteCloseComponentDTO.getGroupId(), deleteCloseComponentDTO.getArtifactId(), deleteCloseComponentDTO.getVersion());
            javaDependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(deleteCloseComponentDTO.getGroupId(), deleteCloseComponentDTO.getArtifactId(), deleteCloseComponentDTO.getVersion());
            javaDependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(deleteCloseComponentDTO.getGroupId(), deleteCloseComponentDTO.getArtifactId(), deleteCloseComponentDTO.getVersion());
        } else {
            return parentApplicationDOList;
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
    public JavaDependencyTreeDO findComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        JavaDependencyTreeDO javaDependencyTreeDO = javaDependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
        if (javaDependencyTreeDO == null) {
            javaDependencyTreeDO = mavenService.spiderDependency(componentGavDTO.getGroupId(), componentGavDTO.getArtifactId(), componentGavDTO.getVersion());
            javaDependencyTreeDao.save(javaDependencyTreeDO);
            List<JavaDependencyTableDO> javaDependencyTableDOList = mavenService.dependencyTableAnalysis(javaDependencyTreeDO);
            javaDependencyTableDao.saveAll(javaDependencyTableDOList);
        }
        return javaDependencyTreeDO;
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
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        return javaDependencyTableDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavPageDTO.getGroupId(),
                componentGavPageDTO.getArtifactId(),
                componentGavPageDTO.getVersion(), pageable);
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
        JavaComponentDO javaComponentDO = javaComponentDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
        BeanUtils.copyProperties(javaComponentDO, componentDetailDTO);
        return componentDetailDTO;
    }

    /**
     * 根据文件路径删除文件夹
     *
     * @param filePath 文件路径
     */
    private void deleteFolder(String filePath) {
        File folder = new File(filePath);
        if (folder.exists()) {
            File[] files = folder.listFiles();
            for (File file : files) {
                file.delete();
            }
        }
        folder.delete();
    }
}
