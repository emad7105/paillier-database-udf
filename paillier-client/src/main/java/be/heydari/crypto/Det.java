package be.heydari.crypto;

import com.google.common.primitives.Bytes;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Arrays;

/**
 * Warning!!
 * This is a research prototype; use at your own risk.
 *
 * @author Emad Heydari Beni
 */
public class Det {

    public static final String ALG = "AES";

    private String provider;
    private String symmetricCryptoInstance;
    private SecureRandom random;
    private int symmetricKeySize;
    private int symmetricIvLength;
    private byte[] iv;

    public Det(String provider, String secureRandomAlg, String symmetricMode, String symmetricPadding, int symmetricKeySize, int symmetricIvLength) throws Exception {
        this.provider = provider;
        this.symmetricCryptoInstance = ALG + "/" + symmetricMode + "/" + symmetricPadding;
        this.symmetricKeySize = symmetricKeySize;
        this.symmetricIvLength = symmetricIvLength;
        addSecurityProvider(secureRandomAlg);
        this.iv = generateIV(symmetricIvLength);
    }

    public Key genKey() throws NoSuchProviderException, NoSuchAlgorithmException {
        KeyGenerator aesKeyGenerator = KeyGenerator.getInstance(ALG, provider);
        aesKeyGenerator.init(symmetricKeySize);
        return aesKeyGenerator.generateKey();
    }

    public byte[] encrypt(byte[] data, byte[] key) throws Exception {
        return Bytes.concat(iv, encrypt(data, key, iv));
    }

    public byte[] encrypt(byte[] data, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(symmetricCryptoInstance);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, ALG), ivSpec);
        return cipher.doFinal(data);
    }

    public byte[] decrypt(byte[] data, byte[] key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        byte[] iv = Arrays.copyOf(data, symmetricIvLength);
        byte[] encData = Arrays.copyOfRange(data, symmetricIvLength, data.length);
        return decrypt(encData, key, iv);
    }

    public byte[] decrypt(byte[] data, byte[] key, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(symmetricCryptoInstance);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, ALG), ivSpec);
        return cipher.doFinal(data);
    }

    public byte[] generateIV(int ivSize) throws Exception {
        byte[] ivBytes = new byte[ivSize];
        random.nextBytes(ivBytes);
        return ivBytes;
    }

    private void addSecurityProvider(String secureRandomAlg) {
        Security.addProvider(new BouncyCastleProvider());
        try {
            random = SecureRandom.getInstance(secureRandomAlg);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
