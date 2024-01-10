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

    private static final String DEFAULT_DOWNLOAD_PATH = "backend/src/main/resources/poms/";
    private static final String DEFAULT_TYPE = "pom";
    private static final String DEFAULT_URL_LIST_PATH = "backend/src/main/resources/url_list.txt";

    public static void main(String[] args) {

//        String downloadPath = DEFAULT_DOWNLOAD_PATH;
//        Boolean isWriteCsv = true;
//        String type;
//
//        System.out.println("Command-line arguments: " + Arrays.toString(args));
//        // 解析args参数
//        for (int i = 0; i < args.length; i++) {
//            if (args[i].equalsIgnoreCase("downloadPath") && i + 1 < args.length) {
//                downloadPath = args[i + 1];
//                System.out.println("Download Path: " + downloadPath);
//                i++;
//            } else if (args[i].equalsIgnoreCase("writeCsv")) {
//                isWriteCsv = true;
//            } else if (args[i].equalsIgnoreCase("pom")) {
//                type = "pom";
//            }
//        }

        List<String> urlList = readLinesFromFile(DEFAULT_URL_LIST_PATH);
        for (String url : urlList) {
            Model model = PomSpider.getPomModel(url);
            PomParser.parsePomModel(model, url);
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
