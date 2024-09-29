package springframework.linuxupdating.service;

import java.io.FileReader;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;

import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

public class SshKeyCreater {
    /**
     * To Create the KeyPair object
     * @param privateKeyPath
     * @param publicKeyPath
     * @param passphrase
     * @return KeyPair
     * @throws Exception
     */
    public static KeyPair loadKeyPair(String privateKeyPath, String publicKeyPath, char[] passphrase) throws Exception {
        // Read the secret key
        PEMParser pemParser = new PEMParser(new FileReader(privateKeyPath));
        Object object = pemParser.readObject();
        pemParser.close();

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey;

        // Decode the secret key with passphrase
        if (object instanceof PEMEncryptedKeyPair) {
            PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) object;
            PEMKeyPair pemKeyPair = encryptedKeyPair.decryptKeyPair(new JcePEMDecryptorProviderBuilder().build(passphrase));
            privateKey = converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
        }
        // Without passphrase
        else if (object instanceof PEMKeyPair) {
            privateKey = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
        }
        else {
            throw new IllegalArgumentException("Invalid private key format.");
        }

        // Read the public key
        PublicKey publicKey = loadOpenSSHPublicKey(publicKeyPath);

        return new KeyPair(publicKey, privateKey);
    }

    /**
     * Read the OpenSSH pubic key
     * Change to Java PublicKey object
     * @param publicKeyPath Path to the public key file
     * @return public key object
     * @throws Exception error to read the public key
     */
    public static PublicKey loadOpenSSHPublicKey(String publicKeyPath) throws Exception {
        // Read the public key file and covert it into string
        String publicKeyContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(publicKeyPath))).trim();

        // Extract the Base64 part from ssh-rsa
        String[] parts = publicKeyContent.split(" ");
        if (parts.length < 2 || !"ssh-rsa".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid public key format");
        }

        // Decode the Base64 public key part
        byte[] decodedKey = Base64.getDecoder().decode(parts[1]);

        // Pick up modulus and public exponent from decoded public key
        BigInteger e = extractExponent(decodedKey);
        BigInteger n = extractModulus(decodedKey);

        // Create the public key java object with RSAPublicKeySpec
        RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    /**
     * Pick up the public exponent from decoded public key
     * @param decodedKey
     * @return
     */
    private static BigInteger extractExponent(byte[] decodedKey) {
        // Public exponent follows the identifier 'ssh-rsa' (7 bytes)
        int pos = 11;  // Position after the identifier
        int eLength = ((decodedKey[pos++] & 0xFF) << 24) | ((decodedKey[pos++] & 0xFF) << 16) | ((decodedKey[pos++] & 0xFF) << 8) | (decodedKey[pos++] & 0xFF);
        byte[] eBytes = new byte[eLength];
        System.arraycopy(decodedKey, pos, eBytes, 0, eLength);
        return new BigInteger(1, eBytes);
    }

    /**
     * Pick up the modulus from decoded public key
     * @param decodedKey
     * @return
     */
    private static BigInteger extractModulus(byte[] decodedKey) {
        // Modulus follows the public exponent
        int pos = 11;  // Position after the identifier
        int eLength = ((decodedKey[pos++] & 0xFF) << 24) | ((decodedKey[pos++] & 0xFF) << 16) | ((decodedKey[pos++] & 0xFF) << 8) | (decodedKey[pos++] & 0xFF);
        // Skip the public exponent position
        pos += eLength;

        int nLength = ((decodedKey[pos++] & 0xFF) << 24) | ((decodedKey[pos++] & 0xFF) << 16) | ((decodedKey[pos++] & 0xFF) << 8) | (decodedKey[pos++] & 0xFF);
        byte[] nBytes = new byte[nLength];
        System.arraycopy(decodedKey, pos, nBytes, 0, nLength);
        return new BigInteger(1, nBytes);
    }
}
