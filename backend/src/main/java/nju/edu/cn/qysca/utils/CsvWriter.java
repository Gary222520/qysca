package nju.edu.cn.qysca.utils;

import nju.edu.cn.qysca.utils.parser.PomDependencyNode;
import nju.edu.cn.qysca.utils.parser.PomNode;
import nju.edu.cn.qysca.utils.parser.PomParentNode;
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties;

import java.io.*;

public class CsvWriter {
    private final String filePath;
    private final String[] headers;

    public CsvWriter(String filePath, String[] headers){
        this.filePath = filePath;
        this.headers = headers;
    }

    /**
     * 写入csv文件
     * @param pomNode
     */
    public void writePomNode(PomNode pomNode){
        String line = pomNode.getGroupId() + "," + pomNode.getArtifactId() + "," + pomNode.getVersion() + "," + pomNode.getName() + "," + pomNode.getPomUrl();
        write(line);
    }

    /**
     * 写入csv文件
     * @param pomDependencyNode
     */
    public void writePomDependencyNode(PomDependencyNode pomDependencyNode){
        String line = pomDependencyNode.getPomNodeA().getGroupId() + "," + pomDependencyNode.getPomNodeA().getArtifactId() + "," + pomDependencyNode .getPomNodeA().getVersion()
                + "," + pomDependencyNode.getPomNodeB().getGroupId() + "," + pomDependencyNode.getPomNodeB().getArtifactId() + "," + pomDependencyNode.getPomNodeB().getVersion()
                + "," + pomDependencyNode.getScope();
        write(line);
    }

    /**
     *  写入csv文件
     * @param pomParentNode
     */
    public void writePomParentNode(PomParentNode pomParentNode) {
        String line = pomParentNode.getSon().getGroupId() + "," + pomParentNode.getSon().getArtifactId() + "," + pomParentNode.getSon().getVersion()
                + "," + pomParentNode.getParent().getGroupId() + "," + pomParentNode.getParent().getArtifactId() + "," + pomParentNode.getParent().getVersion();
        write(line);
    }


    /**
     * 私有方法，写入csv文件
     * @param line
     */

    private void write(String line){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filePath), true));
            writer.newLine();

            writer.write(line);
            writer.flush();
            writer.close();
        } catch (FileNotFoundException e){
            System.err.println("没有找到指定csv文件:" + filePath);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

}
