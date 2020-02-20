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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;


/**
 * Forwarding ports: ssh ubuntu@192.168.104.96 -i vms -L 9042:localhost:9042
 *
 * @author Emad Heydari Beni
 */
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
        //createEncryptedDocs(3, "Ansar", pk, det, detKey, cassandra);
        //createEncryptedDocs(10, "Bert", pk, det, detKey, cassandra);
        System.out.println("\nInsertion: done!\n");


        // sum
        Instant start_all = Instant.now();
        ResultSet rs = cassandra.getSession().execute("select homsum(invoice_amount, invoice_nsquared) as homsum from facturis.invoices");
        Assert.checkNonNull(rs);
        Instant end_sum = Instant.now();
        System.out.println("Execution time (ms) => additions: " + String.valueOf(Duration.between(start_all, end_sum).toMillis()));


        HomSumResult homsumResult = new HomSumResult();
        rs.forEach(row -> homsumResult.setSum(row.getVarint("homsum")));
        Assert.checkNonNull(homsumResult.getSum());

        BigInteger decryptSum = paillierKeyPair.decrypt(homsumResult.getSum());
        Instant end = Instant.now();

        System.out.println("\n\n\nEncrypted Sum: " + homsumResult.getSum());
        System.out.println("Decrypted Sum: " + decryptSum.toString());
        System.out.println("Execution time (ms): " + String.valueOf(Duration.between(start_all, end).toMillis()));
    }

    private static String loadKeys(String keys) throws IOException, URISyntaxException {
        return new String(Files.readAllBytes(Paths.get(MainMongoDB.class.getClassLoader().getResource(keys).toURI())));
    }

    private static void createEncryptedDocs(Integer count, String invoiceName, PublicKey jpaillierPublicKey, Det det, Key detKey, Cassandra cassandra) throws Exception {
        System.out.println("Inserting " + count.toString() + " " + invoiceName + " ...");

        /**
         * For each doc d:
         *
         *      encrypt_DET (d.invoice_name)
         *      encrypt_DET (d.invoice_status)
         *      encrypt_Paillier (d.amount)
         */
        Random random = new Random();
        CassandraDocument encryptedCassandraDocument = null;
        while (count != 0) {

            System.out.println("Doc " + count + " encrypting ...");
            encryptedCassandraDocument = new CassandraDocument(
                    UUID.randomUUID().toString(),
                    invoiceName,
                    //random.nextBoolean() ? "paid" : "unpaid",
                    "paid",
                    //BigInteger.valueOf(random.nextLong() % 10000L)
                    BigInteger.valueOf(10L)
            );

            System.out.println("Doc " + count + " PHE encrypting ...");
            BigInteger encryptedAmount = jpaillierPublicKey.encrypt(encryptedCassandraDocument.getInvoiceAmount());

            // name (DET)
            System.out.println("Doc " + count + " DET encrypting ...");
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

            System.out.println("Doc " + count + " inserting ...");
            cassandra.getSession().execute(QueryBuilder.insertInto(Configs.CASSANDRA_KEYSPACE, Configs.CASSANDRA_TABLE)
                    .value("invoice_id", encryptedCassandraDocument.getInvoiceId())
                    .value("invoice_name", encryptedCassandraDocument.getInvoiceName())
                    .value("invoice_status", encryptedCassandraDocument.getInvoiceStatus())
                    .value("invoice_amount", encryptedCassandraDocument.getInvoiceAmount())
                    .value("invoice_nsquared", encryptedCassandraDocument.getnSquare())
            );


            count--;
        }
    }

    private static void cleanupLoadEverything(Cassandra cassandra) throws IOException, URISyntaxException {
        cassandra.loadCustomKeyspace();
    }

}
