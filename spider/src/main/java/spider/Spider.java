package spider;

public interface Spider<T>{
    void crawlMany(String directoryUrl);
    T crawl(String url);
}
