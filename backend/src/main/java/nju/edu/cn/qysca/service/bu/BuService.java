package nju.edu.cn.qysca.service.bu;

import nju.edu.cn.qysca.domain.bu.dos.BuDO;

public interface BuService {
    /**
     * 创建BU
     * @param buName 名称
     * @return 返回创建Bu是否成功
     */
    Boolean createBu(String buName);

    /**
     *  查找BU
     * @param buName Bu名称
     * @return BuDO 部门信息
     */
    BuDO findBuByName(String buName);
}
