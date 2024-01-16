package dao;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.List;

public class MongoDBWriter<T> {
    private static final String DATABASE_URL = "mongodb://localhost:27017";
    private static final String DATABASE_NAME = "sca";
    private MongoCollection<T> collection;

    public MongoDBWriter(String COLLECTION_NAME, Class<T> clazz){
        try{
            MongoClient mongoClient = MongoClients.create(DATABASE_URL);
            MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
            // java对象到mongo对象的自动映射
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(
                            // 通过制定automatic为true, 来实现自动映射
                            PojoCodecProvider.builder().automatic(true).build()
                    )
            );
            collection = database.getCollection(COLLECTION_NAME, clazz).withCodecRegistry(codecRegistry);
            System.out.println("Connected to MongoDB: " + DATABASE_URL);
        } catch (Exception e) {
            System.out.println("Failed to connect to MongoDB. Error: " + e.getMessage());

        }
    }

    /**
     * 批量写入
     * @param dataList 数据list
     */
    public void writeMany(List<T> dataList) {
        try {
            // 插入数据
            collection.insertMany(dataList);
            System.out.println("Data written to MongoDB successfully! This Batch Number = "+ dataList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

