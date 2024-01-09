package nju.edu.cn.qysca.domain.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;


@Document("exampleUser")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ExampleUserMongo {

    @MongoId
    private String id;

    @Field("username")
    private String name;

}
