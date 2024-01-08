package nju.edu.cn.qysca.dao.example;

import nju.edu.cn.qysca.domain.example.ExamplePersonNeo;
import org.springframework.data.neo4j.repository.Neo4jRepository;

public interface PersonRepository extends Neo4jRepository<ExamplePersonNeo, Long> {

}
