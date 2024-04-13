package nju.edu.cn.qysca.dao.spider;

import nju.edu.cn.qysca.domain.spider.dos.NpmVisitedPackagesDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NpmVisitedPackagesDao extends JpaRepository<NpmVisitedPackagesDO, String> {
    Boolean existsByName(String name);
}
