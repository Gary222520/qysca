package nju.edu.cn.qysca.domain.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@Node("Person")
@AllArgsConstructor
@NoArgsConstructor
public class ExamplePersonNeo {
    @Id
    @GeneratedValue
    private Long id;

    @Property
    private String name;
}
