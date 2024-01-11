package nju.edu.cn.qysca.utils.idGenerator;

import org.springframework.data.neo4j.core.schema.IdGenerator;
import org.springframework.stereotype.Component;

@Component
public class Neo4jIdGenerator implements IdGenerator<String> {

    @Override
    public String generateId(String primaryLabel, Object entity) {
        return UUIDGenerator.getUUID();
    }
}
