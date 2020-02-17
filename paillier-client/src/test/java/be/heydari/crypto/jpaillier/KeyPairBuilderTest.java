package be.heydari.crypto.jpaillier;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;

public class KeyPairBuilderTest {


    @Test
    public void simpleFlow() {

        BigInteger plainData1 = BigInteger.valueOf(1000);
        BigInteger plainData2 = BigInteger.valueOf(3920);

        // gen key
        KeyPairBuilder keygen = new KeyPairBuilder();
        KeyPair keyPair = keygen.generateKeyPair();

        // encrypt
        PublicKey publicKey = keyPair.getPublicKey();
        BigInteger ciphertext1 = publicKey.encrypt(plainData1);
        BigInteger ciphertext2 = publicKey.encrypt(plainData2);

        BigInteger ciphertextSum = JPaillierAddition.add(ciphertext1, ciphertext2, publicKey.getnSquared());

        // decrypt
        BigInteger decryptedSum = keyPair.decrypt(ciphertextSum);
        BigInteger decrypted1 = keyPair.decrypt(ciphertext1);
        BigInteger decrypted2 = keyPair.decrypt(ciphertext2);


        Assert.assertNotNull(decryptedSum);
        Assert.assertNotNull(decrypted1);
        Assert.assertNotNull(decrypted2);

        Assert.assertEquals(plainData1.add(plainData2), decryptedSum);
        Assert.assertEquals(plainData1, decrypted1);
        Assert.assertEquals(plainData2, decrypted2);
    }
}
