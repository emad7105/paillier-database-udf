package be.heydari;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mongodb.BasicDBObject;
import com.mongodb.client.MapReduceIterable;
import com.mongodb.client.MongoCollection;
import com.n1analytics.paillier.*;
import com.sun.tools.javac.util.Assert;
import be.heydari.configs.Configs;
import be.heydari.crypto.Det;
import be.heydari.crypto.PaillierCrypto;
import be.heydari.mongo.Document;
import be.heydari.mongo.MongoDB;
import be.heydari.mongo.Results;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * This code is purely written for testing purposes and
 * research prototype. Any use of this piece of art would
 * cause cancer.
 *
 * @author Emad heydari Beni
 */
public class MainMongoDB {

    // for printing purpose
    private static Gson GSON_SERIALIZER = new GsonBuilder().setPrettyPrinting().create();

    public static void main(String[] args) throws Exception {
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

    private static void test() throws Exception {
        // preparing be.heydari.crypto
        PaillierCrypto phe = new PaillierCrypto(Configs.JAVALIER_KEY_LENGHT);
        Det det = new Det("BC", Configs.RAND_ALG, Configs.AES_MODE,
                Configs.AES_PADDING, Configs.AES_KEY_LENGHT, Configs.AES_IV_LENGHT);

        // Generate keys: Paillier
        PaillierPrivateKey sk = phe.generatePrivateKey();
        PaillierPublicKey pk = phe.generatePublicKey(sk);
        // Generate Key: DET
        Key detKey = det.genKey();

        // Prepare MongoDB client
        MongoDB mongoDB = new MongoDB();
        MongoCollection<BasicDBObject> collection = mongoDB.getCollection();


        // Create some encrypted docs
        List<BasicDBObject> docsEmad = createEncryptedDocs(100, "Emad", phe, pk, det, detKey);
        List<BasicDBObject> docsAnsar = createEncryptedDocs(3, "Ansar", phe, pk, det, detKey);
        List<BasicDBObject> docsBert = createEncryptedDocs(10, "Bert", phe, pk, det, detKey);


        // Insert all docs to be.heydari.mongo.MongoDB
        docsEmad.forEach(doc -> mongoDB.insert(doc, collection));
        docsAnsar.forEach(doc -> mongoDB.insert(doc, collection));
        docsBert.forEach(doc -> mongoDB.insert(doc, collection));


        // Search for
        String searchName = "Emad";
        List<String> documentIds = searchInvoices(searchName, det, detKey, mongoDB, collection);

        // MapReduce
        String map = loadFunction("mongodb/map.js");
        String reduce = loadFunction("mongodb/reduce.js");

        MapReduceIterable<BasicDBObject> encryptedResults = mongoDB.mapReduce(map, reduce, documentIds, collection);
        Assert.checkNonNull(encryptedResults);

        Results results = decryptResults(encryptedResults, phe, pk, sk, det, detKey);

        System.out.println(results.toString());
    }

    // return document IDs
    private static List<String> searchInvoices(String searchName, Det det, Key detKey, MongoDB mongoDB, MongoCollection<BasicDBObject> collection) throws Exception {
        String token = Hex.toHexString(det.encrypt(searchName.getBytes(), detKey.getEncoded()));

        return mongoDB.search(token, collection);
    }

    private static String loadFunction(String function) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(MainMongoDB.class.getClassLoader().getResource(function).toURI())));
    }


    private static List<BasicDBObject> createEncryptedDocs(Integer count, String name, PaillierCrypto phe, PaillierPublicKey pk, Det det, Key detKey) throws Exception {
        System.out.println("Inserting " + count.toString() + " " + name + " ...");
        List<BasicDBObject> docs = new ArrayList();

        /**
         * For each doc d:
         *
         *      encrypt_DET (d.name)
         *      encrypt_Paillier (d.amount)
         */

        while (count != 0) {

            Document doc = new Document(name, 10);

            PaillierContext context = phe.context(pk);
            EncryptedNumber encryptedAmount = phe.encrypt(doc.getAmount(), context);

            BasicDBObject encryptedMongodbBasicDBObject = new BasicDBObject();
            encryptedMongodbBasicDBObject.put("name",
                    Hex.toHexString(
                            det.encrypt(doc.getName().getBytes(), detKey.getEncoded())
                    ));
            encryptedMongodbBasicDBObject.put("amount",
                    Hex.toHexString(
                            new PaillierCrypto.SerializableEncryptedNumber(encryptedAmount, pk.getModulusSquared(), "amount").toBytes()
                    ));

            docs.add(encryptedMongodbBasicDBObject);
            count--;
        }

        return docs;
    }

    private static Results decryptResults(MapReduceIterable<BasicDBObject> encryptedResults, PaillierCrypto phe, PaillierPublicKey pk, PaillierPrivateKey sk, Det det, Key detKey) throws NoSuchPaddingException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // Iterating and decrypting the results (HE)
        Results results = new Results();
        for (BasicDBObject encryptedResult : encryptedResults) {
            // keys
            PaillierContext context = phe.context(pk);

            String detEncryptedNameResultInHex  = (String) encryptedResult.get("_id");
            String heEncryptedAggregateResultInHex = (String) encryptedResult.get("value");

            //System.out.println("\n\nEncrypted (DET) name:" + GSON_SERIALIZER.toJson(detEncryptedNameResultInHex));
            //System.out.println("Encrypted (Paillier) sum:" + GSON_SERIALIZER.toJson(heEncryptedAggregateResultInHex) + "\n\n");

            // decrypt DET
            byte[] nameBytes = det.decrypt(Hex.decode(detEncryptedNameResultInHex), detKey.getEncoded());

            // decrypt Paillier
            PaillierCrypto.SerializableEncryptedNumber encryptedNumber = new PaillierCrypto.SerializableEncryptedNumber(Hex.decode(heEncryptedAggregateResultInHex));
            EncodedNumber plainResult = phe.decrypt(encryptedNumber, sk, context);

            results.put(new String(nameBytes, Charsets.UTF_8), plainResult.decodeDouble());
        }

        return results;
    }


}
