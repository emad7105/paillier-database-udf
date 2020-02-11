package configs;

public interface Configs {

    // Paillier
    int PAILLIER_KEY_LENGHT = 1024;


    // AES (DET) => Warning!! research prototype!
    int AES_KEY_LENGHT = 128;
    int AES_IV_LENGHT = 16;
    String AES_PADDING = "PKCS7Padding";
    String AES_MODE = "CBC";
    String RAND_ALG = "SHA1PRNG";


    // mongo.MongoDB
    String HOST = "192.168.104.69";
    int PORT = 27017;

    String DATABASE = "fintech";
    String COLLECTION = "sensitiveinvoices";
}
