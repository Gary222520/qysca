package nju.edu.cn.qysca.service.application;

import nju.edu.cn.qysca.auth.ContextUtil;
import nju.edu.cn.qysca.dao.bu.BuAppDao;
import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.dao.component.ComponentDao;
import nju.edu.cn.qysca.dao.component.DependencyTableDao;
import nju.edu.cn.qysca.dao.component.DependencyTreeDao;
import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.user.UserRoleDao;
import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.domain.component.dos.*;
import nju.edu.cn.qysca.domain.component.dtos.ComponentCompareTreeDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentDetailDTO;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import nju.edu.cn.qysca.domain.application.dos.*;
import nju.edu.cn.qysca.domain.application.dtos.*;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
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
    private BuDao buDao;

    @Autowired
    private BuAppDao buAppDao;

    @Autowired
    private UserRoleDao userRoleDao;

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
     * @param version    版本
     * @return SubApplicationDTO 子应用信息
     */
    @Override
    public SubApplicationDTO findSubApplication(String name, String version) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(name, version);
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
    public Boolean saveApplication(SaveApplicationDTO saveApplicationDTO) {
        ApplicationDO applicationDO = null;
        UserDO user = ContextUtil.getUserDO();
        BuDO buDO = buDao.findBuDOByName(saveApplicationDTO.getBuName());
        if(buDO == null){
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
        if(buAppDO == null) {
            buAppDO.setBid(buDO.getId());
            buAppDO.setAid(applicationDO.getId());
        }
        return true;
    }


    /**
     * 在保存应用依赖时将应用状态改为RUNNING
     *
     * @param name       应用名称
     * @param version    版本
     */
    @Override
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
            //部门名称为groupId 应用名称为ArtifactId
            DependencyTreeDO analyzedDependencyTreeDO = mavenService.dependencyTreeAnalysis(saveApplicationDependencyDTO.getFilePath(), saveApplicationDependencyDTO.getBuilder(), "");
            ApplicationDO temp  = applicationDao.findByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
            BuAppDO buAppDO = buAppDao.findByAid(temp.getId());
            BuDO buDO = buDao.findByBid(buAppDO.getBid());
            if (!analyzedDependencyTreeDO.getGroupId().equals((buDO.getName())) || !analyzedDependencyTreeDO.getArtifactId().equals(saveApplicationDependencyDTO.getName()) || !analyzedDependencyTreeDO.getVersion().equals(saveApplicationDependencyDTO.getVersion())) {
                throw new PlatformException(500, "上传pom文件非本项目");
            }
            // 非叶子节点不能保存pom信息
            ApplicationDO applicationDO = applicationDao.findByNameAndVersion(saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
            if (applicationDO.getChildApplication().length > 0 || applicationDO.getChildComponent().length > 0) {
                throw new PlatformException(500, "该应用不能保存pom信息");
            }
            DependencyTreeDO applicationDependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(buDO.getName(), saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
            if (applicationDependencyTreeDO == null) {
                applicationDependencyTreeDO = new DependencyTreeDO();
                applicationDependencyTreeDO.setGroupId(buDO.getName());
                applicationDependencyTreeDO.setArtifactId(saveApplicationDependencyDTO.getName());
                applicationDependencyTreeDO.setVersion(saveApplicationDependencyDTO.getVersion());
            }
            applicationDependencyTreeDO.setTree(analyzedDependencyTreeDO.getTree());
            dependencyTreeDao.save(applicationDependencyTreeDO);
            // 批量更新依赖平铺表
            dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(buDO.getName(), saveApplicationDependencyDTO.getName(), saveApplicationDependencyDTO.getVersion());
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
        applicationDao.save(newApplicationDO);
        BuAppDO newBuAppDO = new BuAppDO();
        BuAppDO oldBuAppDO = buAppDao.findByAid(oldApplicationDO.getId());
        newBuAppDO.setBid(oldBuAppDO.getBid());
        newBuAppDO.setAid(newApplicationDO.getId());
        buAppDao.save(newBuAppDO);
        //copy 角色表
        List<UserRoleDO> userRoleDOS = userRoleDao.findAllByAid(oldApplicationDO.getId());
        List<UserRoleDO> newUserRoleDOS = new ArrayList<>();
        for(UserRoleDO userRoleDO : userRoleDOS) {
            UserRoleDO newUserRoleDO = new UserRoleDO();
            BeanUtils.copyProperties(userRoleDO, newUserRoleDO);
            newUserRoleDO.setId(null);
            newUserRoleDO.setAid(newApplicationDO.getId());
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
            if (applicationDOList.size() == 0) {
                applicationDao.delete(applicationDO);
                //删除组件库中信息 否则没有层次信息
                BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
                BuDO buDO = buDao.findByBid(buAppDO.getBid());
                componentDao.deleteByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
                dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
                dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
                //删除BuApp中信息
                buAppDao.delete(buAppDO);
                //删除UserRole中信息
                userRoleDao.deleteAllByAid(applicationDO.getId());
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
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
        ApplicationDO parentApplicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getParentName(), applicationComponentDTO.getParentVersion());
        //锁定和发布状态不能添加组件
        if (parentApplicationDO.getRelease() || parentApplicationDO.getLock()) {
            throw new PlatformException(500, "该应用已锁定或已发布，无法添加组件");
        }
        //叶子节点无法添加组件
        BuAppDO buAppDO = buAppDao.findByAid(parentApplicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        DependencyTreeDO dependencyTreeDO = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationComponentDTO.getParentName(), applicationComponentDTO.getParentVersion());
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
        ApplicationDO parentApplicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getParentName(), applicationComponentDTO.getParentVersion());
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationComponentDTO.getArtifactId(), applicationComponentDTO.getVersion());
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
     * @param name 应用名称
     * @param version    版本号
     */
    @Override
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
    public void changeReleaseState(ChangeReleaseStateDTO changeReleaseStateDTO) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(changeReleaseStateDTO.getName(), changeReleaseStateDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        if (applicationDO.getRelease()) {
            applicationDO.setRelease(false);
            componentDao.deleteByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
            dependencyTreeDao.deleteByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
            dependencyTableDao.deleteAllByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
        } else {
            //发布应用成组件
            applicationDO.setRelease(true);
            ComponentDO componentDO = new ComponentDO();
            BeanUtils.copyProperties(applicationDO, componentDO);
            componentDO.setGroupId(buDO.getName());
            componentDO.setArtifactId(applicationDO.getName());
            componentDO.setType(changeReleaseStateDTO.getType());
            //TODO: 通过应用发布的组件没有license等信息
            componentDO.setUrl("-");
            componentDO.setDownloadUrl("-");
            componentDO.setPUrl("-");
            componentDao.save(componentDO);
            //根据结构生成依赖信息并保存
            DependencyTreeDO temp = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(buDO.getName(), applicationDO.getName(), applicationDO.getVersion());
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
    public DependencyTreeDO generateDependencyTree(ApplicationDO applicationDO, String type) {
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
            BuAppDO buAppDO = buAppDao.findByAid(tempApplicationDO.getId());
            BuDO buDO = buDao.findByBid(buAppDO.getBid());
            ComponentDependencyTreeDO temp = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(buDO.getName(), tempApplicationDO.getName(), tempApplicationDO.getVersion()).getTree();
            addDepth(temp);
            componentDependencyTreeDOS.add(temp);
        }
        for (String id : applicationDO.getChildComponent()) {
            ComponentDO tempComponentDO = componentDao.findComponentDOById(id);
            ComponentDependencyTreeDO temp = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(tempComponentDO.getGroupId(), tempComponentDO.getArtifactId(), tempComponentDO.getVersion()).getTree();
            addDepth(temp);
            componentDependencyTreeDOS.add(temp);
        }
        componentDependencyTreeDO.setDependencies(componentDependencyTreeDOS);
        dependencyTreeDO.setTree(componentDependencyTreeDO);
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
     * @param name 名称
     * @param number     页码
     * @param size       页大小
     * @return Page<ApplicationDO> 应用版本信息分页结果
     */
/*    @Override
    public Page<ApplicationDO> findApplicationVersionPage(String name, int number, int size) {
        // 数据库页号从0开始，需减1
        return applicationDao.findAllByName(name, PageRequest.of(number - 1, size, Sort.by(Sort.Order.desc("version").nullsLast())));
    }*/

    /**
     * 检查指定应用扫描中组件的个数
     *
     * @param groupId    组织Id
     * @param artifactId 工件Id
     * @return Integer 扫描中组件的个数
     */
/*    @Override
    public Integer checkRunningApplication(String groupId, String artifactId) {
        return applicationDao.countByGroupIdAndArtifactIdAndState(groupId, artifactId, "RUNNING");
    }*/

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
    public ApplicationDO findApplicationVersionInfo(ApplicationSearchDTO applicationSearchDTO) {
        return applicationDao.findByNameAndVersion(
                applicationSearchDTO.getName(),
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
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        return dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                buDO.getName(),
                applicationDO.getName(),
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
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationSearchPageDTO.getName(), applicationSearchPageDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        return dependencyTableDao.findByGroupIdAndArtifactIdAndVersion(
                buDO.getName(),
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
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        List<TableExcelBriefDTO> resList = dependencyTableDao.findTableListByGroupIdAndArtifactIdAndVersion(
                buDO.getName(), applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        String fileName = applicationSearchDTO.getName() + "-" + applicationSearchDTO.getVersion() + "-dependencyTable-brief";
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
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        List<TableExcelDetailDTO> resList = new ArrayList<>();
        String fileName = applicationSearchDTO.getName() + "-" + applicationSearchDTO.getVersion() + "-dependencyTable-detail";
        // 先获取依赖平铺的简明信息
        List<TableExcelBriefDTO> briefList = dependencyTableDao.findTableListByGroupIdAndArtifactIdAndVersion(
                buDO.getName(), applicationSearchDTO.getName(), applicationSearchDTO.getVersion());
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
        ApplicationDO application = applicationDao.findByNameAndVersion(versionCompareReqDTO.getName(), versionCompareReqDTO.getFromVersion());
        BuAppDO buApp = buAppDao.findByAid(application.getId());
        BuDO buDO = buDao.findByBid(buApp.getBid());
        DependencyTreeDO fromDependencyTree = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                buDO.getName(), versionCompareReqDTO.getName(), versionCompareReqDTO.getFromVersion());
        if (fromDependencyTree == null) {
            throw new PlatformException(500, "被对比的应用版本依赖树信息不存在");
        }
        DependencyTreeDO toDependencyTree = dependencyTreeDao.findByGroupIdAndArtifactIdAndVersion(
                buDO.getName(), versionCompareReqDTO.getName(), versionCompareReqDTO.getToVersion());
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
