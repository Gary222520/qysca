import dataAccess.DataAccess;
import domain.component.JavaOpenComponentDO;
import spider.JavaOpenPomSpider;
import spider.Spider;

public class Main {
    public static void main(String[] args) {
        DataAccess<JavaOpenComponentDO> dataAccess = new DataAccess<JavaOpenComponentDO>("java_component_open",JavaOpenComponentDO.class);
        Spider spider = new JavaOpenPomSpider();

        String targetUrl = "https://repo1.maven.org/maven2/org/springframework/boot/";
        spider.crawlMany(targetUrl, dataAccess);

    }
}
