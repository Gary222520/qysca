package nju.edu.cn.qysca.dao.bu;

import nju.edu.cn.qysca.domain.bu.dos.BuDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BuDao extends JpaRepository<BuDO, String> {

    BuDO findBuDOByName(String name);

    BuDO findByBid(String bid);
}
