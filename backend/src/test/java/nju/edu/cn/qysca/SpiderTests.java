package nju.edu.cn.qysca;

import nju.edu.cn.qysca.utils.parser.PomParser;
import nju.edu.cn.qysca.utils.spider.*;
import org.junit.Test;

public class SpiderTests {

    /**
     * 这里本来想用@Test的，但是这样相对路径是相对test的，而不是project，会产生问题
     */
    public void pomSpiderTest(){
        // 这个url特殊规则比较多
        //String pomUrl = "https://repo1.maven.org/maven2/org/apache/maven/plugins/maven-compiler-plugin/3.8.1/maven-compiler-plugin-3.8.1.pom";
        //这个url比较常规
         String pomUrl = "https://repo1.maven.org/maven2/garden/ephemeral/dsstore/dsstore-gradle-plugin/0.0.5/dsstore-gradle-plugin-0.0.5.pom";

        //String pomUrl = "https://repo1.maven.org/maven2/org/sonatype/sisu/sisu-inject-plexus/1.4.2/sisu-inject-plexus-1.4.2.pom";

        //String pomUrl = "https://repo1.maven.org/maven2/org/apache/maven/maven-model/2.0/maven-model-2.0.pom";

        //String pomUrl = "https://repo1.maven.org/maven2/ant/ant/1.6.2/ant-1.6.2.pom";

        //String pomUrl = "https://repo1.maven.org/maven2/com/fasterxml/jackson/core/jackson-databind/2.12.6/jackson-databind-2.12.6.pom";
        PomParser pomParser = new PomParser();
        pomParser.parsePom(pomUrl);

    }

    public static void main(String[] args) {
        SpiderTests spiderTests = new SpiderTests();
        spiderTests.pomSpiderTest();}
    }


