package nju.edu.cn.qysca.dao.components;

import nju.edu.cn.qysca.domain.components.JavaComponentDO;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface JavaComponentDao extends Neo4jRepository<JavaComponentDO, String> {
}
