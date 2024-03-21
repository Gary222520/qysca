package nju.edu.cn.qysca.dao.bu;

import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BuDao extends JpaRepository<BuDO, String> {

    /**
     * 根据名称查找部门信息
     * @param name 部门名称
     * @return BuDO 部门信息
     */
    BuDO findBuDOByName(String name);

    /**
     * 根据bid查找部门信息
     * @param bid 部门编号
     * @return BuDO 部门信息
     */
    BuDO findByBid(String bid);

    /**
     * 根据名称删除部门
     * @param name 部门名称
     */
    void deleteBuDOByName(String name);
}
