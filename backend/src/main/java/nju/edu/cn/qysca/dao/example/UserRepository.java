package nju.edu.cn.qysca.dao.example;

import nju.edu.cn.qysca.domain.example.ExampleUserMongo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<ExampleUserMongo, String> {


}
