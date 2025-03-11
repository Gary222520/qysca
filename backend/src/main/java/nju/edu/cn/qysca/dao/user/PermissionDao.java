package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.PermissionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionDao extends JpaRepository<PermissionDO,String> {
    /**
     * 根据url查找权限
     * @param url 路由信息
     * @return PermissionDO 权限信息
     */
    PermissionDO findByUrl(String url);
}
