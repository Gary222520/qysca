import dataAccess.DataAccessInterface;
import dataAccess.OpensourceComponentDODataAccess;
import spider.PomSpider;
import spider.Spider;

public class Main {
    public static void main(String[] args) {
        DataAccessInterface dataAccess = new OpensourceComponentDODataAccess();
        Spider spider = new PomSpider(dataAccess);

        String targetUrl = "https://repo1.maven.org/maven2/org/springframework/boot/";
        spider.crawlMany(targetUrl);

    }
}
