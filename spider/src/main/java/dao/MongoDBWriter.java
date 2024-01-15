package dao;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import domain.OpensourceComponentDO;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

import java.util.Collections;
import java.util.List;

public class MongoDBWriter {
    private static final String DATABASE_NAME = "qysca";
    private static final String COLLECTION_NAME = "opensource_components";

    private MongoClient mongoClient;
    private MongoDatabase database;

    public MongoDBWriter(){
        try{
            String mongoDBUrl = "mongodb://localhost:27017";
            mongoClient = MongoClients.create(mongoDBUrl);
            database = mongoClient.getDatabase(DATABASE_NAME);
            System.out.println("Connected to MongoDB: " + mongoDBUrl);
        } catch (Exception e) {
            System.out.println("Failed to connect to MongoDB. Error: " + e.getMessage());
        }
    }

    public void writeMany(List<OpensourceComponentDO> dataList) {
        try {

            // java对象到mongo对象的自动映射
            CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
                    MongoClientSettings.getDefaultCodecRegistry(),
                    CodecRegistries.fromProviders(
                            // 通过制定automatic为true, 来实现自动映射
                            PojoCodecProvider.builder().automatic(true).build()
                    )
            );
            MongoCollection<OpensourceComponentDO> collection = database.getCollection(COLLECTION_NAME, OpensourceComponentDO.class).withCodecRegistry(codecRegistry);

            // 插入数据`````````
            collection.insertMany(dataList);
            System.out.println("Data written to MongoDB successfully! This Batch Number = "+ dataList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Document convertOpensourceComponentDOtoDocument(OpensourceComponentDO componentDO) {
        Document document = new Document();
        document.append("name",componentDO.getName());
        document.append("groupId",componentDO.getGroupId());
        document.append("artifactId",componentDO.getArtifactId());
        document.append("version", componentDO.getVersion());
        document.append("language", componentDO.getLanguage());
        document.append("opensource", componentDO.getOpensource());
        document.append("description", componentDO.getDescription());
        document.append("url", componentDO.getUrl());
        document.append("downloadUrl", componentDO.getDownloadUrl());
        document.append("sourceUrl", componentDO.getSourceUrl());
        document.append("developers", componentDO.getDevelopers());
        document.append("licenses", componentDO.getLicenses());
        document.append("pom", componentDO.getPom());
        return document;
    }

}

