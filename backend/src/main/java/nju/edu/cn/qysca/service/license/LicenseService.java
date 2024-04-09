package nju.edu.cn.qysca.service.license;

import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseBriefDTO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseConflictInfoDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LicenseService {
    /**
     * 查询许可证
     * @param queryName 待查询许可证名称
     * @return List<String> 匹配到的许可证名称，当精确匹配到时，只会返回一个许可证
     */
    List<String> searchLicense(String queryName);

    /**
     * 获取许可证冲突信息
     *
     * @param licenses 许可证列表
     * @return 许可证冲突信息
     */
    LicenseConflictInfoDTO getLicenseConflictInformation(List<LicenseDO> licenses);

    /**
     * 分页查询应用许可证列表
     * @param name 名称
     * @param version 版本
     * @return Page<LicenseBriefDTO> 许可证简明信息
     */
    Page<LicenseBriefDTO> getLicenseList(String name, String version, int page, int size);


    /**
     * 添加应用许可证
     * @param name 名称
     * @param version 版本
     * @param licenseName 许可证名称
     * @return 添加应用许可证是否成功
     */
    Boolean addAppLicense(String name, String version, String licenseName);

    /**
     * 删除应用许可证
     * @param name 名称
     * @param version 版本
     * @param licenseName 许可证名称
     * @return 删除许可证是否成功
     */
    Boolean deleteAppLicense(String name,String version, String licenseName);

    /**
     * 获取许可证详细信息
     * @param name 许可证名称
     * @return LicenseDO 许可证详细信息
     */
    LicenseDO getLicenseInfo(String name);

    /**
     * 许可证库界面查看详细信息
     * @param name 名称
     * @param page 页数
     * @param size 大小
     * @return Page<LicenseBriefDTO> 许可证库界面详细信息
     */
    Page<LicenseBriefDTO> getLicensePage(String name, int page, int size);
}
