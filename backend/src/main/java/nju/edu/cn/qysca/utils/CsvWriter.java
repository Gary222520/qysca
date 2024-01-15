package nju.edu.cn.qysca.utils;

import nju.edu.cn.qysca.utils.parser.DependsRelationship;
import nju.edu.cn.qysca.utils.parser.JavaComponentNode;
import nju.edu.cn.qysca.utils.parser.HasParentRelationship;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class CsvWriter {
    /**
     * 写入csv文件
     * @param javaComponentNodeList
     */
    public static void writeJavaComponentList(List<JavaComponentNode> javaComponentNodeList, String filePath){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), true));

            if (isFileEmpty(filePath)) {
                // 如果csv文件为空，则写入HEADERS
                writer.write(JavaComponentNode.HEADERS);
            }

            for (JavaComponentNode javaComponentNode : javaComponentNodeList) {
                writer.newLine();
                writer.write(javaComponentNode.toCsvString());
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e){
            System.err.println("没有找到指定csv文件:" + filePath);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * 写入csv文件
     * @param dependsRelationshipList
     */
    public static void writeDependsRelationshipList(List<DependsRelationship> dependsRelationshipList, String filePath){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), true));

            if (isFileEmpty(filePath)) {
                // 如果csv文件为空，则写入HEADERS
                writer.write(DependsRelationship.HEADERS);
            }

            for (DependsRelationship dependsRelationship : dependsRelationshipList) {
                writer.newLine();
                writer.write(dependsRelationship.toCsvString());
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e){
            System.err.println("没有找到指定csv文件:" + filePath);
        } catch (IOException e){
            e.printStackTrace();
        }
    }


    /**
     * 写入csv文件
     * @param hasParentRelationshipList
     */
    public static void writeHasParentRelationshipList(List<HasParentRelationship> hasParentRelationshipList, String filePath){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), true));

            if (isFileEmpty(filePath)) {
                // 如果csv文件为空，则写入HEADERS
                writer.write(HasParentRelationship.HEADERS);
            }

            for (HasParentRelationship hasParentRelationship : hasParentRelationshipList) {
                writer.newLine();
                writer.write(hasParentRelationship.toCsvString());
            }
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e){
            System.err.println("没有找到指定csv文件:" + filePath);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    private static boolean isFileEmpty(String filePath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            return reader.readLine() == null;
        }
    }

}
