package nju.edu.cn.qysca.auth;

import nju.edu.cn.qysca.dao.application.ApplicationDao;
import nju.edu.cn.qysca.dao.bu.BuDao;
import nju.edu.cn.qysca.dao.user.PermissionDao;
import nju.edu.cn.qysca.dao.user.RoleDao;
import nju.edu.cn.qysca.dao.user.RolePermissionDao;
import nju.edu.cn.qysca.dao.user.UserRoleDao;
import nju.edu.cn.qysca.domain.user.dos.PermissionDO;
import nju.edu.cn.qysca.domain.user.dos.UserDO;
import nju.edu.cn.qysca.domain.user.dos.UserRoleDO;
import nju.edu.cn.qysca.domain.user.dtos.ApplicationMemberDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("my")
public class MyExpressionRoot {
    @Autowired
    UserRoleDao userRoleDao;

    @Autowired
    RolePermissionDao rolePermissionDao;

    @Autowired
    PermissionDao permissionDao;

    @Autowired
    RoleDao roleDao;

    @Autowired
    BuDao buDao;

    @Autowired
    ApplicationDao applicationDao;

    /**
     * 根据所需的url判断用户是否有权限访问
     * @param url 所需访问的url
     * @return 是否有权限
     */
    public boolean checkAuth(String url){
        // 查用户对应的角色
        UserDO userDO=ContextUtil.getUserDO();
        if(userDO==null){
            return false;
        }
        List<String> ur=userRoleDao.findRidsByUid(userDO.getUid());
        if(ur==null){
            return  false;
        }
        // 查权限对应的角色
        PermissionDO permissionDO=permissionDao.findByUrl(url);
        if(permissionDO==null){
            return false;
        }
        List<String> pr=rolePermissionDao.findRidsByPid(permissionDO.getId());
        if(pr==null){
            return false;
        }
        // 判断用户是否具备对应权限所需的角色
        ur.retainAll(pr);
        return ur.size()>0;
    }


    /**
     * 根据所需的url和新增用户信息判断用户是否有权限访问
     * @param applicationMemberDTO 新增用户信息
     * @param url 所需访问的url
     * @return 是否有权限
     */
    public boolean checkAuthAddAppMember(ApplicationMemberDTO applicationMemberDTO,String url){
        // 查用户对应的角色
        UserDO userDO=ContextUtil.getUserDO();
        if(userDO==null){
            return false;
        }
        List<String> ur=userRoleDao.findRidsByUid(userDO.getUid());
        if(ur==null){
            return  false;
        }
        // 获取角色名称
        List<String> roles=new ArrayList<>();
        for(String rid:ur){
            roles.add(roleDao.findNameById(rid));
        }
        // 判断用户有无创建应用成员角色的权限
        switch (applicationMemberDTO.getRole()) {
            case "Bu PO":
                if (!roles.contains("Admin")) {
                    return false;
                }
                break;
            case "App Leader":
                if (!roles.contains("Bu PO") && !roles.contains("App Leader")) {
                    return false;
                }
                break;
            case "App Member":
                if(!roles.contains("App Leader")){
                    return false;
                }
                break;
            default:
                return false;
        }
        // 查权限对应的角色
        PermissionDO permissionDO=permissionDao.findByUrl(url);
        if(permissionDO==null){
            return false;
        }
        List<String> pr=rolePermissionDao.findRidsByPid(permissionDO.getId());
        if(pr==null){
            return false;
        }
        // 判断用户是否具备对应权限所需的角色
        ur.retainAll(pr);
        return ur.size()>0;
    }
}
