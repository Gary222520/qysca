package nju.edu.cn.qysca.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class FolderUtil {
//    /**
//     * 强制删除文件夹
//     * @param filePath 文件夹
//     * @throws IOException
//     */
//    public static void forceDeleteFolder(String filePath) {
//        File folder = new File(filePath);
//        Path folderPath = folder.toPath();
//        try {
//            if (folder.isDirectory()) {
//                try (Stream<Path> paths = Files.walk(folderPath)) {
//                    paths.sorted(Comparator.reverseOrder())
//                            .forEach(path -> {
//                                try {
//                                    Files.deleteIfExists(path);
//                                } catch (IOException e) {
//                                    System.err.println("here");
//                                    e.printStackTrace();
//                                }
//                            });
//                }
//            } else {
//                Files.deleteIfExists(folderPath);
//            }
//        } catch (IOException e){
//            System.err.println("there");
//            e.printStackTrace();
//        }
//    }


    /**
     * 删除文件夹
     *
     * @param filePath 文件夹
     */
    public static void deleteFolder(String filePath) {
        File folder = new File(filePath);
        if (folder.exists()) {
            deleteFolderFile(folder);
        }
    }

    /**
     * 递归删除文件夹下的文件
     *
     * @param folder 文件夹
     */
    private static void deleteFolderFile(File folder) {
        File[] files = folder.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                deleteFolderFile(file);
            } else {
                file.delete();
            }
        }
        folder.delete();
    }
}
