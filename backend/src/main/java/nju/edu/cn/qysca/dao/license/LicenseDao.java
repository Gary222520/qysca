package nju.edu.cn.qysca.dao.license;


import nju.edu.cn.qysca.domain.license.dos.LicenseDO;
import nju.edu.cn.qysca.domain.license.dtos.LicenseBriefDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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


    /**
     * 查询应用的license
     * @param licenses 应用的license信息
     * @param pageable 分页信息
     * @return Page<LicenseDO> 许可证信息
     */
    @Query(value = "select * from plt_license where name in (:licenses) ORDER BY ARRAY_POSITION(ARRAY['high', 'medium', 'low'], risk_level), name asc",
            countQuery = "select count (*) from plt_license where name in (:licenses)",
            nativeQuery = true)
    Page<LicenseDO> getLicenseList(@Param("licenses") List<String> licenses, Pageable pageable);

    /**
     * 许可证库界面的查询
     * @param name 许可证名称
     * @param pageable 分页信息
     * @return Page<LicenseDO> 许可证信息
     */
    @Query(value = "select * from plt_license where (:name = '' or :name = name) ORDER BY ARRAY_POSITION(ARRAY['high', 'medium', 'low'], risk_level), name asc",
               countQuery = "select count (*) from plt_license where (:name = '' or :name = name)",
               nativeQuery = true)
    Page<LicenseDO> getLicensePage(@Param("name") String name, Pageable pageable);
}
