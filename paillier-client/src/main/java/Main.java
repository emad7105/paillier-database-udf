import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.n1analytics.paillier.*;
import com.sun.tools.javac.util.Assert;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * This code is purely written for testing purposes and
 * research prototype. Any use of this piece of art would
 * cause cancer.
 *
 * @author Emad heydari Beni
 */
public class Main {


    public static void main(String[] args) throws IOException, URISyntaxException {
        //loadFunction();
        cleanup();
        test();
    }

    private static void cleanup() {
        MongoDB mongoDB = new MongoDB();
        mongoDB.cleanUp(mongoDB.getCollection());
    }

    private static void loadFunction() throws IOException, URISyntaxException {
        // Mongo collection
        MongoDB mongoDB = new MongoDB();
        mongoDB.loadCustomFunctions();
    }

    private static void test() throws IOException, URISyntaxException {
        PaillierCrypto phe = new PaillierCrypto(Configs.KEY_LENGHT);

        // Gen keys
        PaillierPrivateKey sk = phe.generatePrivateKey();
        PaillierPublicKey pk = phe.generatePublicKey(sk);

        // Mongo collection
        MongoDB mongoDB = new MongoDB();
        MongoCollection<BasicDBObject> collection = mongoDB.getCollection();


        // Create some encrypted docs
        List<BasicDBObject> docsEmad = createEncryptedDocs(100, "Emad", phe, pk);
        List<BasicDBObject> docsAnsar = createEncryptedDocs(3, "Ansar", phe, pk);


        // Insert all docs to MongoDB
        docsEmad.forEach(doc -> mongoDB.insert(doc, collection));
        docsAnsar.forEach(doc -> mongoDB.insert(doc, collection));


        // MapReduce
        String map = loadFunction("map.js");
        String reduce = loadFunction("reduce.js");

        MapReduceIterable<BasicDBObject> encryptedResults = mongoDB.mapReduce(map, reduce, collection);
        Assert.checkNonNull(encryptedResults);

        // Iterating and decrypting the result (HE)
        HashMap result = new HashMap();
        MongoCursor<BasicDBObject> iterator = encryptedResults.iterator();
        while (iterator.hasNext()) {
            // keys
            PaillierContext context = phe.context(pk);

            BasicDBObject encryptedResult = iterator.next();
            String heEncryptedResultInHex = (String) encryptedResult.get("value");

            System.out.println("\n\nEncrypted sum:" + new GsonBuilder().setPrettyPrinting().create().toJson(heEncryptedResultInHex) + "\n\n");

            // decrypt
            PaillierCrypto.SerializableEncryptedNumber encryptedNumber = new PaillierCrypto.SerializableEncryptedNumber(Hex.decode(heEncryptedResultInHex));
            EncodedNumber plainResult = phe.decrypt(encryptedNumber, sk, context);

            result.put(encryptedResult.get("_id").toString(), plainResult.decodeDouble());
        }

        System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(result));
    }

    private static String loadFunction(String function) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(Main.class.getClassLoader().getResource(function).toURI())));
    }


    private static List<BasicDBObject> createEncryptedDocs(Integer count, String name, PaillierCrypto phe, PaillierPublicKey pk) {
        List<BasicDBObject> docs = new ArrayList();

        while(count != 0) {

            Document doc = new Document(name, 10);

            PaillierContext context = phe.context(pk);
            EncryptedNumber encryptedAmount = phe.encrypt(doc.getAmount(), context);

            BasicDBObject encryptedMongodbBasicDBObject = new BasicDBObject();
            encryptedMongodbBasicDBObject.put("name", doc.getName());
            encryptedMongodbBasicDBObject.put("amount",
                    Hex.toHexString(
                            new PaillierCrypto.SerializableEncryptedNumber(encryptedAmount, pk.getModulusSquared(), "amount").toBytes()
                    ));

            docs.add(encryptedMongodbBasicDBObject);
            count--;
        }

        return docs;
    }



}
