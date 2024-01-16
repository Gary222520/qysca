package nju.edu.cn.qysca.dao.component;

import nju.edu.cn.qysca.domain.component.JavaCloseDependencyTreeDO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface JavaCloseDependencyTreeDao extends MongoRepository<JavaCloseDependencyTreeDO, String> {
}
