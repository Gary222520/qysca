import spider.JavaOpenPomSpider;
import spider.Spider;

public class Main {
    public static void main(String[] args) {
        Spider spider = new JavaOpenPomSpider();

        String targetUrl = "https://repo1.maven.org/maven2/org/springframework/boot/";
        spider.crawlMany(targetUrl);

    }
}
