package nju.edu.cn.qysca.utils;

import fr.dutra.tools.maven.deptree.core.InputType;
import fr.dutra.tools.maven.deptree.core.Node;
import fr.dutra.tools.maven.deptree.core.Parser;
import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.InvocationRequest;
import org.apache.maven.shared.invoker.Invoker;

import java.io.*;
import java.util.Collections;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class MavenUtil {

    /**
     * 根据pom文件路径在pom文件路径下生成result.txt文件，文件中记录了pom文件中所有依赖的树形结构
     * Maven_HOME
     *
     * @param filePath
     */
    public static Node mavenDependencyTreeAnalyzer(String filePath, String builder) {
        try {
            Invoker invoker = new DefaultInvoker();
            invoker.setMavenHome(new File("D:\\apache-maven-3.8.6"));
            String resultPath = null;
            InvocationRequest request = new DefaultInvocationRequest();
            // result文件在文件夹下
            if (builder.equals("maven")) {
                File pom = new File(filePath);
                request.setPomFile(pom);
                resultPath = pom.getParent() + "/" + "result";
            } else if (builder.equals("jar")) {
                File file = new File(filePath);
                request.setBaseDirectory(file.getParentFile());
                request.setGoals(Collections.singletonList("install:install-file -Dfile" + filePath));
                invoker.execute(request);
                resultPath = file.getParent() + "/" + "result";
            } else if (builder.equals("zip")) {
                unzip(filePath);
                File file = new File(filePath.substring(0, filePath.lastIndexOf('.')));
                request.setBaseDirectory(file);
                resultPath = file.getPath() + "/" + "result";
            }
            request.setGoals(Collections.singletonList("dependency:tree -DoutputFile=result -DoutputType=text"));
            invoker.execute(request);
            // 获得result结果的路径
            FileInputStream fis = new FileInputStream(new File(resultPath));
            Reader reader = new BufferedReader(new InputStreamReader(fis));
            InputType type = InputType.TEXT;
            Parser parser = type.newParser();
            Node node = parser.parse(reader);
            return node;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解压zip文件
     *
     * @param filePath zip文件路径
     * @throws Exception
     */
    private static void unzip(String filePath) throws Exception {
        File file = new File(filePath);
        File dir = new File(file.getParent());
        if (!dir.exists()) {
            dir.mkdirs();
        }
        ZipFile zipFile = new ZipFile(filePath);
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = zipEntries.nextElement();
            String entryName = zipEntry.getName();
            String fileDestPath = dir + "/" + entryName;
            if (!zipEntry.isDirectory()) {
                File destFile = new File(fileDestPath);
                destFile.getParentFile().mkdirs();
                InputStream inputStream = zipFile.getInputStream(zipEntry);
                FileOutputStream outputStream = new FileOutputStream(destFile);
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                outputStream.close();
                inputStream.close();
            } else {
                File dirToCreate = new File(fileDestPath);
                dirToCreate.mkdirs();
            }
        }
        zipFile.close();
    }
}
