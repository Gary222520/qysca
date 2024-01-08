package nju.edu.cn.qysca.utils.parser;

import java.util.ArrayList;
import java.util.List;

public class PomFileInfo {
    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String pomUrl;
    private List<PomFileInfo> dependencies;

    public PomFileInfo(String groupId, String artifactId, String version, String pomUrl) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.pomUrl = pomUrl;
        this.dependencies = new ArrayList<>();
    }

    public void setDependencies(List<PomFileInfo> dependencies) {
        this.dependencies = dependencies;
    }

    public List<PomFileInfo> getDependencies() {
        return this.dependencies;
    }

    @Override
    public String toString() {
        return "PomFileInfo{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", version='" + version + '\'' +
                ", pomUrl='" + pomUrl + '\'' +
                '}';
    }
}