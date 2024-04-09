package nju.edu.cn.qysca.utils;

import nju.edu.cn.qysca.exception.PlatformException;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private static final String FILE_SEPARATOR = "/";


    /**
     * 解压zip文件
     *
     * @param filePath zip文件路径
     */
    public static void unzip(String filePath) {
        try {
            File file = new File(filePath);
            File dir = new File(file.getParent());
            if (!dir.exists()) {
                dir.mkdirs();
            }
            ZipFile zipFile = new ZipFile(filePath, Charset.forName("GBK"));
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            while (zipEntries.hasMoreElements()) {
                ZipEntry zipEntry = zipEntries.nextElement();
                String entryName = zipEntry.getName();
                String fileDestPath = dir + FILE_SEPARATOR + entryName;
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
        } catch (Exception e) {
            throw new PlatformException(500, "解压zip文件失败");
        }
    }

    /**
     * 解压tar.gz文件
     * @param filePath tar.gz文件路径
     */
    public static void unTarGz(String filePath) {
        // 获取输出目录路径
        String outputDirPath = new File(filePath).getParentFile().getPath();
        // 创建输出目录
        File outputDir = new File(outputDirPath);
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        try (FileInputStream fis = new FileInputStream(filePath);
             GZIPInputStream gzis = new GZIPInputStream(fis);
             TarArchiveInputStream taris = new TarArchiveInputStream(gzis)) {
            // 循环遍历 Tar 文件中的条目，并解压到指定目录
            TarArchiveEntry entry;
            while ((entry = taris.getNextTarEntry()) != null) {
                // 获取条目文件名
                String entryName = entry.getName();
                // 构建实际解压后文件的路径
                File entryFile = new File(outputDir, entryName);
                if (entry.isDirectory()) {
                    // 如果是目录，创建目录
                    entryFile.mkdirs();
                } else {
                    // 如果是文件，创建父目录并解压文件
                    File parent = entryFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    // 创建文件输出流
                    try (FileOutputStream fos = new FileOutputStream(entryFile)) {
                        // 读取 Tar 输入流并写入文件输出流
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = taris.read(buffer)) != -1) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
            }
        } catch (IOException e) {
            throw new PlatformException(500, "解压tar.gz文件失败");
        }
    }

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

    public static void zipDirectory(File sourceDir, File zipFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            zipDir(sourceDir, sourceDir.getName(), zos);
        }
    }

    private static void zipDir(File dir, String basePath, ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    zipDir(file, basePath + File.separator + file.getName(), zos);
                } else {
                    zipFile(file, basePath, zos);
                }
            }
        }
    }

    private static void zipFile(File file, String basePath, ZipOutputStream zos) throws IOException {
        String entryName = basePath + File.separator + file.getName();
        ZipEntry zipEntry = new ZipEntry(entryName);
        zos.putNextEntry(zipEntry);
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fis.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
            }
        }
        zos.closeEntry();
    }

    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        directory.delete();
    }
}
