package nju.edu.cn.qysca.dao.spider;

import nju.edu.cn.qysca.domain.spider.dos.PythonVisitedPackagesDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PythonVisitedPackagesDao extends JpaRepository<PythonVisitedPackagesDO, String> {
}
