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
import nju.edu.cn.qysca.domain.user.dos.UserDO;
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
     * 分页获取应用信息
     *
     * @param number 页码
     * @param size   页大小
     * @return Page<ApplicationDO> 应用信息分页结果
     */
    @Override
    public Page<ApplicationDO> findApplicationPage(int number, int size) {
        //TODO: 权限检查
        Pageable pageable = PageRequest.of(number - 1, size);
        return applicationDao.findApplicationPage(pageable);
    }


    /**
     * 模糊查询应用名称
     *
     * @param name 应用名称
     * @return List<String> 模糊查询应用名称列表
     */
    @Override
    public List<String> searchApplicationName(String name) {
        return applicationDao.searchApplicationName(name);
    }

    /**
     * 根据名称查询应用 并返回应用的最新版本
     *
     * @param name 应用名称
     * @return ApplicationDO 应用信息
     */
    @Override
    public ApplicationDO findApplication(String name) {
        //TODO: 权限检查
        return applicationDao.findApplication(name);
    }


    /**
     * 根据应用信息返回子应用信息
     *
     * @param groupId    组织Id
     * @param artifactId 应用Id
     * @param version    版本
     * @return SubApplicationDTO 子应用信息
     */
    @Override
    public SubApplicationDTO findSubApplication(String groupId, String artifactId, String version) {
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
        List<ApplicationDO> subApplication = applicationDao.findSubApplication(applicationDO.getId());
        List<ComponentDO> subComponent = componentDao.findSubComponent(applicationDO.getId());
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
    public Boolean saveApplication(UserDO userDO, SaveApplicationDTO saveApplicationDTO) {
        //TODO: 权限检查
        ApplicationDO applicationDO = null;
        if (StringUtils.isEmpty(saveApplicationDTO.getId())) {
            applicationDO = new ApplicationDO();
            BeanUtils.copyProperties(saveApplicationDTO, applicationDO);
            applicationDO.setGroupId(userDO.getBu().getName());
            applicationDO.setArtifactId(saveApplicationDTO.getName());
            applicationDO.setCreator(userDO.getUid());
            applicationDO.setBu(userDO.getBu());
            applicationDO.setState("CREATED");
            applicationDO.setLock(false);
            applicationDO.setRelease(false);
        } else {
            //通过Bu Name 获得 groupId Application Name 获得ArtifactId
            applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(userDO.getBu().getName(), saveApplicationDTO.getName(), saveApplicationDTO.getVersion());
            applicationDO.setDescription(saveApplicationDTO.getDescription());
            applicationDO.setType(saveApplicationDTO.getType());
        }
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String timeStamp = dateFormat.format(now);
        applicationDO.setTime(timeStamp);
        applicationDao.save(applicationDO);
        return true;
    }


    /**
     * 在保存应用依赖时将应用状态改为RUNNING
     *
     * @param groupId    组织Id
     * @param artifactId 工件Id
     * @param version    版本
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
        //TODO: 权限检查
        try {
            DependencyTreeDO analyzedDependencyTreeDO = mavenService.dependencyTreeAnalysis(saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder(), "");
            if (!analyzedDependencyTreeDO.getGroupId().equals(saveApplicationDependencyDTO.getGroupId()) || !analyzedDependencyTreeDO.getArtifactId().equals(saveApplicationDependencyDTO.getArtifactId()) || !analyzedDependencyTreeDO.getVersion().equals(saveApplicationDependencyDTO.getVersion())) {
                throw new PlatformException(500, "上传pom文件非本项目");
            }
            DependencyTreeDO applicationDependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
            // 非叶子节点不能保存pom信息
            ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
            if (applicationDO.getChildApplication().length > 0 || applicationDO.getChildComponent().length > 0) {
                throw new PlatformException(500, "该应用不能保存pom信息");
            }
            if (applicationDependencyTreeDO == null) {
                applicationDependencyTreeDO = new DependencyTreeDO();
            }
            BeanUtils.copyProperties(analyzedDependencyTreeDO, applicationDependencyTreeDO);
            dependencyTreeDao.save(applicationDependencyTreeDO);
            // 批量更新依赖平铺表
            dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(saveApplicationDependencyDTO.getGroupId(), saveApplicationDependencyDTO.getArtifactId(), saveApplicationDependencyDTO.getVersion());
            List<DependencyTableDO> applicationDependencyTableDOS = mavenService.dependencyTableAnalysis(applicationDependencyTreeDO);
            dependencyTableDao.saveAll(applicationDependencyTableDOS);
            // 更改状态为SUCCESS
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
        //TODO: 权限检查
        //保存新应用
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
        if (!StringUtils.isEmpty(upgradeApplicationDTO.getParentGroupId()) && !StringUtils.isEmpty(upgradeApplicationDTO.getParentArtifactId()) && !StringUtils.isEmpty(upgradeApplicationDTO.getParentVersion())) {
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
    public List<ApplicationDO> deleteApplicationVersion(DeleteApplicationDTO deleteApplicationDTO) {
        //TODO: 权限检查
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(deleteApplicationDTO.getGroupId(), deleteApplicationDTO.getArtifactId(), deleteApplicationDTO.getVersion());
        //如果该项目已被锁定无法删除
        if (applicationDO.getLock()) {
            throw new PlatformException(500, "该项目已被锁定无法删除");
        }
        //如果该项目已被发布则会返回依赖该项目的列表 如果列表为空则删除
        if (applicationDO.getRelease()) {
            List<ApplicationDO> applicationDOList = applicationDao.findParentApplication(applicationDO.getId());
            if (applicationDOList.size() == 0) {
                applicationDao.delete(applicationDO);
                //删除组件库中信息 否则没有层次信息
                componentDao.deleteByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
                dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
                dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
            }
            return applicationDOList;
        }
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
        //TODO: 权限检查
        //判断是否是应用发布成的组件
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getGroupId(), applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
        ApplicationDO parentApplicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getParentGroupId(), applicationComponentDTO.getParentArtifactId(), applicationComponentDTO.getParentVersion());
        //锁定和发布状态不能添加组件
        if (parentApplicationDO.getRelease() || parentApplicationDO.getLock()) {
            throw new PlatformException(500, "该应用已锁定或已发布，无法添加组件");
        }
        //叶子节点无法添加组件
        DependencyTreeDO dependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getParentGroupId(), applicationComponentDTO.getParentArtifactId(), applicationComponentDTO.getParentVersion());
        if (dependencyTreeDO != null) {
            throw new PlatformException(500, "该应用已添加组件，无法手动添加");
        }
        //不是应用发布成的组件
        if (applicationDO == null) {
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildComponent()));
            ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getGroupId(), applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
            temp.add(componentDO.getId());
            parentApplicationDO.setChildComponent(temp.toArray(new String[temp.size()]));
        } else {
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildApplication()));
            temp.add(applicationDO.getId());
            parentApplicationDO.setChildApplication(temp.toArray(new String[temp.size()]));
        }
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
        //TODO: 权限检查
        ApplicationDO parentApplicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getParentGroupId(), applicationComponentDTO.getParentArtifactId(), applicationComponentDTO.getParentVersion());
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getGroupId(), applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
        //发布和锁定状态禁止删除
        if (parentApplicationDO.getLock() || parentApplicationDO.getRelease()) {
            throw new PlatformException(500, "该应用已发布或锁定，禁止删除");
        }
        //不是应用发布成的组件
        if (applicationDO == null) {
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildComponent()));
            ComponentDO componentDO = componentDao.findByGroupIdAndArtifactIdAndVersion(applicationComponentDTO.getGroupId(), applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
            temp.remove(componentDO.getId());
            parentApplicationDO.setChildComponent(temp.toArray(new String[temp.size()]));
        } else {
            ArrayList<String> temp = new ArrayList<>(Arrays.asList(parentApplicationDO.getChildApplication()));
            temp.remove(applicationDO.getId());
            parentApplicationDO.setChildApplication(temp.toArray(new String[temp.size()]));
        }
        applicationDao.save(parentApplicationDO);
        return Boolean.TRUE;
    }

    /**
     * 改变应用的锁定状态
     *
     * @param groupId    组织Id
     * @param artifactId 工件Id
     * @param version    版本号
     */
    @Override
    public void changeLockState(String groupId, String artifactId, String version) {
        //TODO: 权限检查
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(groupId, artifactId, version);
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
    public void changeReleaseState(ChangeReleaseStateDTO changeReleaseStateDTO) {
        //TODO: 权限检查
        ApplicationDO applicationDO = applicationDao.findByGroupIdAndArtifactIdAndVersion(changeReleaseStateDTO.getGroupId(), changeReleaseStateDTO.getArtifactId(), changeReleaseStateDTO.getVersion());
        if (applicationDO.getRelease()) {
            applicationDO.setRelease(false);
            componentDao.deleteByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
            dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
            dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
        } else {
            //发布应用成组件
            applicationDO.setRelease(true);
            ComponentDO componentDO = new ComponentDO();
            BeanUtils.copyProperties(applicationDO, componentDO);
            componentDO.setType(changeReleaseStateDTO.getType());
            //TODO: 通过应用发布的组件没有license等信息
            componentDao.save(componentDO);
            //根据结构生成依赖信息并保存
            DependencyTreeDO temp = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(applicationDO.getGroupId(), applicationDO.getArtifactId(), applicationDO.getVersion());
            if(temp == null) {
                DependencyTreeDO dependencyTreeDO = generateDependencyTree(applicationDO, changeReleaseStateDTO.getType());
                dependencyTreeDao.save(dependencyTreeDO);
                if (applicationDO.getLanguage().equals("java")) {
                    List<DependencyTableDO> dependencyTableDOS = mavenService.dependencyTableAnalysis(dependencyTreeDO);
                    dependencyTableDao.saveAll(dependencyTableDOS);
                }
            }
        }
        applicationDao.save(applicationDO);
    }

    /**
     * 根据结构生成依赖信息
     *
     * @param applicationDO 项目信息
     * @param type          组件类型
     * @return DependencyTreeDO 依赖树信息
     */
    private DependencyTreeDO generateDependencyTree(ApplicationDO applicationDO, String type) {
        DependencyTreeDO dependencyTreeDO = new DependencyTreeDO();
        BeanUtils.copyProperties(applicationDO, dependencyTreeDO);
        ComponentDependencyTreeDO componentDependencyTreeDO = new ComponentDependencyTreeDO();
        BeanUtils.copyProperties(applicationDO, componentDependencyTreeDO);
        componentDependencyTreeDO.setType(type);
        componentDependencyTreeDO.setScope("-");
        componentDependencyTreeDO.setDepth(0);
        List<ComponentDependencyTreeDO> componentDependencyTreeDOS = new ArrayList<>();
        for (String id : applicationDO.getChildApplication()) {
            ApplicationDO tempApplicationDO = applicationDao.findApplicationDOById(id);
            ComponentDependencyTreeDO temp = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(tempApplicationDO.getGroupId(), tempApplicationDO.getArtifactId(), tempApplicationDO.getVersion()).getTree();
            addDepth(temp);
            componentDependencyTreeDOS.add(temp);
        }
        for (String id : applicationDO.getChildComponent()) {
            ComponentDO tempComponentDO = componentDao.findComponentDOById(id);
            ComponentDependencyTreeDO temp = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(tempComponentDO.getGroupId(), tempComponentDO.getArtifactId(), tempComponentDO.getVersion()).getTree();
            addDepth(temp);
            componentDependencyTreeDOS.add(temp);
        }
        return dependencyTreeDO;
    }

    /**
     * 将树节点层数加1
     */
    private void addDepth(ComponentDependencyTreeDO componentDependencyTreeDO) {
        if(componentDependencyTreeDO == null){
            return;
        }
        componentDependencyTreeDO.setDepth(componentDependencyTreeDO.getDepth() + 1);
        for(ComponentDependencyTreeDO temp : componentDependencyTreeDO.getDependencies()) {
            addDepth(temp);
        }
    }
    /**
     * 分页获取指定应用的版本信息
     *
     * @param groupId    组织Id
     * @param artifactId 工件Id
     * @param number     页码
     * @param size       页大小
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
     * @param groupId    组织Id
     * @param artifactId 工件Id
     * @return Integer 扫描中组件的个数
     */
    @Override
    public Integer checkRunningApplication(String groupId, String artifactId) {
        return applicationDao.countByGroupIdAndArtifactIdAndState(groupId, artifactId, "RUNNING");
    }

    /**
     * 获取指定应用的所有版本列表
     *
     * @param groupId    组织Id
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
     * @param response             Http服务响应
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
     * @param response             Http服务响应
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
