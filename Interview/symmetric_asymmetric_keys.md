# 대칭키와 비대칭키

## 1. 개요
암호화 방식은 크게 대칭키(Symmetric Key)와 비대칭키(Asymmetric Key) 두 가지로 나눌 수 있습니다. 각각의 방식은 서로 다른 특징과 용도를 가지고 있습니다.

## 2. 대칭키 (Symmetric Key)

### 2.1. 정의
- 암호화와 복호화에 **동일한 키**를 사용하는 방식
- 단일 키로 데이터를 암호화하고 복호화함

### 2.2. 특징
- 빠른 암호화/복호화 속도
- 구현이 간단하고 리소스 사용이 적음
- 키의 길이가 상대적으로 짧음

### 2.3. 대표적인 알고리즘
- AES (Advanced Encryption Standard)
  ```java
  // AES 암호화 예제
  public class AESExample {
      private static final String ALGORITHM = "AES";
      private static final String KEY = "ThisIs128BitKey!"; // 128비트 키

      public static String encrypt(String data) throws Exception {
          SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(ALGORITHM);
          cipher.init(Cipher.ENCRYPT_MODE, key);

          byte[] encryptedBytes = cipher.doFinal(data.getBytes());
          return Base64.getEncoder().encodeToString(encryptedBytes);
      }

      public static String decrypt(String encryptedData) throws Exception {
          SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(ALGORITHM);
          cipher.init(Cipher.DECRYPT_MODE, key);

          byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
          return new String(decryptedBytes);
      }
  }
  ```

- DES (Data Encryption Standard)
  ```java
  // DES 암호화 예제 (현재는 보안상 권장되지 않음)
  public class DESExample {
      private static final String ALGORITHM = "DES";
      private static final String KEY = "8ByteKey"; // 64비트 키

      public static String encrypt(String data) throws Exception {
          SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(ALGORITHM);
          cipher.init(Cipher.ENCRYPT_MODE, key);

          byte[] encryptedBytes = cipher.doFinal(data.getBytes());
          return Base64.getEncoder().encodeToString(encryptedBytes);
      }

      public static String decrypt(String encryptedData) throws Exception {
          SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(ALGORITHM);
          cipher.init(Cipher.DECRYPT_MODE, key);

          byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
          return new String(decryptedBytes);
      }
  }
  ```

- 3DES (Triple DES)
  ```java
  // Triple DES 암호화 예제
  public class TripleDESExample {
      private static final String ALGORITHM = "DESede"; // Triple DES
      private static final String KEY = "ThisIsA24ByteSecretKey!!"; // 24바이트 키

      public static String encrypt(String data) throws Exception {
          SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(ALGORITHM);
          cipher.init(Cipher.ENCRYPT_MODE, key);

          byte[] encryptedBytes = cipher.doFinal(data.getBytes());
          return Base64.getEncoder().encodeToString(encryptedBytes);
      }

      public static String decrypt(String encryptedData) throws Exception {
          SecretKeySpec key = new SecretKeySpec(KEY.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(ALGORITHM);
          cipher.init(Cipher.DECRYPT_MODE, key);

          byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
          return new String(decryptedBytes);
      }
  }
  ```

### 2.4. 장단점
#### 장점
- 암호화/복호화 속도가 빠름
- 리소스 사용량이 적음
- 구현이 간단함

#### 단점
- 키 공유의 어려움
- 많은 사용자와 통신 시 키 관리가 복잡
- 키가 유출되면 모든 데이터가 노출될 위험

## 3. 비대칭키 (Asymmetric Key)

### 3.1. 정의
- 암호화와 복호화에 **서로 다른 키**를 사용하는 방식
- 공개키(Public Key)와 개인키(Private Key)로 구성

### 3.2. 특징
- 두 개의 키를 사용 (공개키/개인키)
- 수학적 알고리즘에 기반
- 더 높은 보안성 제공

### 3.3. 대표적인 알고리즘
- RSA
  ```java
  // RSA 암호화 예제
  public class RSAExample {
      private static final int KEY_SIZE = 2048;

      public static KeyPair generateKeyPair() throws Exception {
          KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
          generator.initialize(KEY_SIZE);
          return generator.generateKeyPair();
      }

      public static String encrypt(String data, PublicKey publicKey) throws Exception {
          Cipher cipher = Cipher.getInstance("RSA");
          cipher.init(Cipher.ENCRYPT_MODE, publicKey);

          byte[] encryptedBytes = cipher.doFinal(data.getBytes());
          return Base64.getEncoder().encodeToString(encryptedBytes);
      }

      public static String decrypt(String encryptedData, PrivateKey privateKey) throws Exception {
          Cipher cipher = Cipher.getInstance("RSA");
          cipher.init(Cipher.DECRYPT_MODE, privateKey);

          byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
          return new String(decryptedBytes);
      }

      // 디지털 서명 예제
      public static String sign(String data, PrivateKey privateKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withRSA");
          signature.initSign(privateKey);
          signature.update(data.getBytes());

          byte[] signedBytes = signature.sign();
          return Base64.getEncoder().encodeToString(signedBytes);
      }

      public static boolean verify(String data, String signed, PublicKey publicKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withRSA");
          signature.initVerify(publicKey);
          signature.update(data.getBytes());

          return signature.verify(Base64.getDecoder().decode(signed));
      }
  }
  ```

