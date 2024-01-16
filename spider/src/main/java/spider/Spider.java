package spider;

import dataAccess.DataAccess;

public interface Spider<T>{

    void crawlMany(String directoryUrl, DataAccess<T> dataAccess);
}
