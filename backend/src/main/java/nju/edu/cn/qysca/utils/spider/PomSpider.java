package nju.edu.cn.qysca.utils.spider;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.StringReader;

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
        String pomString = jsoupDocument.outerHtml();

        // 转化为maven-model
        Model model = null;
        try {
            MavenXpp3Reader reader = new MavenXpp3Reader();
            model = reader.read(new StringReader(pomString));
        } catch (IOException | XmlPullParserException e) {
            System.err.println("Failed to convert Pom file to a maven model: " + pomUrl);
            e.printStackTrace();
        }

        return model;
    }


//    /**
//     * 通过url获取pom文件，存至Document对象中
//     * @param pomUrl pom文件的url
//     * @return org.jsoup.nodes.Document
//     */
//    public static Document getPomAsDocument(String pomUrl){
//
//        System.out.println("working on : " + pomUrl);
//
//        // 确认url存在并且为一个pom文件
//        if (pomUrl == null || !pomUrl.endsWith(".pom")) {
//            System.err.println("\"" + pomUrl + "\" is not a valid url or a pom file.");
//            return null;
//        }
//
//        // 以org.jsoup.nodes.Document返回其内容
//        return (Document) UrlConnector.getDocumentByUrl(pomUrl);
//    }

    /**
     * 在指定目录url下查找.pom文件
     *
     * @param directoryUrl 目录url
     * @return 如果存在.pom文件则返回其url，否则返回null
     */
    public static String findPomFileUrlInDirectory(String directoryUrl) {

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
            String fileUrl = fileElement.absUrl("href");
            if (fileUrl.endsWith(".pom")) {
                return fileUrl;
            }
        }
        System.err.println("No .pom file found in \"" + directoryUrl + "\"");
        return null;
    }

}
