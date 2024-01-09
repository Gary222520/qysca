package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.example.UserRepository;
import nju.edu.cn.qysca.domain.example.ExampleUserMongo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class UserMongoTest
{
    @Autowired
    private UserRepository userRepository;
    @Test
    public void testSave(){
        ExampleUserMongo userMongo = new ExampleUserMongo();
        userMongo.setName("hbj");
        userRepository.save(userMongo);
        System.out.println(userMongo);
    }
}
