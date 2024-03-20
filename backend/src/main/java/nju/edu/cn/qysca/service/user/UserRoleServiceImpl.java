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
import nju.edu.cn.qysca.domain.user.dtos.UserBriefDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserRoleServiceImpl implements UserRoleService {

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

    /**
     * 在角色表里面添加成员 可以添加的角色为Bu PO, App Member, App Leader
     * @param applicationMemberDTO 应用成员信息
     */
    @Override
    @Transactional
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

    /**
     * 在角色表中删除成员
     * @param applicationMemberDTO 应用成员信息
     */
    @Override
    @Transactional
    public void deleteMember(ApplicationMemberDTO applicationMemberDTO) {
        ApplicationDO applicationDO = applicationDao.findByNameAndVersion(applicationMemberDTO.getName(), applicationMemberDTO.getVersion());
        BuAppDO buAppDO = buAppDao.findByAid(applicationDO.getId());
        BuDO buDO = buDao.findByBid(buAppDO.getBid());
        RoleDO roleDO = roleDao.findByName(applicationMemberDTO.getRole());
        userRoleDao.deleteByUidAndRidAndBidAndAid(applicationMemberDTO.getUid(), roleDO.getId(), buDO.getBid(), applicationDO.getId());
    }


    /**
     * 添加Bu Rep
     * @param buRepDTO 部门代表信息
     */
    @Override
    @Transactional
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

    /**
     * 删除Bu Rep
     * @param buRepDTO 部门代表信息
     */
    @Transactional
    @Override
    public void deleteBuRep(BuRepDTO buRepDTO) {
        BuDO buDO = buDao.findBuDOByName(buRepDTO.getBuName());
        RoleDO roleDO = roleDao.findByName("BU Rep");
        userRoleDao.deleteByUidAndRidAndBidAndAid(buRepDTO.getUid(), roleDO.getId(), buDO.getBid(), "-");
    }


    /**
     * 部门添加成员
     * @param buMemberDTO 部门成员信息
     */
    @Override
    @Transactional
    public void addBuMember(BuMemberDTO buMemberDTO) {
        BuDO buDO = buDao.findBuDOByName(buMemberDTO.getBuName());
        UserRoleDO userRoleDO = new UserRoleDO();
        userRoleDO.setUid(buMemberDTO.getUid());
        userRoleDO.setBid(buDO.getBid());
        userRoleDO.setRid("-");
        userRoleDO.setAid("-");
        userRoleDao.save(userRoleDO);
    }

    /**
     * 列出部门所有成员
     * @param name 部门名称
     * @return List<UserBriefDTO> 部门所有成员信息
     */

    @Override
    public List<UserBriefDTO> listBuMember(String name) {
        BuDO buDO = buDao.findBuDOByName(name);
        return userRoleDao.listBuMember(buDO.getBid());
    }

    /**
     * 删除部门成员
     * @param name 部门名称
     * @param uid 成员编号
     */
    @Override
    @Transactional
    public void deleteBuMember(String name, String uid) {
        BuDO buDO = buDao.findBuDOByName(name);
        userRoleDao.deleteByUidAndRidAndBidAndAid(uid, "-", buDO.getBid(), "-");
    }
}
