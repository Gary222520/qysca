import dataAccess.MongoDBAccess;
import domain.component.JavaOpenComponentDO;
import domain.component.JavaOpenComponentInformationDO;
import domain.component.JavaOpenDependencyTableDO;
import domain.component.JavaOpenDependencyTreeDO;
import org.junit.Test;
import spider.JavaOpenPomSpider;

public class JavaOpenPomSpiderTest {

    @Test
    public void testOpenPomSpider() {
        String groupId = "junit";
        String artifactId = "junit";
        String version = "4.12";
        JavaOpenPomSpider spider = new JavaOpenPomSpider();
        JavaOpenComponentInformationDO javaOpenComponentInformationDO = spider.crawlWithDependencyByGav(groupId, artifactId, version);
        spider.flush();

        MongoDBAccess<JavaOpenComponentDO> mongoDBAccess1 = new MongoDBAccess<>("java_component_open_detail", JavaOpenComponentDO.class);
        MongoDBAccess<JavaOpenDependencyTreeDO> mongoDBAccess2 = new MongoDBAccess<>("java_component_open_dependency_tree", JavaOpenDependencyTreeDO.class);
        MongoDBAccess<JavaOpenDependencyTableDO> mongoDBAccess3 = new MongoDBAccess<>("java_component_open_dependency_table", JavaOpenDependencyTableDO.class);

        JavaOpenComponentDO javaOpenComponentDO = mongoDBAccess1.readByGAV(groupId, artifactId, version);
        JavaOpenDependencyTreeDO javaOpenDependencyTreeDO = mongoDBAccess2.readByGAV(groupId, artifactId, version);
        JavaOpenDependencyTableDO javaOpenDependencyTableDO = mongoDBAccess3.readByGAV("org.hamcrest", "hamcrest-core", "1.3");

        assert javaOpenComponentDO.getName().equals("JUnit");
        assert javaOpenDependencyTreeDO.getTree().getDependencies().size() == 1;
        assert javaOpenDependencyTableDO.getDirect().equals(true);
    }
}
