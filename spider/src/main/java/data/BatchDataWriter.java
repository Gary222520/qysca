//package data;
//
//import dataAccess.MongoDBAccess;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 连接数据库的接口
// * 设置了batch，采用入队和批量写入的方式
// *
// * @param <T>
// */
//public class BatchDataWriter<T> {
//
//    private final MongoDBAccess<T> mongoDBAccess;
//    private static final int BATCH_SIZE = 500;
//    private List<T> queue;
//
//
//    public BatchDataWriter(String COLLECTION_NAME, Class<T> clazz) {
//        mongoDBAccess = MongoDBAccess.getInstance(COLLECTION_NAME, clazz);
//        queue = new ArrayList<>();
//    }
//
//    /**
//     * 将数据加入queue，当queue达到BATCH_SIZE时，自动批量写入数据库
//     *
//     * @param data 数据
//     */
//    public synchronized void enqueue(T data) {
//        queue.add(data);
//        if (queue.size() >= BATCH_SIZE) {
//            batchWriteToDatabase();
//        }
//    }
//
//    /**
//     * 批量写入数据库
//     * 写入后将queue清空
//     */
//    private void batchWriteToDatabase() {
//        mongoDBAccess.writeMany(queue);
//        queue = new ArrayList<>();
//    }
//
//    /**
//     * 手动刷新queue，将queue中的数据写入数据库
//     */
//    public synchronized void flush() {
//        batchWriteToDatabase();
//    }
//}
