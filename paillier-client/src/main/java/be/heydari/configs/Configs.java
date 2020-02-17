package be.heydari.configs;

public interface Configs {

    /**
     * jPaillier
     */
    int PAILLIER_KEYS_LENGTH  = 1024;

    /**
     * Javalier
     */
    int JAVALIER_KEY_LENGHT = 1024;


    /**
     * AES (DET) => Warning!! research prototype!
     */
    int AES_KEY_LENGHT = 128;
    int AES_IV_LENGHT = 16;
    String AES_PADDING = "PKCS7Padding";
    String AES_MODE = "CBC";
    String RAND_ALG = "SHA1PRNG";


    /**
     * MongoDB
     */
    String HOST = "192.168.104.69";
    int PORT = 27017;
    Boolean MONGO_AUTH_ENABLED = false;
    String MONGO_USER = "ansar";
    String MONGO_PASS = "";
    String MONGO_AUTH_DB = "admin";

    String DATABASE = "fintech";
    String COLLECTION = "sensitiveinvoices";


    /**
     * Cassandra
     *
     * Port forwarding
     * ssh -L 9042:localhost:9042 -i vms ubuntu@192.168.104.96
     */

    String CASSANDRA_NODE = "127.0.0.1";
    Integer CASSANDRA_PORT = 9042;
    String CASSANDRA_KEYSPACE = "facturis";
    String CASSANDRA_TABLE = "invoices";
}
