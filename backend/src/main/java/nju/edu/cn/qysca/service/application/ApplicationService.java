package nju.edu.cn.qysca.service.application;

import nju.edu.cn.qysca.domain.application.dos.AppDependencyTreeDO;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.application.dtos.*;
import nju.edu.cn.qysca.domain.component.dtos.ComponentTableDTO;
import org.springframework.data.domain.Page;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


public interface ApplicationService {

    /**
     * 分页获取根应用信息
     *
     * @param number 页码
     * @param size   页大小
     * @return Page<ApplicationDO> 根应用信息分页结果
     */
    Page<ApplicationDO> findApplicationPage(int number, int size);

    /**
     * 模糊查询应用名称
     * @param name 应用名称
     * @return List<String> 模糊查询应用名称列表
     */
    List<String> searchApplicationName(String name);


    /**
     * 根据名称查询应用 并返回应用的最新版本
     * @param name 应用名称
     * @return ApplicationDO 应用信息
     */
    ApplicationDO findApplication(String name);


    /**
     * 根据应用Id查询子应用信息
     * @param name 名称
     * @param version 应用版本
     * @return SubApplicationDTO 子应用信息
     */
    SubApplicationDTO findSubApplication(String name, String version);

    /**
     * 新增/更新应用信息
     * @param saveApplicationDTO 保存应用接口信息
     * @return Boolean 应用是否保存成功
     */
    Boolean saveApplication(SaveApplicationDTO saveApplicationDTO);


    /**
     * 在保存应用依赖时将应用状态变为RUNNING
     * @param name 应用名称
     * @param version 应用版本
     * @param state 应用状态
     */
    void changeApplicationState(String name, String version, String state);

    /**
     * 新增/更新应用依赖信息
     *
     * @param saveApplicationDependencyDTO 保存应用接口信息
     */
    void saveApplicationDependency(SaveApplicationDependencyDTO saveApplicationDependencyDTO);


    /**
     * 升级应用
     *
     * @param upgradeApplicationDTO 升级应用接口信息
     * @return 升级应用是否成功
     */
    Boolean upgradeApplication(UpgradeApplicationDTO upgradeApplicationDTO);


    /**
     * 删除应用的某个版本
     * @param deleteApplicationDTO 删除应用接口信息
     * @return 删除应用的某个版本是否成功
     */
    List<ApplicationDO> deleteApplicationVersion(DeleteApplicationDTO deleteApplicationDTO);

    /**
     * 向应用中添加组件
     * @param applicationComponentDTO 应用组件接口
     * @return 向应用中添加组件是否成功
     */
    Boolean saveApplicationComponent(ApplicationComponentDTO applicationComponentDTO);

    /**
     * 删除应用中某个组件
     * @param applicationComponentDTO 应用组件接口
     * @return 删除应用中组件是否成功
     */
    Boolean deleteApplicationComponent(ApplicationComponentDTO applicationComponentDTO);


    /**
     * 获取指定应用的所有版本列表
     *
     * @param name 应用名称
     * @return List<String> 版本列表
     */
    List<String> getVersionsList(String name);

    /**
     * 获取指定应用指定版本的详细信息
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @return ApplicationDO 应用版本的详细信息
     */
    ApplicationDetailDTO findApplicationVersionInfo(ApplicationSearchDTO applicationSearchDTO);

    /**
     * 查询应用依赖树信息
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @return JavaDependencyTreeDO 应用依赖树信息
     */
    AppDependencyTreeDO findApplicationDependencyTree(ApplicationSearchDTO applicationSearchDTO);

    /**
     * 分页查询应用依赖平铺信息
     *
     * @param applicationSearchPageDTO 带分页应用版本搜索信息
     * @return Page<ComponentTableDTO> 依赖平铺信息分页
     */
    Page<ComponentTableDTO> findApplicationDependencyTable(ApplicationSearchPageDTO applicationSearchPageDTO);

    /**
     * 导出应用依赖平铺信息（简明）Excel
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @param response         Http服务响应
     */
    void exportTableExcelBrief(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response);

    /**
     * 导出应用依赖平铺信息（详细）Excel
     *
     * @param applicationSearchDTO 应用版本搜索信息
     * @param response         Http服务响应
     */
    void exportTableExcelDetail(ApplicationSearchDTO applicationSearchDTO, HttpServletResponse response);

    /**
     * 生成应用版本对比树
     *
     * @param versionCompareReqDTO 需对比的应用版本
     * @return VersionCompareTreeDTO 对比树
     */
    VersionCompareTreeDTO getApplicationVersionCompareTree(VersionCompareReqDTO versionCompareReqDTO);

    /**
     * 改变应用锁定状态
     * @param name 名称
     * @param version 版本号
     */
    void changeLockState(String name, String version);

    /**
     * 改变应用发布状态
     * @param changeReleaseStateDTO 应用发布状态
     */
    List<ApplicationDO> changeReleaseState(ChangeReleaseStateDTO changeReleaseStateDTO);

    /**
     * 根据结构生成依赖信息
     *
     * @param applicationDO 项目信息
     * @param type          组件类型
     * @return JavaDependencyTreeDO 依赖树信息
     */
    AppDependencyTreeDO generateDependencyTree(ApplicationDO applicationDO, String type);
}
