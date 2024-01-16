package nju.edu.cn.qysca.dao.components;

import nju.edu.cn.qysca.domain.components.JavaCloseDependencyTreeDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseDependencyTreeDao extends MongoRepository<JavaCloseDependencyTreeDO, String> {
}
