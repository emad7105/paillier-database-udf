package crypto;

import com.google.gson.Gson;
import com.n1analytics.paillier.*;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * A wrapper class on top of javallier implementation of the
 * additive Paillier implementation.
 *
 *
 * @author Emad Heydari Beni
 */
public class PaillierCrypto {

    public static Gson gson = new Gson();

    private final int keyLength;

    public PaillierCrypto(int keyLength) {
        this.keyLength = keyLength;
    }

    public PaillierPrivateKey generatePrivateKey() {
        return PaillierPrivateKey.create(keyLength);
    }

    public PaillierPublicKey generatePublicKey(PaillierPrivateKey privateKey) {
        return privateKey.getPublicKey();
    }

    public PaillierContext context(PaillierPublicKey publicKey) {
        return publicKey.createSignedContext();
    }


    public EncryptedNumber encrypt(EncodedNumber encodedNumber, PaillierContext context) {
        return context.encrypt(encodedNumber);
    }

    public EncryptedNumber encrypt(double num, PaillierContext context) {
        return encrypt(context.encode(num), context);
    }

    public EncryptedNumber encrypt(long num, PaillierContext context) {
        return encrypt(context.encode(num), context);
    }

    public EncryptedNumber encrypt(BigDecimal num, PaillierContext context) {
        return encrypt(context.encode(num), context);
    }

    // TODO test
    // HACK
    public EncryptedNumber encrypt(Object num, PaillierContext context) {
        if (num instanceof Double) {
            return encrypt(((Double) num).doubleValue(), context);
        } else if (num instanceof Long) {
            return encrypt(((Long) num).longValue(), context);
        } else if (num instanceof Integer) {
            return encrypt(((Integer) num).doubleValue(), context);
        }
        /*
        TODO recursive: cast doesn't work correctly
        else if (num instanceof BigInteger) {
            return encrypt((BigInteger)num, context);
        } else if (num instanceof BigDecimal) {
            return encrypt((BigDecimal) num, context);
        }*/
        throw new IllegalArgumentException("Given type of your number is not supported: "+ num.getClass().getName());
    }

    public EncodedNumber decrypt(EncryptedNumber encryptedNum, PaillierPrivateKey privateKey) {
        return privateKey.decrypt(encryptedNum);
    }

    // TODO test
    public EncodedNumber decrypt(SerializableEncryptedNumber encryptedNum, PaillierPrivateKey privateKey, PaillierContext context) {
        return privateKey.decrypt(
                new EncryptedNumber(context, encryptedNum.getCipherText(), encryptedNum.getExponent()));
    }

    public PaillierPrivateKey loadPrivateKeyFromJson(String jsonPhePrivateKey) {
        return gson.fromJson(jsonPhePrivateKey, PaillierPrivateKey.class);
    }


    // TODO Test
    public static class SerializableEncryptedNumber {
        private static Gson gson = new Gson();

        private final int e; // exponent
        private final BigInteger c; // cipherText
        private final BigInteger ms; // modulusSquared
        private final String d; // description (number field name: age, weight, ...)

        public SerializableEncryptedNumber(int exponent, BigInteger cipherText, String description, BigInteger modulusSquared) {
            this.e = exponent;
            this.c = cipherText;
            this.ms = modulusSquared;
            this.d = description;
        }

        public SerializableEncryptedNumber(EncryptedNumber encryptedNumber, BigInteger modulusSquared, String description) {
            this.e = encryptedNumber.getExponent();
            this.c = encryptedNumber.calculateCiphertext();
            this.ms = modulusSquared;
            this.d = description;
        }

        // TODO Test
        public SerializableEncryptedNumber(byte[] jsonBytes) {
            SerializableEncryptedNumber serializableEncryptedNumber = fromJson(new String(jsonBytes));
            this.e = serializableEncryptedNumber.getExponent();
            this.c = serializableEncryptedNumber.getCipherText();
            this.ms = serializableEncryptedNumber.getModulusSquared();
            this.d = serializableEncryptedNumber.getDescription();
        }

        private String toJson() {
            return gson.toJson(this);
        }

        private SerializableEncryptedNumber fromJson(String json) {
            return gson.fromJson(json, getClass());
        }

        public byte[] toBytes() {
            return toJson().getBytes();
        }

        public int getExponent() {
            return e;
        }

        public BigInteger getCipherText() {
            return c;
        }

        public String getDescription() {
            return d;
        }

        public BigInteger getModulusSquared() {
            return ms;
        }

        public static SerializableEncryptedNumber build(String json) {
            return gson.fromJson(json, SerializableEncryptedNumber.class);
        }
    }
}
