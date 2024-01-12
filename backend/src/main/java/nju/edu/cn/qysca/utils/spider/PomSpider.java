package nju.edu.cn.qysca.utils.spider;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class PomSpider {

    /**
     * 通过url获取pom文件，然后转化为maven-model
     * model的内容完全和pom文件本身一样
     *
     * @param pomUrl pom文件的url
     * @return maven-model
     */
    public static Model getPomModel(String pomUrl) {
        // 确认url存在并且为一个pom文件
        if (pomUrl == null || !pomUrl.endsWith(".pom")) {
            System.err.println("\"" + pomUrl + "\" is not a valid url or a pom file.");
            return null;
        }

        // 使用jsoup获取pom文件
        Document jsoupDocument = UrlConnector.getDocumentByUrl(pomUrl);
        if (jsoupDocument == null) {
            System.err.println("\"" + pomUrl + "\" is not a valid url or a pom file.");
            return null;
        }
        String pomString = jsoupDocument.outerHtml();

        // 转化为maven-model
        Model model = null;
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(new StringReader(pomString));
            return model;
        } catch (IOException | XmlPullParserException e) {
            System.err.println("Failed to convert Pom file to a maven model: " + pomUrl);
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 在指定目录url下查找.pom文件
     *
     * @param directoryUrl 目录url
     * @return 如果存在.pom文件则返回其url，否则返回null
     */
    public static String findPomFileUrlInDirectory(String directoryUrl) {

        // todo 有时目录下可能有多个pom，如何找到正确的那个pom
        // https://repo1.maven.org/maven2/org/apache/maven/maven/2.0/

        Document document = UrlConnector.getDocumentByUrl(directoryUrl);

        // 确认url存在
        if (document == null) {
            System.err.println("\"" + directoryUrl + "\" is not a valid url.");
            return null;
        }

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");

        //遍历目录下文件，找到其中以.pom结尾的文件
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            // 一般含-javadoc的不是需要的pom文件，这样的处理可能比较简单了
            if (fileAbsUrl.endsWith(".pom") && !fileAbsUrl.contains("-javadoc")) {
                return fileAbsUrl;
            }
        }
        System.err.println("No .pom file found in \"" + directoryUrl + "\"");
        return null;
    }


    /**
     * 在指定目录url下，获取所有版本的pomUrl
     * 例如 directoryUrl为 https://repo1.maven.org/maven2/junit/junit/， 那么其获得其下面每个版本的一份pom文件的url
     * @param directoryUrl 目录url
     * @return 返回所有版本的pomUrl
     */
    public static List<String> findAllPomUrlInDirectory(String directoryUrl) {
        List<String>  pomUrls = new ArrayList<>();

        Document document = UrlConnector.getDocumentByUrl(directoryUrl);
        // 确认url存在
        if (document == null) {
            System.err.println("\"" + directoryUrl + "\" is not a valid url.");
            return new ArrayList<>();
        }

        // 获取目录下所有文件
        Elements fileElements = document.select("a[href]");
        //遍历目录下文件，找到其中的子目录（也就是某一版本的目录）
        for (Element fileElement : fileElements) {
            String fileAbsUrl = fileElement.absUrl("href");
            String fileText = fileElement.ownText();
            if (fileText.endsWith("/") && !fileText.endsWith("../")) {
                // 找到这一版本的目录下的pom文件
                String pomUrl = findPomFileUrlInDirectory(fileAbsUrl);
                pomUrls.add(pomUrl);
            }
        }
        return pomUrls;
    }
}