- ECC (Elliptic Curve Cryptography)
  ```java
  // ECC 암호화 예제
  public class ECCExample {
      public static KeyPair generateKeyPair() throws Exception {
          KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
          generator.initialize(256); // NIST P-256 curve
          return generator.generateKeyPair();
      }

      // ECDH (Elliptic Curve Diffie-Hellman) 키 교환 예제
      public static SecretKey generateSharedSecret(PrivateKey privateKey, PublicKey publicKey) 
              throws Exception {
          KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
          keyAgreement.init(privateKey);
          keyAgreement.doPhase(publicKey, true);

          byte[] sharedSecret = keyAgreement.generateSecret();
          return new SecretKeySpec(sharedSecret, "AES");
      }

      // ECDSA (Elliptic Curve Digital Signature Algorithm) 예제
      public static String sign(String data, PrivateKey privateKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withECDSA");
          signature.initSign(privateKey);
          signature.update(data.getBytes());

          byte[] signedBytes = signature.sign();
          return Base64.getEncoder().encodeToString(signedBytes);
      }

      public static boolean verify(String data, String signed, PublicKey publicKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withECDSA");
          signature.initVerify(publicKey);
          signature.update(data.getBytes());

          return signature.verify(Base64.getDecoder().decode(signed));
      }
  }
  ```

- DSA (Digital Signature Algorithm)
  ```java
  // DSA 서명 예제
  public class DSAExample {
      public static KeyPair generateKeyPair() throws Exception {
          KeyPairGenerator generator = KeyPairGenerator.getInstance("DSA");
          generator.initialize(2048);
          return generator.generateKeyPair();
      }

      public static String sign(String data, PrivateKey privateKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withDSA");
          signature.initSign(privateKey);
          signature.update(data.getBytes());

          byte[] signedBytes = signature.sign();
          return Base64.getEncoder().encodeToString(signedBytes);
      }

      public static boolean verify(String data, String signed, PublicKey publicKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withDSA");
          signature.initVerify(publicKey);
          signature.update(data.getBytes());

          return signature.verify(Base64.getDecoder().decode(signed));
      }
  }
  ```

### 3.4. 장단점
#### 장점
- 키 분배 문제 해결
- 높은 보안성
- 디지털 서명 가능

#### 단점
- 암호화/복호화 속도가 느림
- 리소스 사용량이 많음
- 키의 길이가 더 김

## 4. 실제 사용 사례

### 4.1. 대칭키 사용 사례
- 파일 암호화
  ```java
  // 파일 암호화 예제
  public class SecureFileEncryption {
      private static final String ALGORITHM = "AES";
      private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

      public static void encryptFile(String key, File inputFile, File outputFile) throws Exception {
          SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
          Cipher cipher = Cipher.getInstance(TRANSFORMATION);

          byte[] iv = new byte[16];
          SecureRandom secureRandom = new SecureRandom();
          secureRandom.nextBytes(iv);
          IvParameterSpec ivSpec = new IvParameterSpec(iv);

          cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

          try (FileInputStream fis = new FileInputStream(inputFile);
               FileOutputStream fos = new FileOutputStream(outputFile)) {
              // IV를 파일 시작에 저장
              fos.write(iv);

              byte[] buffer = new byte[4096];
              int bytesRead;
              while ((bytesRead = fis.read(buffer)) != -1) {
                  byte[] output = cipher.update(buffer, 0, bytesRead);
                  if (output != null) {
                      fos.write(output);
                  }
              }
              byte[] finalOutput = cipher.doFinal();
              if (finalOutput != null) {
                  fos.write(finalOutput);
              }
          }
      }
  }
  ```

- 디스크 암호화
- 세션 암호화
  ```java
  // 세션 암호화 예제
  public class SessionEncryption {
      private static final String ALGORITHM = "AES";
      private static final int KEY_SIZE = 256;

      public static String encryptSessionData(String sessionId, String userData) throws Exception {
          KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
          keyGen.init(KEY_SIZE);
          SecretKey secretKey = keyGen.generateKey();

          Cipher cipher = Cipher.getInstance(ALGORITHM);
          cipher.init(Cipher.ENCRYPT_MODE, secretKey);

          byte[] encryptedData = cipher.doFinal(userData.getBytes());
          return Base64.getEncoder().encodeToString(encryptedData);
      }
  }
  ```

- HTTPS 통신 시 실제 데이터 암호화

