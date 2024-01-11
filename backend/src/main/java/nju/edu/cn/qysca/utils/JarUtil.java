package nju.edu.cn.qysca.utils;

import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarUtil {

    public static List<Model> extractJarForPom(String path) {
        List<Model> models = new ArrayList<>();
        try {
            JarFile jarFile = new JarFile(path);
            Enumeration<? extends JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith("pom.xml")) {
                    MavenXpp3Reader reader = new MavenXpp3Reader();
                    Model model = reader.read(jarFile.getInputStream(entry));
                    models.add(model);
                }
            }
            jarFile.close();
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return models;
    }
}
