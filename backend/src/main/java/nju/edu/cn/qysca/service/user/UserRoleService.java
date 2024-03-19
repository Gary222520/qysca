package nju.edu.cn.qysca.service.user;

import nju.edu.cn.qysca.domain.user.dtos.ApplicationMemberDTO;

public interface UserRoleService {

    /**
     * 在应用添加成员
     * @param applicationMemberDTO 应用成员信息
     */
    void addMember(ApplicationMemberDTO applicationMemberDTO);

    /**
     * 在应用删除成员
     * @param applicationMemberDTO 应用成员信息
     */
    void deleteMember(ApplicationMemberDTO applicationMemberDTO);

    /**
     * 在应用添加APP Leader
     * @param applicationMemberDTO 应用成员信息
     */
    void addLeader(ApplicationMemberDTO applicationMemberDTO);

    /**
     * 在应用删除APP Leader
     * @param applicationMemberDTO 应用成员信息
     */
    void deleteLeader(ApplicationMemberDTO applicationMemberDTO);

    /**
     * 在应用添加BU PO
     * @param applicationMemberDTO 应用成员信息
     */
    void addBuPO(ApplicationMemberDTO applicationMemberDTO);

    /**
     * 在应用删除BU PO
     * @param applicationMemberDTO 应用成员信息
     */
    void deleteBuPO(ApplicationMemberDTO applicationMemberDTO);

    /**
     * 在应用添加BU Rep
     * @param applicationMemberDTO 应用成员信息
     */
    void addBuRep(ApplicationMemberDTO applicationMemberDTO);

    /**
     * 在应用删除BU Rep
     * @param applicationMemberDTO 应用成员信息
     */
    void deleteBuRep(ApplicationMemberDTO applicationMemberDTO);
}
