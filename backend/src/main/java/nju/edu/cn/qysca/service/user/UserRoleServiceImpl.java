package nju.edu.cn.qysca.service.user;

import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.bu.BuAppDao;
import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.dao.user.RoleDao;
import nju.edu.cn.qysca.dao.user.UserRoleDao;
import nju.edu.cn.qysca.domain.application.dos.ApplicationDO;
import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import nju.edu.cn.qysca.domain.bu.dtos.BuMemberDTO;
import nju.edu.cn.qysca.domain.user.dos.RoleDO;
import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
import nju.edu.cn.qysca.domain.user.dtos.ApplicationMemberDTO;
import nju.edu.cn.qysca.domain.bu.dtos.BuRepDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class UserRoleServiceImpl implements UserRoleService{

    @Autowired
    private ApplicationDao applicationDao;

    @Autowired
    private BuAppDao buAppDao;

    @Autowired
    private BuDao buDao;

    @Autowired
    private UserRoleDao userRoleDao;

    @Autowired
    private RoleDao roleDao;

    @Override
    public void addMember(ApplicationMemberDTO applicationMemberDTO) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationMemberDTO.getName(), applicationMemberDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setAid(applicationDO.getId());
        userRoleDO.setBid(buDO.getBid());
        userRoleDO.setUid(applicationMemberDTO.getUid());
        RoleDO roleDO = roleDao.findByName(applicationMemberDTO.getRole());
        userRoleDO.setRid(roleDO.getId());
        userRoleDao.save(userRoleDO);
    }

    @Override
    public void deleteMember(ApplicationMemberDTO applicationMemberDTO) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationMemberDTO.getName(), applicationMemberDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        RoleDO roleDO = roleDao.findByName(applicationMemberDTO.getRole());
        userRoleDao.deleteByUidAndRidAndBidAndAid(applicationMemberDTO.getUid(), roleDO.getId(), buDO.getBid(), applicationDO.getId());
    }


    @Override
    public void addBuRep(BuRepDTO buRepDTO) {
        BuDO buDO = buDao.findBuDOByName(buRepDTO.getBuName());
        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUid(buRepDTO.getUid());
        RoleDO roleDO = roleDao.findByName("BU Rep");
        userRoleDO.setRid(roleDO.getId());
        userRoleDO.setBid(buDO.getBid());
        userRoleDO.setUid("-");
        userRoleDao.save(userRoleDO);
    }

    @Override
    public void deleteBuRep(BuRepDTO buRepDTO) {
        BuDO buDO = buDao.findBuDOByName(buRepDTO.getBuName());
        RoleDO roleDO = roleDao.findByName("BU Rep");
        userRoleDao.deleteByUidAndRidAndBidAndAid(buRepDTO.getUid(), roleDO.getId(), buDO.getBid(), "-");
    }


    @Override
    public void addBuMember(BuMemberDTO buMemberDTO) {
        BuDO buDO = buDao.findBuDOByName(buMemberDTO.getBuName());
        userRoleDao.deleteByUidAndRidAndBidAndAid(buMemberDTO.getUid(), "-", buDO.getBid(), "-");
    }
}
