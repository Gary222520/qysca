package nju.edu.cn.qysca.service.license;

import nju.edu.cn.qysca.domain.license.dos.LicenseDO;

import java.util.List;

public interface LicenseService {
    /**
     * 查询许可证
     * @param queryName 待查询许可证名称
     * @return List<LicenseDO> 匹配到的许可证，当精确匹配到时，只会返回一个许可证
     */
    List<LicenseDO> searchLicense(String queryName);
}
