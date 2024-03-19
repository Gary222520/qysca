package nju.edu.cn.qysca.service.user;

import nju.edu.cn.qysca.domain.bu.dtos.BuMemberDTO;
import nju.edu.cn.qysca.domain.user.dtos.ApplicationMemberDTO;
import nju.edu.cn.qysca.domain.bu.dtos.BuRepDTO;

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
     * 在应用添加BU Rep
     * @param buRepDTO 部门代表信息
     */
    void addBuRep(BuRepDTO buRepDTO);

    /**
     * 在应用删除BU Rep
     * @param buRepDTO 部门代表信息
     */
    void deleteBuRep(BuRepDTO buRepDTO);

    /**
     * 在部门中增加成员
     * @param buMemberDTO
     */
    void addBuMember(BuMemberDTO buMemberDTO);
}
