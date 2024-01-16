package dataAccess;

import dao.MongoDBWriter;

import java.util.ArrayList;
import java.util.List;

public class DataAccess<T> {

    private final MongoDBWriter mongoDBWriter;
    private static final int BATCH_SIZE = 5000;
    private List<T> queue;

    public DataAccess(String COLLECTION_NAME, Class<T> clazz){
        mongoDBWriter = new MongoDBWriter<T>(COLLECTION_NAME, clazz);
        queue = new ArrayList<>();
    }

    /**
     * 将数据加入queue，当queue达到BATCH_SIZE时，自动批量写入数据库
     * @param data 数据
     */
    public synchronized void enqueue(T data){
        queue.add(data);
        if (queue.size() >= BATCH_SIZE){
            batchWriteToDatabase();
        }
    }

    /**
     * 批量写入数据库
     * 写入后将queue清空
     */
    private void batchWriteToDatabase() {
        mongoDBWriter.writeMany(queue);
        queue = new ArrayList<>();
    }

    /**
     * 手动刷新queue，将queue中的数据写入数据库
     */
    public synchronized void flush() {
        batchWriteToDatabase();
    }
}
