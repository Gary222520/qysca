package nju.edu.cn.qysca;

import nju.edu.cn.qysca.utils.parser.PomFileInfo;
import nju.edu.cn.qysca.utils.parser.PomParser;
import nju.edu.cn.qysca.utils.spider.*;

public class SpiderTests {

    // @Test
    void pomSpiderTest(){

        //String pomUrl = "https://repo1.maven.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/maven-compiler-plugin-3.8.1.pom";
        String pomUrl = "https://repo1.maven.org/maven2/garden/ephemeral/dsstore/dsstore-gradle-plugin/0.0.5/dsstore-gradle-plugin-0.0.5.pom";
        PomFileInfo pomFileInfo = PomParser.parsePomDocument(PomSpider.getPomAsDocument(pomUrl), pomUrl);
        System.out.println(pomFileInfo.toString());

    }

//    public static void main(String[] args) {
//        SpiderTests spiderTests = new SpiderTests();
//        spiderTests.pomSpiderTest();
//    }
}
