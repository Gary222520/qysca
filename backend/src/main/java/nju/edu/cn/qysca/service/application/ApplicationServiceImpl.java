package nju.edu.cn.qysca.service.application;

import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.dao.component.DependencyTableDao;
import nju.edu.cn.qysca.dao.component.DependencyTreeDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.ComponentCompareTreeDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentDetailDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.application.dos.*;
import nju.edu.cn.qysca.domain.application.dtos.*;
import nju.edu.cn.qysca.exception.PlatformException;
import nju.edu.cn.qysca.service.maven.MavenService;
import nju.edu.cn.qysca.utils.excel.ExcelUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class ApplicationServiceImpl implements ApplicationService {

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private DependencyTreeDao dependencyTreeDao;

    @Autowired
    private DependencyTableDao dependencyTableDao;

    @Autowired
    private ComponentDao componentDao;

    @Autowired
    private MavenService mavenService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 分页获取根应用信息
     *
     * @param number 页码
     * @param size   页大小
     * @return Page<ApplicationDO> 应用信息分页结果
     */
    @Override
    public Page<ApplicationDO> findRootPage(int number, int size) {
        Pageable pageable = PageRequest.of(number - 1, size);
        return applicationDao.findRootPage(pageable);
    }


    /**
     * 模糊查询应用名称
     * @param name 应用名称
     * @return List<String> 模糊查询应用名称列表
     */
    @Override
    public List<String> searchApplicationName(String name) {
        return applicationDao.searchApplicationName(name);
    }

    /**
     * 根据名称查询应用 并返回应用的最新版本
     * @param name 应用名称
     * @return ApplicationDO 应用信息
     */
    @Override
    public ApplicationDO findApplication(String name) {
        return applicationDao.findApplication(name);
    }


    /**
     * 根据应用Id返回子应用信息
     * @param  groupId 组织Id
     * @param  artifactId 应用Id
     * @param  version 版本
     * @return SubApplicationDTO 子应用信息
     */
    @Override
    public SubApplicationDTO findSubApplication(String groupId, String artifactId, String version) {
        ApplicationDO applicationDO =  applicationDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        List<ApplicationDO> subapplication = applicationDao.findSubApplication(applicationDO.getId());
        List<ComponentDO> subComponent = componentDao.findSubComponent(applicationDO.getId());
        SubApplicationDTO subApplicationDTO = new SubApplicationDTO();
        subApplicationDTO.setSubapplication(subapplication);
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
        if(StringUtils.isEmpty(saveApplicationDTO.getId())){
            applicationDO = new ApplicationDO();
            BeanUtils.copyProperties(saveApplicationDTO, applicationDO);
            applicationDO.setState("CREATED");
            applicationDO.setLock(false);
            applicationDO.setRelease(false);
            applicationDO.setRoot(saveApplicationDTO.getParentId() == null);
        }else{
            applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(saveApplicationDTO.getGroupId(), saveApplicationDTO.getArtifactId(), saveApplicationDTO.getVersion());
            applicationDO.setDescription(saveApplicationDTO.getDescription());
            applicationDO.setType(saveApplicationDTO.getType());
        }
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        applicationDO.setTime(timeStamp);
        applicationDao.save(applicationDO);
        if(saveApplicationDTO.getParentId() != null) {
            ApplicationDO parentApplicationDO = applicationDao.findApplicationDOById(saveApplicationDTO.getParentId());
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildApplication()));
            temp.add(applicationDO.getId());
            parentApplicationDO.setChildApplication(temp.toArray(new String[temp.size()]));
            applicationDao.save(parentApplicationDO);
        }
        return true;
    }


    /**
     * 在保存应用依赖时将应用状态改为RUNNING
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本
     */
    @Override
    public void changeApplicationState(String groupId, String artifactId, String version) {
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
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
            ComponentDependencyTreeDO componentDependencyTreeDO = mavenService.applicationDependencyAnalysis(saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder(), 0);
            DependencyTreeDO applicationDependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
            if(applicationDependencyTreeDO == null){
                applicationDependencyTreeDO = new DependencyTreeDO();
                applicationDependencyTreeDO.setGroupId(saveApplicationDependencyDTO.getGroupId());
                applicationDependencyTreeDO.setArtifactId(saveApplicationDependencyDTO.getArtifactId());
                applicationDependencyTreeDO.setVersion(saveApplicationDependencyDTO.getVersion());
                applicationDependencyTreeDO.setTree(componentDependencyTreeDO);
            }else{
                applicationDependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
                applicationDependencyTreeDO.setTree(componentDependencyTreeDO);
            }
            dependencyTreeDao.save(applicationDependencyTreeDO);
            // 批量更新依赖平铺表
            List<DependencyTableDO> applicationDependencyTableDOS = createapplicationDependencyTable(applicationDependencyTreeDO);
            for (DependencyTableDO dependencyTableDO : applicationDependencyTableDOS) {
                dependencyTableDO.setLanguage(saveApplicationDependencyDTO.getLanguage());
            }
            dependencyTableDao.saveAll(applicationDependencyTableDOS);
            // 更改状态为SUCCESS
            ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
            applicationDO.setBuilder(saveApplicationDependencyDTO.getBuilder());
            applicationDO.setScanner(saveApplicationDependencyDTO.getScanner());
            applicationDO.setLanguage(saveApplicationDependencyDTO.getLanguage());
            applicationDO.setState("SUCCESS");
            applicationDao.save(applicationDO);
            File file = new File(saveApplicationDependencyDTO.getFilePath());
            redisTemplate.delete(file.getParentFile().getName());
            deleteFolder(saveApplicationDependencyDTO.getFilePath().substring(0, saveApplicationDependencyDTO.getFilePath().lastIndexOf("/")));
        } catch (Exception e) {
            ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
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
        // 保存新应用
        ApplicationDO oldApplicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(upgradeApplicationDTO.getGroupId(), upgradeApplicationDTO.getArtifactId(), upgradeApplicationDTO.getOldVersion());
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
        applicationDao.save(newApplicationDO);
        // 新增新应用关系 删除旧应用关系
        if(!StringUtils.isEmpty(upgradeApplicationDTO.getParentGroupId()) && !StringUtils.isEmpty(upgradeApplicationDTO.getParentArtifactId()) && !StringUtils.isEmpty(upgradeApplicationDTO.getParentVersion())){
            ApplicationDO parentApplicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(upgradeApplicationDTO.getParentGroupId(), upgradeApplicationDTO.getParentArtifactId(), upgradeApplicationDTO.getParentVersion());
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
    public Boolean deleteApplicationVersion(DeleteApplicationDTO deleteApplicationDTO) {
        // 先删除在父应用中的依赖信息
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(deleteApplicationDTO.getGroupId(), deleteApplicationDTO.getArtifactId(), deleteApplicationDTO.getVersion());
        if(!StringUtils.isEmpty(deleteApplicationDTO.getParentGroupId()) && !StringUtils.isEmpty(deleteApplicationDTO.getParentArtifactId())&& !StringUtils.isEmpty(deleteApplicationDTO.getParentVersion())) {
            ApplicationDO parentApplicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(deleteApplicationDTO.getParentGroupId(), deleteApplicationDTO.getParentArtifactId(), deleteApplicationDTO.getParentVersion());
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildApplication()));
            temp.remove(applicationDO.getId());
            parentApplicationDO.setChildApplication(temp.toArray(new String[temp.size()]));
            applicationDao.save(parentApplicationDO);
        }
        // 如果应用本身是其他应用的子应用则无需继续操作
        if(!applicationDao.findParentApplication(applicationDO.getId()).isEmpty()) {
            return Boolean.TRUE;
        }
        // 递归删除子应用 若子应用在其他应用中引用则只删除关系，否则删除应用
        deleteapplicationRecursively(applicationDO.getId());
        return Boolean.TRUE;
    }
    private void deleteapplicationRecursively(String applicationId) {
        ApplicationDO applicationDO = applicationDao.findApplicationDOById(applicationId);
        if(applicationDO != null){
            deleteChildrenRecursively(applicationDO.getChildApplication());
            applicationDao.delete(applicationDO);
            dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
            dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
        }
    }

    private void deleteChildrenRecursively(String[] children) {
        if(children == null || children.length == 0){
            return;
        }
        for(String child : children){
            List<ApplicationDO> parentapplication = applicationDao.findParentApplication(child);
            if(parentapplication.size() <= 1){
                deleteapplicationRecursively(child);
            }
        }
    }


    /**
     * 在应用中增加组件
     * @param applicationComponentDTO 应用组件信息接口
     * @return 在应用中增加组件是否成功
     */
    @Override
    @Transactional
    public Boolean saveApplicationComponent(ApplicationComponentDTO applicationComponentDTO) {
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getParentGroupId(), applicationComponentDTO.getParentArtifactId(), applicationComponentDTO.getParentVersion());
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(applicationDO.getChildComponent()));
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getGroupId(), applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
        temp.add(componentDO.getId());
        applicationDO.setChildComponent(temp.toArray(new String[temp.size()]));
        applicationDao.save(applicationDO);
        return Boolean.TRUE;
    }

    /**
     * 在应用中删除组件
     * @param applicationComponentDTO 应用组件信息接口
     * @return 在应用中删除组件是否成功
     */
    @Override
    @Transactional
    public Boolean deleteApplicationComponent(ApplicationComponentDTO applicationComponentDTO) {
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getParentGroupId(), applicationComponentDTO.getParentArtifactId(), applicationComponentDTO.getParentVersion());
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(applicationDO.getChildComponent()));
        ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getGroupId(), applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
        temp.remove(componentDO.getId());
        applicationDO.setChildComponent(temp.toArray(new String[temp.size()]));
        applicationDao.save(applicationDO);
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public Boolean addSubApplication(AddSubApplicationDTO addSubApplicationDTO) {
        ApplicationDO parentApplicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(addSubApplicationDTO.getParentGroupId(), addSubApplicationDTO.getParentArtifactId(), addSubApplicationDTO.getParentVersion());
        ApplicationDO subApplicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(addSubApplicationDTO.getGroupId(), addSubApplicationDTO.getArtifactId(), addSubApplicationDTO.getVersion());
        ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildComponent()));
        temp.add(subApplicationDO.getId());
        parentApplicationDO.setChildComponent(temp.toArray(new String[temp.size()]));
        applicationDao.save(parentApplicationDO);
        return Boolean.TRUE;
    }

    /**
     * 改变应用的锁定状态
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本号
     */
    @Override
    public void changeLockState(String groupId, String artifactId, String version) {
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        if(applicationDO.getLock()){
            applicationDO.setLock(false);
        }else{
            applicationDO.setLock(true);
        }
        applicationDao.save(applicationDO);
    }

    /**
     * 改变应用的发布状态
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param version 版本号
     */
    @Override
    public void changeReleaseState(String groupId, String artifactId, String version) {
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        if(applicationDO.getRelease()) {
            applicationDO.setRelease(false);
        } else {
            applicationDO.setRelease(true);
        }
        applicationDao.save(applicationDO);
    }

    /**
     * 分页获取指定应用的版本信息
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @param number 页码
     * @param size   页大小
     * @return Page<ApplicationDO> 应用版本信息分页结果
     */
    @Override
    public Page<ApplicationDO> findApplicationVersionPage(String groupId, String artifactId, int number, int size) {
        // 数据库页号从0开始，需减1
        return applicationDao.findAllByGroupIdAndArtifactId(groupId, artifactId, PageRequest.of(number - 1, size, Sort.by(Sort.Order.desc("version").nullsLast())));
    }

    /**
     * 检查指定应用扫描中组件的个数
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return Integer 扫描中组件的个数
     */
    @Override
    public Integer checkRunningApplication(String groupId, String artifactId){
        return applicationDao.countByGroupIdAndArtifactIdAndState(groupId, artifactId, "RUNNING");
    }

    /**
     * 获取指定应用的所有版本列表
     *
     * @param groupId 组织Id
     * @param artifactId 工件Id
     * @return List<String> 版本列表
     */
    @Override
    public List<String> getVersionsList(String groupId, String artifactId) {
        return applicationDao.findVersionsByGroupIdAndArtifactId(groupId, artifactId);
    }

    /**
     * 获取指定应用指定版本的详细信息
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @return ApplicationDO 应用版本的详细信息
     */
    @Override
    public ApplicationDO findApplicationVersionInfo(ApplicationSearchDTO applicationSearchDTO) {
        return applicationDao.findByGroupIdAndArtifactIdAndVersion(
                applicationSearchDTO.getGroupId(),
                applicationSearchDTO.getArtifactId(),
                applicationSearchDTO.getVersion());
    }

    /**
     * 查询应用依赖树信息
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @return DependencyTreeDO 应用依赖树信息
     */
    @Override
    public DependencyTreeDO findApplicationDependencyTree(ApplicationSearchDTO applicationSearchDTO) {
        return dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                applicationSearchDTO.getGroupId(),
                applicationSearchDTO.getArtifactId(),
                applicationSearchDTO.getVersion());
    }

    /**
     * 分页查询应用依赖平铺信息
     *
     * @param applicationSearchPageDTO 带分页应用版本搜索信息
     * @return Page<ComponentTableDTO> 依赖平铺信息分页
     */
    @Override
    public Page<ComponentTableDTO> findApplicationDependencyTable(ApplicationSearchPageDTO applicationSearchPageDTO) {
        // 设置排序规则
        List<Sort.Order> orders = new ArrayList<>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "depth").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "groupId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.ASC, "artifactId").nullsLast());
        orders.add(new Sort.Order(Sort.Direction.DESC, "version").nullsLast());
        // 数据库页号从0开始，需减1
        Pageable pageable = PageRequest.of(applicationSearchPageDTO.getNumber() - 1, applicationSearchPageDTO.getSize(), Sort.by(orders));
        return dependencyTableDao.findByGroupIdAndArtifactIdAndVersion(
                applicationSearchPageDTO.getGroupId(),
                applicationSearchPageDTO.getArtifactId(),
                applicationSearchPageDTO.getVersion(), pageable);
    }

    /**
     * 导出应用依赖平铺信息（简明）Excel
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @param response         Http服务响应
     */
    @Override
    public void exportTableExcelBrief(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response) {
        List<TableExcelBriefDTO> resList = dependencyTableDao.findTableListByGroupIdAndArtifactIdAndVersion(
                applicationSearchDTO.getGroupId(), applicationSearchDTO.getArtifactId(), applicationSearchDTO.getVersion());
        String fileName = applicationSearchDTO.getArtifactId() + "-" + applicationSearchDTO.getVersion() + "-dependencyTable-brief";
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
     * @param response         Http服务响应
     */
    @Override
    public void exportTableExcelDetail(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response) {
        List<TableExcelDetailDTO> resList = new ArrayList<>();
        String fileName = applicationSearchDTO.getArtifactId() + "-" + applicationSearchDTO.getVersion() + "-dependencyTable-detail";
        // 先获取依赖平铺的简明信息
        List<TableExcelBriefDTO> briefList = dependencyTableDao.findTableListByGroupIdAndArtifactIdAndVersion(
                applicationSearchDTO.getGroupId(), applicationSearchDTO.getArtifactId(), applicationSearchDTO.getVersion());
        for (TableExcelBriefDTO brief : briefList) {
            TableExcelDetailDTO detail = new TableExcelDetailDTO();
            BeanUtils.copyProperties(brief, detail);
            ComponentDetailDTO componentDetailDTO = new ComponentDetailDTO();
            // 获取对应依赖组件的详细信息
            ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(detail.getCGroupId(), detail.getCArtifactId(), detail.getCVersion());
            if (componentDO == null) {
                resList.add(detail);
                continue;
            }
            BeanUtils.copyProperties(componentDO, componentDetailDTO);
            detail.setName(componentDetailDTO.getName());
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
            for (LicenseDO licenseDO : componentDetailDTO.getLicenses()) {
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
        DependencyTreeDO fromDependencyTree = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                versionCompareReqDTO.getGroupId(), versionCompareReqDTO.getArtifactId(), versionCompareReqDTO.getFromVersion());
        if (fromDependencyTree == null) {
            throw new PlatformException(500, "被对比的应用版本依赖树信息不存在");
        }
        DependencyTreeDO toDependencyTree = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                versionCompareReqDTO.getGroupId(), versionCompareReqDTO.getArtifactId(), versionCompareReqDTO.getToVersion());
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
    private ComponentCompareTreeDTO recursionDealWithChange(ComponentDependencyTreeDO from, ComponentDependencyTreeDO to) {
        if (from == null || to == null) {
            return null;
        }
        // 变更的组件打上CHANGE标记
        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
        BeanUtils.copyProperties(to, root);
        root.setMark("CHANGE");
        // 分析各子树应属于何种标记
        Map<String, ComponentDependencyTreeDO> fromMap = new HashMap<>();
        Map<String, ComponentDependencyTreeDO> toMap = new HashMap<>();
        Set<String> intersection = new HashSet<>();
        for (ComponentDependencyTreeDO fromChild : from.getDependencies()) {
            fromMap.put(fromChild.getGroupId() + fromChild.getArtifactId() + fromChild.getType(), fromChild);
        }
        for (ComponentDependencyTreeDO toChild : to.getDependencies()) {
            toMap.put(toChild.getGroupId() + toChild.getArtifactId() + toChild.getType(), toChild);
        }
        for (String key : toMap.keySet()) {
            // 求交集
            if (fromMap.containsKey(key)) {
                intersection.add(key);
            }
        }
        // 依次进行分析
        for (ComponentDependencyTreeDO toChild : to.getDependencies()) {
            String key = toChild.getGroupId() + toChild.getArtifactId() + toChild.getType();
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
        for (ComponentDependencyTreeDO fromChild : from.getDependencies()) {
            String key = fromChild.getGroupId() + fromChild.getArtifactId() + fromChild.getType();
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
    private ComponentCompareTreeDTO recursionMark(ComponentDependencyTreeDO tree, String mark) {
        if (tree == null) {
            return null;
        }
        ComponentCompareTreeDTO root = new ComponentCompareTreeDTO();
        BeanUtils.copyProperties(tree, root);
        root.setMark(mark);
        for (ComponentDependencyTreeDO child : tree.getDependencies()) {
            ComponentCompareTreeDTO childAns = recursionMark(child, mark);
            if (childAns != null) {
                root.getDependencies().add(childAns);
            }
        }
        return root;
    }

    /**
     * 保存应用依赖平铺表
     *
     * @param applicationDependencyTreeDO 应用依赖信息树状
     */
    private List<DependencyTableDO> createapplicationDependencyTable(DependencyTreeDO applicationDependencyTreeDO) {
        // 先删除已有记录
        dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(applicationDependencyTreeDO.getGroupId(), applicationDependencyTreeDO.getArtifactId(), applicationDependencyTreeDO.getVersion());
        List<DependencyTableDO> result = new ArrayList<>();
        Queue<ComponentDependencyTreeDO> queue = new LinkedList<>(applicationDependencyTreeDO.getTree().getDependencies());
        while (!queue.isEmpty()) {
            DependencyTableDO applicationDependencyTableDO = new DependencyTableDO();
            applicationDependencyTableDO.setGroupId(applicationDependencyTreeDO.getGroupId());
            applicationDependencyTableDO.setArtifactId(applicationDependencyTreeDO.getArtifactId());
            applicationDependencyTableDO.setVersion(applicationDependencyTreeDO.getVersion());
            ComponentDependencyTreeDO componentDependencyTreeDO = Objects.requireNonNull(queue.poll());
            applicationDependencyTableDO.setCGroupId(componentDependencyTreeDO.getGroupId());
            applicationDependencyTableDO.setCArtifactId(componentDependencyTreeDO.getArtifactId());
            applicationDependencyTableDO.setCVersion(componentDependencyTreeDO.getVersion());
            applicationDependencyTableDO.setScope(componentDependencyTreeDO.getScope());
            applicationDependencyTableDO.setDepth(componentDependencyTreeDO.getDepth());
            applicationDependencyTableDO.setDirect(applicationDependencyTableDO.getDepth() == 1);
            applicationDependencyTableDO.setType(componentDependencyTreeDO.getType());
            result.add(applicationDependencyTableDO);
            queue.addAll(componentDependencyTreeDO.getDependencies());
        }
        return result;
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
