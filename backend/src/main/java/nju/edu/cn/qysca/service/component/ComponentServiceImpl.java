package nju.edu.cn.qysca.service.component;


import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.bu.BuAppDao;
import nju.edu.cn.qysca.dao.component.*;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
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
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class ComponentServiceImpl implements ComponentService {

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private BuAppDao buAppDao;


    @Autowired
    private DependencyTreeDao dependencyTreeDao;

    @Autowired
    private DependencyTableDao dependencyTableDao;

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
     * @return Page<ComponentDO> 查询结果
     */
    @Override
    public Page<ComponentDO> findComponentsPage(ComponentSearchDTO searchComponentDTO) {
        // 设置查询条件
        ComponentDO searcher = new ComponentDO();
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
        Example<ComponentDO> example = Example.of(searcher, matcher);
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "language"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "name").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(searchComponentDTO.getNumber() - 1, searchComponentDTO.getSize(), Sort.by(orders));
        return componentDao.findAll(example, pageable);
    }


    /**
     * 模糊查询组件名称
     *
     * @param name 组件名称
     * @return List<String> 模糊查询组件名称结果
     */
    @Override
    public List<ComponentSearchNameDTO> searchComponentName(String name) {
        return componentDao.searchComponentName(name);
    }

    /**
     * 存储闭源组件信息
     *
     * @param saveCloseComponentDTO 保存闭源组件接口信息
     * @return 存储闭源组件信息
     */
    @Transactional
    @Override
    public Boolean saveCloseComponent(SaveCloseComponentDTO saveCloseComponentDTO) {
        UserDO userDO = ContextUtil.getUserDO();
        //接口获得详细信息
        if (saveCloseComponentDTO.getLanguage().equals("java")) {
            ComponentDO componentDO = mavenService.componentAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
            //存储闭源组件详细信息
            componentDO.setCreator(userDO.getUid());
            componentDO.setState("RUNNING");
            componentDao.save(componentDO);
        }
        return true;
    }

    @Transactional
    @Override
    @Async("taskExecutor")
    public Boolean saveCloseComponentDependency(SaveCloseComponentDTO saveCloseComponentDTO) {
        //存储闭源组件树状依赖信息
        DependencyTreeDO closeDependencyTreeDO = mavenService.dependencyTreeAnalysis(saveCloseComponentDTO.getFilePath(), saveCloseComponentDTO.getBuilder(), saveCloseComponentDTO.getType());
        dependencyTreeDao.save(closeDependencyTreeDO);
        //存储闭源组件平铺依赖信息
        List<DependencyTableDO> dependencyTableDOList = mavenService.dependencyTableAnalysis(closeDependencyTreeDO);
        dependencyTableDao.saveAll(dependencyTableDOList);
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(closeDependencyTreeDO.getGroupId(), closeDependencyTreeDO.getArtifactId(), closeDependencyTreeDO.getVersion());
        componentDO.setState("SUCCESS");
        componentDao.save(componentDO);
        File file = new File(saveCloseComponentDTO.getFilePath());
        deleteFolder(file.getParent());
        return true;
    }


    /**
     * 将闭源组建状态设置为RUNNING
     * @param updateCloseComponentDTO 更新闭源组件信息接口
     * @return 设置闭源组件状态是否成功
     */
    @Override
    @Transactional
    public Boolean changeCloseComponentState(UpdateCloseComponentDTO updateCloseComponentDTO) {
        UserDO userDO = ContextUtil.getUserDO();
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
        if (!userDO.getUid().equals(componentDO.getCreator())) {
            throw new PlatformException(500, "您没有权限修改该组件信息");
        }
        componentDO.setState("RUNNING");
        componentDao.save(componentDO);
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
    @Async("taskExecutor")
    public Boolean updateCloseComponent(UpdateCloseComponentDTO updateCloseComponentDTO) {
        //更新基础信息
        UserDO userDO = ContextUtil.getUserDO();
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
        if (!userDO.getUid().equals(componentDO.getCreator())) {
            throw new PlatformException(500, "您没有权限修改该组件信息");
        }
        if (updateCloseComponentDTO.getFilePath() == null) {
            componentDO.setType(updateCloseComponentDTO.getType());
            componentDao.save(componentDO);
        } else {
            ApplicationDO applicationDO = applicationDao.findByNameAndVersion(updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
            if (applicationDO != null && (applicationDO.getChildApplication().length > 0 || applicationDO.getChildComponent().length > 0)) {
                throw new PlatformException(500, "该组件不可更新依赖信息");
            }
            if (updateCloseComponentDTO.getLanguage().equals("java")) {
                ComponentDO temp = mavenService.componentAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
                if (!temp.getGroupId().equals(updateCloseComponentDTO.getGroupId()) || !temp.getArtifactId().equals(updateCloseComponentDTO.getArtifactId()) || !temp.getVersion().equals(updateCloseComponentDTO.getVersion())) {
                    throw new PlatformException(500, "更新失败，组件信息不匹配");
                }
                componentDao.deleteByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
                temp.setState("SUCCESS");
                temp.setLanguage(updateCloseComponentDTO.getLanguage());
                temp.setCreator(userDO.getUid());
                componentDao.save(temp);
                dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
                DependencyTreeDO closeDependencyTreeDO = mavenService.dependencyTreeAnalysis(updateCloseComponentDTO.getFilePath(), updateCloseComponentDTO.getBuilder(), updateCloseComponentDTO.getType());
                dependencyTreeDao.save(closeDependencyTreeDO);
                //存储闭源组件平铺依赖信息
                dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(updateCloseComponentDTO.getGroupId(), updateCloseComponentDTO.getArtifactId(), updateCloseComponentDTO.getVersion());
                List<DependencyTableDO> dependencyTableDOList = mavenService.dependencyTableAnalysis(closeDependencyTreeDO);
                dependencyTableDao.saveAll(dependencyTableDOList);
            }
            File file = new File(updateCloseComponentDTO.getFilePath());
            deleteFolder(file.getParent());
        }
        return null;
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
            componentDao.deleteByGroupIdAndArtifactIdAndVersion(deleteCloseComponentDTO.getGroupId(), deleteCloseComponentDTO.getArtifactId(), deleteCloseComponentDTO.getVersion());
            dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(deleteCloseComponentDTO.getGroupId(), deleteCloseComponentDTO.getArtifactId(), deleteCloseComponentDTO.getVersion());
            dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(deleteCloseComponentDTO.getGroupId(), deleteCloseComponentDTO.getArtifactId(), deleteCloseComponentDTO.getVersion());
        } else {
            return parentApplicationDOList;
        }
        return null;
    }

    /**
     * 查询组件依赖树信息
     *
     * @param componentGavDTO 组件gav
     * @return DependencyTreeDO 依赖树信息
     */
    @Transactional
    @Override
    public DependencyTreeDO findComponentDependencyTree(ComponentGavDTO componentGavDTO) {
        DependencyTreeDO dependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String timeStamp = dateFormat.format(now);
        String tempPomFolder = tempFolder + timeStamp;
        File file = new File(tempPomFolder);
        file.mkdirs();
        String tempPath = tempPomFolder + "/pom.xml";
        if (dependencyTreeDO == null) {
            try {
                // 调用爬虫获得pom文件
                String xml = spiderService.getPomStrByGav(componentGavDTO.getGroupId(), componentGavDTO.getArtifactId(), componentGavDTO.getVersion());
                FileWriter fileWriter = new FileWriter(tempPath);
                fileWriter.write(xml);
                fileWriter.flush();
                fileWriter.close();
                dependencyTreeDO = mavenService.dependencyTreeAnalysis(tempPath, "maven", "opensource");
                dependencyTreeDao.save(dependencyTreeDO);
                List<DependencyTableDO> dependencyTableDOList = mavenService.dependencyTableAnalysis(dependencyTreeDO);
                dependencyTableDao.saveAll(dependencyTableDOList);
                deleteFolder(tempPomFolder);
            } catch (Exception e) {
                deleteFolder(tempPomFolder);
                throw new PlatformException(500, "查询组件依赖树失败");
            }

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
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(componentGavPageDTO.getNumber() - 1, componentGavPageDTO.getSize(), Sort.by(orders));
        return dependencyTableDao.findByGroupIdAndArtifactIdAndVersion(
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
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(
                componentGavDTO.getGroupId(),
                componentGavDTO.getArtifactId(),
                componentGavDTO.getVersion());
        BeanUtils.copyProperties(componentDO, componentDetailDTO);
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
