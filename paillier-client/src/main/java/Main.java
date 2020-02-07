import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCollection;
import com.n1analytics.paillier.EncryptedNumber;
import com.n1analytics.paillier.PaillierContext;
import com.n1analytics.paillier.PaillierPrivateKey;
import com.n1analytics.paillier.PaillierPublicKey;
import org.bouncycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

/**
 * This code is purely written for testing purposes and
 * research prototype. Any use of this piece of art would
 * cause cancer.
 *
 * @author Emad heydari Beni
 */
public class Main {


    public static void main(String[] args) {
        PaillierCrypto phe = new PaillierCrypto(Configs.KEY_LENGHT);

        // Gen keys
        PaillierPrivateKey sk = phe.generatePrivateKey();
        PaillierPublicKey pk = phe.generatePublicKey(sk);

        // Mongo collection
        MongoDB mongoDB = new MongoDB();
        MongoCollection<BasicDBObject> collection = mongoDB.getCollection();


        // Create some encrypted docs
        List<BasicDBObject> docs = createEncryptedDocs(10, "Emad", phe, pk);


        // Insert all docs to MongoDB
        docs.forEach(doc -> mongoDB.insert(doc, collection));

        //

    }


    private static List<BasicDBObject> createEncryptedDocs(Integer count, String name, PaillierCrypto phe, PaillierPublicKey pk) {
        List<BasicDBObject> docs = new ArrayList();

        while(count != 0) {

            Document doc = new Document(name, 12);

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
