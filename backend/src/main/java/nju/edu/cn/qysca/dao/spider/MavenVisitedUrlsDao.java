package nju.edu.cn.qysca.dao.spider;

import nju.edu.cn.qysca.domain.spider.dos.MavenVisitedUrlsDO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MavenVisitedUrlsDao extends JpaRepository<MavenVisitedUrlsDO, String> {

    /**
     *
     * @param url url
     * @return MavenVisitedUrlsDO
     */
    MavenVisitedUrlsDO findByUrl(String url);
}
