package nju.edu.cn.qysca.utils.spider;

import nju.edu.cn.qysca.utils.parser.PomParser;
import org.apache.maven.model.Model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Spider {
    private static final String DEFAULT_TYPE = "pom";
    private static final String DEFAULT_URL_LIST_PATH = "backend/src/main/resources/url_list.txt";

    public static void main(String[] args) {

        List<String> directoryUrlList = readLinesFromFile(DEFAULT_URL_LIST_PATH);
        for (String directoryUrl : directoryUrlList) {
            if (directoryUrl.startsWith("//"))
                continue;
            System.out.println("开始爬取并解析： "+ directoryUrl);
            List<String> pomUrlList =  PomSpider.findAllPomUrlInDirectory(directoryUrl);
            PomParser pomParser = new PomParser();
            for (String pomUrl : pomUrlList){
                pomParser.parsePom(pomUrl);
            }
            System.out.println();
        }
    }



    private static List<String> readLinesFromFile(String filePath) {
        List<String> lines = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return lines;
    }
}
