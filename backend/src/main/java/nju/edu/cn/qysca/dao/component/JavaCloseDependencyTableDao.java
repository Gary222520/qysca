package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.JavaCloseDependencyTableDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseDependencyTableDao extends MongoRepository<JavaCloseDependencyTableDO, String> {
}
