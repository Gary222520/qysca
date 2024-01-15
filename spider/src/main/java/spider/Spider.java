package spider;

import dataAccess.DataAccessInterface;

public interface Spider<T>{

    void crawlMany(String directoryUrl, DataAccessInterface<T> dataAccess);

    T crawlByGAV(String groupId, String artifactId, String version);
}
