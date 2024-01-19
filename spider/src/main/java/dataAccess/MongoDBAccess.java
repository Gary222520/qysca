package dataAccess;

import com.mongodb.MongoBulkWriteException;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.InsertManyOptions;
import config.DatabaseConfig;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoDBAccess<T> {
    private static final Map<String, MongoDBAccess<?>> instances = new HashMap<>();

    private MongoCollection<T> collection;
    private InsertManyOptions insertManyOptions;

    public static synchronized <T> MongoDBAccess<T> getInstance(String COLLECTION_NAME, Class<T> clazz) {
        String key = COLLECTION_NAME + ":" + clazz.getName();
        if (!instances.containsKey(key)) {
            MongoDBAccess<T> instance = new MongoDBAccess<>(COLLECTION_NAME, clazz);
            instances.put(key, instance);
        }
        return (MongoDBAccess<T>) instances.get(key);
    }

    /**
     * 写入mongodb
     *
     * @param COLLECTION_NAME collection name
     * @param clazz           T.class
     */
    private MongoDBAccess(String COLLECTION_NAME, Class<T> clazz) {
        try {
            MongoClient mongoClient = MongoClients.create(DatabaseConfig.getDatabaseUrl());
            MongoDatabase database = mongoClient.getDatabase(DatabaseConfig.getDatabaseName());
            // java对象到mongo对象的自动映射
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(
                            // 通过制定automatic为true, 来实现自动映射
                            PojoCodecProvider.builder().automatic(true).build()
                    )
            );
            collection = database.getCollection(COLLECTION_NAME, clazz).withCodecRegistry(codecRegistry);
            // 设置为无序插入，避免因批次中一个插入失败而全部停止
            insertManyOptions = new InsertManyOptions().ordered(false);
        } catch (Exception e) {
            System.out.println("Failed to connect to MongoDB. Error: " + e.getMessage());

        }
    }

    /**
     * 批量写入
     *
     * @param dataList 数据list
     */
    public void writeMany(List<T> dataList) {
        try {
            // 插入数据
            collection.insertMany(dataList, insertManyOptions);
            System.out.println("Data written to MongoDB successfully! Collection name = " + collection.getNamespace().toString() + ". This Batch Number = " + dataList.size());
        } catch (MongoBulkWriteException e) {
            // 主键重复，忽略该错误
            System.out.println("Data written to MongoDB successfully! Collection name = " + collection.getNamespace().toString() + ". This Batch Number = " + dataList.size());
        } catch (IllegalArgumentException e){
            // 插入空队列，do nothing
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public T readByGAV(String groupId, String artifactId, String version) {
        try {
            // 构建查询条件
            Bson filter = Filters.and(
                    Filters.eq("groupId", groupId),
                    Filters.eq("artifactId", artifactId),
                    Filters.eq("version", version)
            );

            // 执行查询
            FindIterable<T> result = collection.find(filter);

            // 获取第一个匹配的文档
            return result.first();
        } catch (Exception e) {
            e.printStackTrace();
            return null; // 或者根据需要抛出异常
        }
    }

    /**
     * 获取collection中所有文档
     *
     * @return
     */
    public FindIterable<T> findAll() {
        try {
            // 无限制条件，获取所有文档
            FindIterable<T> result = collection.find();
            // 返回所有文档
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            // 或者根据需要抛出异常
            return null;
        }
    }

    /**
     * 删除collection中所有文档
     */
    public void deleteAllDocuments() {
        try {
            // 删除集合中的所有文档
            collection.deleteMany(new Document());

            System.out.println("All documents deleted from the collection.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

