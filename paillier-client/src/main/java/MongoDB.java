import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.types.ObjectId;

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
        MongoDatabase mongodbClient = this.getClient().getDatabase(Configs.DATABASE);
        return mongodbClient.getCollection(Configs.COLLECTION, BasicDBObject.class);
    }

    public String insert(BasicDBObject basicDBObject, MongoCollection<BasicDBObject> collection) {
        collection.insertOne(basicDBObject);

        return basicDBObject.getObjectId("_id").toString();
    }


    public BasicDBObject get(String documentId , MongoCollection<BasicDBObject> collection) throws DocumentNotFoundException {
        ArrayList<BasicDBObject> docs = collection.find(Filters.eq("_id", new ObjectId(documentId))).into(new ArrayList<BasicDBObject>());

        if (docs == null || docs.size() == 0) {
            throw new DocumentNotFoundException();
        }

        return docs.get(0);
    }

    public MapReduceIterable<BasicDBObject> mapReduce(List<String> documentIds, String map, String reduce, MongoCollection<BasicDBObject> collection) {
        List<ObjectId> objectIds = documentIds.stream().map(ObjectId::new).collect(Collectors.toList());
        MapReduceIterable<BasicDBObject> result = collection.mapReduce(map, reduce).filter(Filters.in("_id", objectIds));
        return result;
    }
}
