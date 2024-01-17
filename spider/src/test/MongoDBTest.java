import dataAccess.MongoDBAccess;
import domain.component.DeveloperDO;
import domain.component.JavaOpenComponentDO;
import org.junit.Test;
import util.idGenerator.UUIDGenerator;

import java.util.ArrayList;
import java.util.List;

public class MongoDBTest {

    @Test
    public void testMongoDB() {

        MongoDBAccess<JavaOpenComponentDO> dbAccess = new MongoDBAccess<JavaOpenComponentDO>("test", JavaOpenComponentDO.class);

        List<JavaOpenComponentDO> list = new ArrayList<>();

        JavaOpenComponentDO do1 = new JavaOpenComponentDO();
        do1.setId(UUIDGenerator.getUUID());
        do1.setGroupId("groupId1");
        do1.setArtifactId("artifactId1");
        do1.setVersion("1.0.0");
        do1.setDownloadUrl("http://www.example.com/download1");
        do1.setLanguage("Java");

        List<DeveloperDO> developers = new ArrayList<>();
        DeveloperDO developerDO1 = new DeveloperDO();
        developerDO1.setDeveloperName("John Doe");
        developerDO1.setDeveloperEmail("123456@example.com");
        DeveloperDO developerDO2 = new DeveloperDO();
        developerDO1.setDeveloperName("xiao li");
        developerDO1.setDeveloperEmail("111111@example.com");
        developers.add(developerDO1);
        developers.add(developerDO2);

        do1.setDevelopers(developers);
        list.add(do1);

        dbAccess.writeMany(list);

        JavaOpenComponentDO get = dbAccess.readByGAV("groupId1", "artifactId1", "1.0.0");
        assert get.getDownloadUrl().equals(do1.getDownloadUrl());

    }

}
