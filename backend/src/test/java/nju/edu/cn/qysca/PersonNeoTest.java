package nju.edu.cn.qysca;

import nju.edu.cn.qysca.dao.example.PersonRepository;
import nju.edu.cn.qysca.domain.example.ExamplePersonNeo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class PersonNeoTest {
    @Autowired
    private PersonRepository personRepository;

    @Test
    public void test() {
        ExamplePersonNeo personNeo = new ExamplePersonNeo();
        personNeo.setName("hbj");
        personRepository.save(personNeo);
        System.out.println(personNeo);
    }
}
