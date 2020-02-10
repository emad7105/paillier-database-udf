package mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.UpdateOptions;
import configs.Configs;
import org.bson.BsonDocument;
import org.bson.BsonJavaScript;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author Emad Heydari Beni
 */
public class MongoDB {

    private MongoClient getClient() {
        return new com.mongodb.MongoClient(Configs.HOST, Configs.PORT);
    }

    public MongoCollection<BasicDBObject> getCollection() {
        MongoDatabase mongodbClient = getDatabase();
        return mongodbClient.getCollection(Configs.COLLECTION, BasicDBObject.class);
    }

    private MongoDatabase getDatabase() {
        return this.getClient().getDatabase(Configs.DATABASE);
    }

    public String insert(BasicDBObject basicDBObject, MongoCollection<BasicDBObject> collection) {
        collection.insertOne(basicDBObject);

        return basicDBObject.getObjectId("_id").toString();
    }


    public List<String> search(String searchToken , MongoCollection<BasicDBObject> collection) throws DocumentNotFoundException {
        ArrayList<BasicDBObject> docIds = collection.find(new BasicDBObject("name", searchToken)) // find
                .projection(Projections.include("_id"))// return only _id field
        .into(new ArrayList<BasicDBObject>());

        //     ArrayList<BasicDBObject> docs = collection.find(Filters.eq("_id", new ObjectId(documentId))).into(new ArrayList<BasicDBObject>());

        if (docIds == null || docIds.size() == 0) {
            throw new DocumentNotFoundException();
        }

        return docIds.stream().map(docId -> String.valueOf(docId.get("_id"))).collect(Collectors.toList());
    }

    public MapReduceIterable<BasicDBObject> mapReduce(String map, String reduce, MongoCollection<BasicDBObject> collection) {
        return collection.mapReduce(map, reduce);
    }

    public MapReduceIterable<BasicDBObject> mapReduce(String map, String reduce, List<String> documentIds,MongoCollection<BasicDBObject> collection) {
        List<ObjectId> objectIds = documentIds.stream().map(ObjectId::new).collect(Collectors.toList());
        MapReduceIterable<BasicDBObject> result = collection.mapReduce(map, reduce).filter(Filters.in("_id", objectIds));
        return result;
    }

    public void loadCustomFunctions() throws IOException, URISyntaxException {
        System.out.println("\n\n\n" + HEADD.heAdd + "\n\n\n");
        String customFunction = HEADD.heAdd;

        BsonDocument heAddFunction = new BsonDocument("value", new BsonJavaScript(customFunction));

        getDatabase().getCollection("system.js").updateOne(
                new Document("_id", "he_add"),
                new Document("$set", heAddFunction),
                new UpdateOptions().upsert(true));
    }

    public void cleanUp(MongoCollection<BasicDBObject> collection) {
        try {
            collection.drop();
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }
}
