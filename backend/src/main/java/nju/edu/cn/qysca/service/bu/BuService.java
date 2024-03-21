package nju.edu.cn.qysca.service.bu;

import nju.edu.cn.qysca.domain.bu.dos.BuDO;

import java.util.List;

public interface BuService {
    /**
     * 创建BU
     * @param bid 部门编号
     * @param buName 名称
     * @return 返回创建Bu是否成功
     */
    Boolean createBu(String bid, String buName);

    /**
     *  查找BU
     * @param buName Bu名称
     * @return BuDO 部门信息
     */
    BuDO findBuByName(String buName);

    /**
     * 删除Bu
     * @param name 部门名称
     */
    void deleteBu(String name);

    /**
     * 列出所有Bu名称
     * @return List<String> Bu名称列表
     */
    List<String> listAllBu();
}
