package nju.edu.cn.qysca.utils.parser;

/**
 * 表示pom间的父子关系
 */
public class PomParentNode {
    public static final String[] headers = {"GroupIdA", "ArtifactIdA", "VersionA", "GroupIdB", "ArtifactIdB", "VersionB"};
    private PomNode son;
    private PomNode parent;
    public PomParentNode(PomNode son, PomNode parent){
        this.son = son;
        this.parent = parent;
    }
    public PomNode getSon() {
        return this.son;
    }
    public PomNode getParent() {
        return this.parent;
    }
}
