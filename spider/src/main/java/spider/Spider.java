package spider;

public interface Spider<T>{
    void crawlMany(String directoryUrl);
    T crawlByGAV(String groupId, String artifactId, String version);
}
