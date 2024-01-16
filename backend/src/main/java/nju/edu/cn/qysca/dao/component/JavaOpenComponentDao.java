package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.JavaOpenComponentDO;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JavaOpenComponentDao extends MongoRepository<JavaOpenComponentDO, String> {
}