### 4.2. 비대칭키 사용 사례
- SSL/TLS 인증서
  ```java
  // SSL/TLS 인증서 생성 예제
  public class SSLCertificateGenerator {
      public static X509Certificate generateSelfSignedCertificate(KeyPair keyPair) throws Exception {
          X500Name subject = new X500Name("CN=localhost");
          BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());
          Date notBefore = new Date(System.currentTimeMillis() - 86400000L); // 1일 전
          Date notAfter = new Date(System.currentTimeMillis() + 86400000L * 365); // 1년 후

          SubjectPublicKeyInfo subPubKeyInfo = SubjectPublicKeyInfo
              .getInstance(keyPair.getPublic().getEncoded());

          X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
              subject, serial, notBefore, notAfter, subject, subPubKeyInfo);

          ContentSigner signer = new JcaContentSignerBuilder("SHA256WithRSA")
              .build(keyPair.getPrivate());

          return new JcaX509CertificateConverter()
              .getCertificate(builder.build(signer));
      }
  }
  ```

- 디지털 서명
  ```java
  // PGP 디지털 서명 예제
  public class PGPSignatureExample {
      public static byte[] createSignature(byte[] data, PrivateKey privateKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withRSA");
          signature.initSign(privateKey);
          signature.update(data);
          return signature.sign();
      }

      public static boolean verifySignature(byte[] data, byte[] signatureBytes, 
              PublicKey publicKey) throws Exception {
          Signature signature = Signature.getInstance("SHA256withRSA");
          signature.initVerify(publicKey);
          signature.update(data);
          return signature.verify(signatureBytes);
      }
  }
  ```

- 이메일 암호화 (PGP)
- SSH 인증
  ```java
  // SSH 키 생성 예제
  public class SSHKeyGenerator {
      public static KeyPair generateSSHKeyPair() throws Exception {
          KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
          generator.initialize(2048);
          return generator.generateKeyPair();
      }

      public static String getPublicKeyString(KeyPair keyPair) throws Exception {
          byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
          return "ssh-rsa " + Base64.getEncoder().encodeToString(publicKeyBytes);
      }
  }
  ```

- HTTPS 통신 시 대칭키 교환

## 5. 하이브리드 암호화 시스템
실제 많은 시스템에서는 대칭키와 비대칭키를 함께 사용합니다:
1. 비대칭키로 안전하게 대칭키를 교환
2. 실제 데이터는 대칭키로 암호화
3. 예: HTTPS 프로토콜

### 5.1 하이브리드 암호화 구현 예제
```java
public class HybridEncryptionExample {
    // 대칭키 암호화에 사용할 알고리즘과 키 크기
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_KEY_SIZE = 256;

    // 비대칭키 암호화에 사용할 알고리즘과 키 크기
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_KEY_SIZE = 2048;

    // 대칭키 생성
    public static SecretKey generateSymmetricKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
        keyGen.init(SYMMETRIC_KEY_SIZE);
        return keyGen.generateKey();
    }

    // 비대칭키 쌍 생성
    public static KeyPair generateAsymmetricKeyPair() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
        keyGen.initialize(ASYMMETRIC_KEY_SIZE);
        return keyGen.generateKeyPair();
    }

    // 하이브리드 암호화
    public static class EncryptedMessage {
        public byte[] encryptedKey;      // 대칭키를 비대칭키로 암호화한 결과
        public byte[] encryptedData;     // 실제 데이터를 대칭키로 암호화한 결과
        public byte[] iv;                // 초기화 벡터
    }

    public static EncryptedMessage encrypt(String data, PublicKey publicKey) throws Exception {
        // 1. 대칭키 생성
        SecretKey symmetricKey = generateSymmetricKey();

        // 2. 데이터를 대칭키로 암호화
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
        byte[] encryptedData = aesCipher.doFinal(data.getBytes());

        // 3. 대칭키를 공개키로 암호화
        Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = rsaCipher.doFinal(symmetricKey.getEncoded());

        // 4. 결과 저장
        EncryptedMessage result = new EncryptedMessage();
        result.encryptedKey = encryptedKey;
        result.encryptedData = encryptedData;
        result.iv = aesCipher.getIV();

        return result;
    }

    // 하이브리드 복호화
    public static String decrypt(EncryptedMessage message, PrivateKey privateKey) throws Exception {
        // 1. 비대칭키로 대칭키 복호화
        Cipher rsaCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedKey = rsaCipher.doFinal(message.encryptedKey);
        SecretKey symmetricKey = new SecretKeySpec(decryptedKey, SYMMETRIC_ALGORITHM);

        // 2. 복구된 대칭키로 데이터 복호화
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, symmetricKey, new IvParameterSpec(message.iv));
        byte[] decryptedData = aesCipher.doFinal(message.encryptedData);

        return new String(decryptedData);
    }
}
```

이 예제는 HTTPS와 유사한 방식으로 작동합니다:
1. 임시 대칭키(세션키) 생성
2. 수신자의 공개키로 세션키를 암호화
3. 세션키로 실제 데이터를 암호화
4. 암호화된 세션키와 데이터를 함께 전송

## 6. 결론
 대칭키: 빠른 속도, 간단한 구현, 키 공유 문제  
 비대칭키: 높은 보안성, 키 분배 용이, 느린 속도  
 실제 환경에서는 두 방식을 적절히 조합하여 사용  
 용도와 상황에 맞는 암호화 방식 선택이 중요
