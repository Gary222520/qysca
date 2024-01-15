package dataAccess;

import dao.MongoDBWriter;
import domain.OpensourceComponentDO;

import java.util.ArrayList;
import java.util.List;

public class OpensourceComponentDODataAccess implements DataAccessInterface<OpensourceComponentDO> {

    private final MongoDBWriter mongoDBWriter;
    private static final int BATCH_SIZE = 5000;
    private List<OpensourceComponentDO> queue;

    public OpensourceComponentDODataAccess(){
        mongoDBWriter = new MongoDBWriter();
        queue = new ArrayList<>();
    }

    /**
     * 将数据加入queue，当queue达到BATCH_SIZE时，自动批量写入数据库
     * @param data 数据
     */
    public synchronized void enqueue(OpensourceComponentDO data){
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
    @Override
    public synchronized void flush() {
        batchWriteToDatabase();
    }
}
