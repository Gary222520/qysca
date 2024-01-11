package nju.edu.cn.qysca.utils.parser;

/**
 * 表示pom见的依赖关系
 */
public class PomDependencyNode {

    public static final String[] headers = {"GroupIdA", "ArtifactIdA", "VersionA", "GroupIdB", "ArtifactIdB", "VersionB", "Scope"};

    private final PomNode a;
    private final PomNode b;
    private final String scope;

    public PomDependencyNode(PomNode a, PomNode b, String scope) {
        this.a = a;
        this.b = b;
        this.scope = scope;
    }

    public PomNode getPomNodeA() {
        return a;
    }

    public PomNode getPomNodeB() {
        return b;
    }

    public String getScope() {
        return scope;
    }
}
