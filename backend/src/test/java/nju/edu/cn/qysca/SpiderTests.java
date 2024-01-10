package nju.edu.cn.qysca;

import nju.edu.cn.qysca.utils.parser.PomFileInfo;
import nju.edu.cn.qysca.utils.parser.PomParser;
import nju.edu.cn.qysca.utils.spider.*;

public class SpiderTests {

    // @Test
    void pomSpiderTest(){
        // 这个url特殊规则比较多
        //String pomUrl = "https://repo1.maven.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/maven-compiler-plugin-3.8.1.pom";
        // 这个url比较常规
        // String pomUrl = "https://repo1.maven.org/maven2/garden/ephemeral/dsstore/dsstore-gradle-plugin/0.0.5/dsstore-gradle-plugin-0.0.5.pom";

        String pomUrl = "https://repo1.maven.org/maven2/org/sonatype/sisu/sisu-inject-plexus/1.4.2/sisu-inject-plexus-1.4.2.pom";
        PomParser.parsePomModel(PomSpider.getPomModel(pomUrl));

    }

    public static void main(String[] args) {
        SpiderTests spiderTests = new SpiderTests();
        spiderTests.pomSpiderTest();


    }
}
