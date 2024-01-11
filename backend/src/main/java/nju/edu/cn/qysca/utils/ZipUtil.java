package nju.edu.cn.qysca.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipUtil {

    public static List<Model> extractZipForPom(String path) {
        List<Model> models = new ArrayList<>();
        try {
            ZipFile zipFile = new ZipFile(path);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith("pom.xml")) {
                    //将pom文件转化成Maven Model类型
                    MavenXpp3Reader reader = new MavenXpp3Reader();
                    Model model = reader.read(zipFile.getInputStream(entry));
                    models.add(model);
                }
            }
            zipFile.close();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return models;
    }
}
