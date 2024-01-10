package nju.edu.cn.qysca.utils.parser;

/**
 *  用以记录pom
 */
public class PomNode {
    public static final String[] headers = {"GroupId", "ArtifactId", "Version", "PomUrl", "Name"};
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String pomUrl;
    private final String name;

    public PomNode(String groupId, String artifactId, String version, String name, String pomUrl) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.pomUrl = pomUrl;
        this.name = name;
    }

    public String getGroupId() {
        return this.groupId;
    }

    public String getArtifactId() {
        return this.artifactId;
    }

    public String getVersion() {
        return this.version;
    }

    public String getPomUrl() {
        return this.pomUrl;
    }

    public String getName() {
        return this.name;
    }
}
