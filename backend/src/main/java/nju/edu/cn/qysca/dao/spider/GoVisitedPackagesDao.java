package nju.edu.cn.qysca.dao.spider;

import nju.edu.cn.qysca.domain.spider.dos.GoVisitedPackagesDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoVisitedPackagesDao extends JpaRepository<GoVisitedPackagesDO, String> {

    GoVisitedPackagesDO findByNameAndVersion(String name, String version);
}
