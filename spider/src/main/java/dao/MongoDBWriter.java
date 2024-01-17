package dao;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import config.DatabaseConfig;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;

public class MongoDBWriter<T> {
    private MongoCollection<T> collection;

    /**
     * 写入mongodb
     *
     * @param COLLECTION_NAME collection name
     * @param clazz           T.class
     */
    public MongoDBWriter(String COLLECTION_NAME, Class<T> clazz) {
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
            System.out.println("Connected to MongoDB: " + DatabaseConfig.getDatabaseName());
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
            collection.insertMany(dataList);
            System.out.println("Data written to MongoDB successfully! This Batch Number = " + dataList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

