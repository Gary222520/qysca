package nju.edu.cn.qysca.dao.project;

import nju.edu.cn.qysca.domain.project.dos.ProjectInfoDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectInfoDao extends MongoRepository<ProjectInfoDO, String> {

    void deleteByName(String name);

}
