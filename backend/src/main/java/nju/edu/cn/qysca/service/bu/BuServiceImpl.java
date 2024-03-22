package nju.edu.cn.qysca.service.bu;

import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BuServiceImpl implements BuService{

    @Autowired
    private BuDao buDao;

    @Override
    @Transactional
    public Boolean createBu(String bid, String buName) {
        BuDO buDO = new BuDO();
        buDO.setBid(bid);
        buDO.setName(buName);
        buDao.save(buDO);
        return Boolean.TRUE;
    }

    @Override
    public BuDO findBuByName(String buName) {
        return buDao.findBuDOByName(buName);
    }

    @Override
    @Transactional
    public void deleteBu(String name) {
        buDao.deleteBuDOByName(name);
    }

    @Override
    public List<BuDO> listAllBu() {
        return buDao.findAll();
    }
}
