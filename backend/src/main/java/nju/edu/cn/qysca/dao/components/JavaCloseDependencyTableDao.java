package nju.edu.cn.qysca.dao.components;

import nju.edu.cn.qysca.domain.components.JavaCloseDependencyTableDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseDependencyTableDao extends MongoRepository<JavaCloseDependencyTableDO, String> {
}
