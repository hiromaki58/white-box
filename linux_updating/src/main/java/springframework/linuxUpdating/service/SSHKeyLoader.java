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

public class SSHKeyLoader {

    /**
     * OpenSSH形式の秘密鍵と公開鍵を読み込んでKeyPairを生成
     * @param privateKeyPath 秘密鍵ファイルのパス
     * @param publicKeyPath 公開鍵ファイルのパス
     * @param passphrase パスフレーズ
     * @return 公開鍵と秘密鍵のペア
     * @throws Exception 公開鍵または秘密鍵の読み込みエラー
     */
    public static KeyPair loadKeyPair(String privateKeyPath, String publicKeyPath, char[] passphrase) throws Exception {
        // 秘密鍵の読み込み
        PEMParser pemParser = new PEMParser(new FileReader(privateKeyPath));
        Object object = pemParser.readObject();
        pemParser.close();

        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
        PrivateKey privateKey;

        if (object instanceof PEMEncryptedKeyPair) {
            // パスフレーズ付きの秘密鍵を復号
            PEMEncryptedKeyPair encryptedKeyPair = (PEMEncryptedKeyPair) object;
            PEMKeyPair pemKeyPair = encryptedKeyPair.decryptKeyPair(new JcePEMDecryptorProviderBuilder().build(passphrase));
            privateKey = converter.getPrivateKey(pemKeyPair.getPrivateKeyInfo());
        }
        else if (object instanceof PEMKeyPair) {
            // パスフレーズなしの秘密鍵
            privateKey = converter.getPrivateKey(((PEMKeyPair) object).getPrivateKeyInfo());
        }
        else {
            throw new IllegalArgumentException("Invalid private key format.");
        }

        // 公開鍵の読み込み
        PublicKey publicKey = loadOpenSSHPublicKey(publicKeyPath);

        return new KeyPair(publicKey, privateKey);
    }

    /**
     * OpenSSH形式の公開鍵を読み込み、JavaのPublicKeyオブジェクトに変換する
     * @param publicKeyPath 公開鍵ファイルのパス
     * @return 公開鍵オブジェクト
     * @throws Exception 公開鍵の読み込みエラー
     */
    public static PublicKey loadOpenSSHPublicKey(String publicKeyPath) throws Exception {
        // 公開鍵ファイルを読み込み、内容を文字列として取得
        String publicKeyContent = new String(java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(publicKeyPath))).trim();

        // "ssh-rsa" の形式の公開鍵からBase64部分を抽出
        String[] parts = publicKeyContent.split(" ");
        if (parts.length < 2 || !"ssh-rsa".equals(parts[0])) {
            throw new IllegalArgumentException("Invalid public key format");
        }

        // Base64エンコードされた公開鍵部分を取得してデコード
        byte[] decodedKey = Base64.getDecoder().decode(parts[1]);

        // デコードされた公開鍵からモジュラス (n) と公開指数 (e) を抽出
        BigInteger e = extractExponent(decodedKey);
        BigInteger n = extractModulus(decodedKey);

        // RSAPublicKeySpec を使って公開鍵を生成
        RSAPublicKeySpec spec = new RSAPublicKeySpec(n, e);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(spec);
    }

    // デコードされた公開鍵から公開指数 (e) を抽出
    private static BigInteger extractExponent(byte[] decodedKey) {
        // "ssh-rsa" の識別子 (7 バイト) の後に公開指数が続く
        int pos = 11;  // 識別子の後の位置
        int eLength = ((decodedKey[pos++] & 0xFF) << 24) | ((decodedKey[pos++] & 0xFF) << 16) | ((decodedKey[pos++] & 0xFF) << 8) | (decodedKey[pos++] & 0xFF);
        byte[] eBytes = new byte[eLength];
        System.arraycopy(decodedKey, pos, eBytes, 0, eLength);
        return new BigInteger(1, eBytes);
    }

    // デコードされた公開鍵からモジュラス (n) を抽出
    private static BigInteger extractModulus(byte[] decodedKey) {
        // 公開指数の後にモジュラスが続く
        int pos = 11;  // 識別子の後の位置
        int eLength = ((decodedKey[pos++] & 0xFF) << 24) | ((decodedKey[pos++] & 0xFF) << 16) | ((decodedKey[pos++] & 0xFF) << 8) | (decodedKey[pos++] & 0xFF);
        pos += eLength;  // 公開指数の位置をスキップ

        int nLength = ((decodedKey[pos++] & 0xFF) << 24) | ((decodedKey[pos++] & 0xFF) << 16) | ((decodedKey[pos++] & 0xFF) << 8) | (decodedKey[pos++] & 0xFF);
        byte[] nBytes = new byte[nLength];
        System.arraycopy(decodedKey, pos, nBytes, 0, nLength);
        return new BigInteger(1, nBytes);
    }
}
