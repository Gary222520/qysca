package nju.edu.cn.qysca.dao.bu;

import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuAppDao extends JpaRepository<BuAppDO, String> {

    /**
     * 查找应用所属部门
     * @param aid 应用编号
     * @return BuAppDO 应用所属部门信息
     */
    BuAppDO findByAid(String aid);

}
