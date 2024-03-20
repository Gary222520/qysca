package nju.edu.cn.qysca.dao.user;

import nju.edu.cn.qysca.domain.user.dos.PermissionDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PermissionDao extends JpaRepository<PermissionDO,String> {
    PermissionDO findByUrl(String url);
}
