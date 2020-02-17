package be.heydari.crypto.jpaillier;

import java.math.BigInteger;

public class JPaillierAddition {

    public static BigInteger add(BigInteger encryptedA, BigInteger encryptedB, BigInteger nSquared) {
        return encryptedA.multiply(encryptedB).mod(nSquared);
    }
}
