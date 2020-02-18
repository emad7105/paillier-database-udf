package be.heydari;

import be.heydari.cassandra.HomSumResult;
import be.heydari.cassandra.Cassandra;
import be.heydari.cassandra.CassandraDocument;
import be.heydari.configs.Configs;
import be.heydari.crypto.Det;
import be.heydari.crypto.jpaillier.KeyPair;
import be.heydari.crypto.jpaillier.KeyPairBuilder;
import be.heydari.crypto.jpaillier.PublicKey;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.gson.Gson;
import com.sun.tools.javac.util.Assert;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MainCassandra {


    public static void main(String[] args) throws Exception {
        Cassandra cassandra = new Cassandra();

        // drop, create keyspace, create table and create UDFs
        cleanupLoadEverything(cassandra);
        test(cassandra);
    }

    private static void test(Cassandra cassandra) throws Exception {
        // preparing be.heydari.crypto
        KeyPairBuilder jpaillier = new KeyPairBuilder();
        Det det = new Det("BC", Configs.RAND_ALG, Configs.AES_MODE,
                Configs.AES_PADDING, Configs.AES_KEY_LENGHT, Configs.AES_IV_LENGHT);

        // Generate Keys: Paillier
        Gson jsonMapper = new Gson();
        KeyPair paillierKeyPair = jsonMapper.fromJson(loadKeys("cassandra/jpaillier-keys-for-test.json"), KeyPair.class);
        PublicKey pk = paillierKeyPair.getPublicKey();
        // Generate Key: DET
        Key detKey = jsonMapper.fromJson(loadKeys("cassandra/det-key-for-test.json"), SecretKeySpec.class);

        // encrypt + persist docs
        createEncryptedDocs(100, "Emad", pk, det, detKey, cassandra);
        createEncryptedDocs(3, "Ansar", pk, det, detKey, cassandra);
        createEncryptedDocs(10, "Bert", pk, det, detKey, cassandra);
        System.out.println("\nInsertion: done!\n");


        // average
        ResultSet rs = cassandra.getSession().execute("select homsum(invoice_amount, invoice_nsquared) as homsum from facturis.invoices");
        Assert.checkNonNull(rs);

        HomSumResult homsumResult = new HomSumResult();
        rs.forEach(row -> homsumResult.setSum(row.getVarint("homsum")));
        Assert.checkNonNull(homsumResult.getSum());

        BigInteger decryptSum = paillierKeyPair.decrypt(homsumResult.getSum());

        System.out.println("\n\n\nEncrypted Sum: "+ homsumResult.getSum());
        System.out.println("Decrypted Sum: " + decryptSum.toString());
    }

    private static String loadKeys(String keys) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(MainMongoDB.class.getClassLoader().getResource(keys).toURI())));
    }

    private static void createEncryptedDocs(Integer count, String invoiceName, PublicKey jpaillierPublicKey, Det det, Key detKey, Cassandra cassandra) throws Exception {
        System.out.println("Inserting " + count.toString() + " " + invoiceName + " ...");
        List<CassandraDocument> docs = new ArrayList();

        /**
         * For each doc d:
         *
         *      encrypt_DET (d.invoice_name)
         *      encrypt_DET (d.invoice_status)
         *      encrypt_Paillier (d.amount)
         */
        Random random = new Random();
        while (count != 0) {

            CassandraDocument encryptedCassandraDocument = new CassandraDocument(
                    UUID.randomUUID().toString(),
                    invoiceName,
                    random.nextBoolean() ? "paid" : "unpaid",
                    //BigInteger.valueOf(random.nextLong() % 10000L)
                    BigInteger.valueOf(10L)
            );

            BigInteger encryptedAmount = jpaillierPublicKey.encrypt(encryptedCassandraDocument.getInvoiceAmount());

            // name (DET)
            encryptedCassandraDocument.setInvoiceName(
                    Hex.toHexString(
                            det.encrypt(encryptedCassandraDocument.getInvoiceName().getBytes(), detKey.getEncoded())
                    ));
            // status (DET)
            encryptedCassandraDocument.setInvoiceName(
                    Hex.toHexString(
                            det.encrypt(encryptedCassandraDocument.getInvoiceStatus().getBytes(), detKey.getEncoded())
                    ));

            // amount (HOM)
            encryptedCassandraDocument.setInvoiceAmount(encryptedAmount);
            encryptedCassandraDocument.setnSquare(jpaillierPublicKey.getnSquared());

            docs.add(encryptedCassandraDocument);
            count--;
        }


        for (CassandraDocument encryptedDoc : docs) {
            cassandra.getSession().execute(QueryBuilder.insertInto(Configs.CASSANDRA_KEYSPACE, Configs.CASSANDRA_TABLE)
                    .value("invoice_id", encryptedDoc.getInvoiceId())
                    .value("invoice_name", encryptedDoc.getInvoiceName())
                    .value("invoice_status", encryptedDoc.getInvoiceStatus())
                    .value("invoice_amount", encryptedDoc.getInvoiceAmount())
                    .value("invoice_nsquared", encryptedDoc.getnSquare())
            );

            //cassandra.getSession().execute(queryString);

            //cassandra.getSession().execute("INSERT INTO invoice (invoice_id, invoice_name, invoice_status, invoice_amount, invoice_nsquared) " +
            //        "VALUES ("+encryptedDoc.getInvoiceId()+ ",'"+ encryptedDoc.getInvoiceName()+"', '"+encryptedDoc.getInvoiceStatus()+"', "+encryptedDoc.getInvoiceAmount()+", "+encryptedDoc.getnSquare()+")");
        }
    }

    private static void cleanupLoadEverything(Cassandra cassandra) throws IOException, URISyntaxException {
        cassandra.loadCustomKeyspace();
    }

}
