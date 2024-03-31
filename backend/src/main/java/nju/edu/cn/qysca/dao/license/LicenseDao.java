package nju.edu.cn.qysca.dao.license;


import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LicenseDao extends JpaRepository<LicenseDO, String> {

    LicenseDO findByName(String name);

    /**
     * 忽略大小写搜索
     * @param fullName 许可证全名
     * @return LicenseDO
     */
    LicenseDO findByFullNameIgnoreCase(String fullName);

    /**
     * 忽略大小写搜索
     * @param name 许可证名称
     * @return LicenseDO
     */
    LicenseDO findByNameIgnoreCase(String name);

    List<LicenseDO> findAllByNameLike(String name);
}
