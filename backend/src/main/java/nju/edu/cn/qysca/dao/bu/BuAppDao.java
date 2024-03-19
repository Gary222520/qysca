package nju.edu.cn.qysca.dao.bu;

import nju.edu.cn.qysca.domain.bu.dos.BuAppDO;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BuAppDao extends JpaRepository<BuAppDO, String> {

    BuAppDO findByAid(String aid);
}
