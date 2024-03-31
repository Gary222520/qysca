package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
import nju.edu.cn.qysca.domain.user.dtos.UserBriefDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRoleDao extends JpaRepository<UserRoleDO, String> {


    /**
     * 查找成员角色信息
     * @param uid 成员编号
     * @return List<String> 角色信息
     */
    @Query("select rid from UserRoleDO where uid = :uid")
    List<String> findRidsByUid(String uid);

    /**
     * 根据成员编号查找角色信息
     * @param uid 成员编号
     * @return List<UserRoleDO> 角色信息
     */
    List<UserRoleDO> findByUid(String uid);

    /**
     * 删除应用的成员信息
     * @param aid 应用编号
     */
    void deleteAllByAid(String aid);


    /**
     * 根据uid、rid、bid、aid删除成员角色信息
     * @param uid 成员编号
     * @param rid 角色编号
     * @param bid 部门编号
     * @param aid 应用编号
     */
    void deleteByUidAndRidAndBidAndAid(String uid, String rid, String bid, String aid);

    /**
     * 查看成员部门信息
     * @param uid 成员编号
     * @return String 部门编号
     */
    @Query("SELECT bid from UserRoleDO where uid = :uid and rid = '-' and aid = '-'")
    String findUserBu(String uid);


    /**
     * 删除成员所有角色
     * @param uid 成员编号
     */
    void deleteAllByUid(String uid);

    /**
     * 列出部门所有成员
     * @param bid 部门编号
     * @return List<UserBriefDTO> 成员列表
     */
    @Query("select new nju.edu.cn.qysca.domain.user.dtos.UserBriefDTO(u.uid, u.name, '') from UserDO u, UserRoleDO r where r.bid = :bid and r.rid = '-' and r.aid = '-' and u.uid = r.uid")
    List<UserBriefDTO> listBuMember(String bid);

    /**
     * 根据应用编号查看角色信息
     * @param aid 应用编号
     * @return List<UserRoleDO> 角色信息
     */
    List<UserRoleDO> findAllByAid(String aid);


    /**
     * 查看部门的Bu Rep
     */
    @Query("select uid from UserRoleDO where bid = :bid and rid = :rid and aid = '-'")
    String findBuRep(String bid, String rid);

    @Query("select new nju.edu.cn.qysca.domain.user.dtos.UserBriefDTO(u.uid, u.name, r.name) from UserDO u, UserRoleDO ur, RoleDO r where ur.aid = :aid and u.uid = ur.uid and ur.rid = r.id")
    List<UserBriefDTO> listAppMember(String aid);

    @Query("select uid from UserRoleDO where bid = :bid and rid = :rid")
    List<String> findBuPO(String bid, String rid);

}
