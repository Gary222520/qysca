package nju.edu.cn.qysca;

import nju.edu.cn.qysca.utils.parser.PomFileInfo;
import nju.edu.cn.qysca.utils.parser.PomParser;
import nju.edu.cn.qysca.utils.spider.*;

public class SpiderTests {

    // @Test
    void pomSpiderTest(){
        // 这个url特殊规则比较多
        // String pomUrl = "https://repo1.maven.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/maven-compiler-plugin-3.8.1.pom";
        // 这个url比较常规
        // String pomUrl = "https://repo1.maven.org/maven2/garden/ephemeral/dsstore/dsstore-gradle-plugin/0.0.5/dsstore-gradle-plugin-0.0.5.pom";
        // 这个url有问题，找不到project > properties > slf4j.version， 却能找到project > properties > forgeReleaseUrl， 怀疑是前者含有符号的原因
        String pomUrl = "https://repo1.maven.org/maven2/org/sonatype/sisu/sisu-parent/1.4.2/sisu-parent-1.4.2.pom";
        PomFileInfo pomFileInfo = PomParser.parsePomDocument(PomSpider.getPomAsDocument(pomUrl), pomUrl);
        System.out.println(pomFileInfo.toString());

    }

    public static void main(String[] args) {
        SpiderTests spiderTests = new SpiderTests();
        spiderTests.pomSpiderTest();


    }
}
