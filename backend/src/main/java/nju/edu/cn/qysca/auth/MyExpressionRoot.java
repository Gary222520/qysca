package nju.edu.cn.qysca.auth;

import nju.edu.cn.qysca.domain.user.dos.UserDO;
import org.springframework.stereotype.Component;

@Component("my")
public class MyExpressionRoot {
    public boolean checkAuth(String uid){
        UserDO userDO=ContextUtil.getUserDO();
        return uid.equals(userDO.getUid());
    }
}
