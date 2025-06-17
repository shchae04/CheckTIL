# HMAC을 이용한 데이터 검증

## 1. 개요
HMAC(Hash-based Message Authentication Code)은 메시지의 무결성과 신뢰성을 검증하기 위한 암호화 기법입니다. 비밀 키와 해시 함수를 조합하여 메시지가 전송 중에 변조되지 않았음을 확인할 수 있습니다.

## 2. HMAC의 기본 원리

### 2.1. 정의
- 해시 함수와 비밀 키를 결합한 메시지 인증 코드
- 메시지의 무결성과 출처를 동시에 검증

### 2.2. 작동 방식
1. 송신자와 수신자가 동일한 비밀 키를 공유
2. 송신자는 메시지와 비밀 키를 사용하여 HMAC 값을 생성
3. 메시지와 HMAC 값을 함께 전송
4. 수신자는 받은 메시지와 공유된 비밀 키로 HMAC 값을 계산
5. 계산된 HMAC 값과 수신된 HMAC 값을 비교하여 메시지 무결성 검증

### 2.3. 수학적 표현
HMAC(K, m) = H((K' ⊕ opad) || H((K' ⊕ ipad) || m))

여기서:
- K: 비밀 키
- K': 해시 함수의 블록 크기에 맞게 조정된 키
- m: 메시지
- H: 해시 함수 (예: SHA-256)
- opad: 외부 패딩 (0x5c로 채워진 블록)
- ipad: 내부 패딩 (0x36으로 채워진 블록)
- ⊕: XOR 연산
- ||: 연결 연산

## 3. HMAC 알고리즘 종류

### 3.1. HMAC-MD5
- MD5 해시 함수 기반
- 현재는 보안 취약점으로 인해 권장되지 않음

### 3.2. HMAC-SHA1
- SHA-1 해시 함수 기반
- 128비트 해시 값 생성
- 현재는 SHA-1의 취약점으로 인해 권장되지 않음

### 3.3. HMAC-SHA256
- SHA-256 해시 함수 기반
- 256비트 해시 값 생성
- 현재 가장 널리 사용되는 HMAC 알고리즘 중 하나

### 3.4. HMAC-SHA512
- SHA-512 해시 함수 기반
- 512비트 해시 값 생성
- 더 높은 보안 수준이 필요한 경우 사용

## 4. HMAC 구현 예제

### 4.1. Java에서의 HMAC 구현
```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HMACExample {
    
    public static String generateHmac(String algorithm, String data, String key) throws Exception {
        Mac mac = Mac.getInstance(algorithm);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), algorithm);
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
    
    public static boolean verifyHmac(String algorithm, String data, String key, String expectedHmac) throws Exception {
        String calculatedHmac = generateHmac(algorithm, data, key);
        return calculatedHmac.equals(expectedHmac);
    }
    
    public static void main(String[] args) {
        try {
            String algorithm = "HmacSHA256";
            String key = "secretKey123";
            String data = "Hello, HMAC!";
            
            // HMAC 생성
            String hmac = generateHmac(algorithm, data, key);
            System.out.println("생성된 HMAC: " + hmac);
            
            // HMAC 검증
            boolean isValid = verifyHmac(algorithm, data, key, hmac);
            System.out.println("HMAC 검증 결과: " + isValid);
            
            // 변조된 데이터로 검증
            boolean isValidForTamperedData = verifyHmac(algorithm, data + "tampered", key, hmac);
            System.out.println("변조된 데이터 HMAC 검증 결과: " + isValidForTamperedData);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

### 4.2. Spring Security에서의 HMAC 사용
```java
import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.codec.Utf8;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class SpringSecurityHmacExample {
    
    private static final String HMAC_SHA256 = "HmacSHA256";
    
    public static String createHmac(String data, String key) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKey = new SecretKeySpec(Utf8.encode(key), HMAC_SHA256);
            mac.init(secretKey);
            return new String(Hex.encode(mac.doFinal(Utf8.encode(data))));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new IllegalStateException("HMAC 생성 중 오류 발생", e);
        }
    }
    
    public static boolean validateHmac(String data, String key, String expectedHmac) {
        String calculatedHmac = createHmac(data, key);
        return calculatedHmac.equals(expectedHmac);
    }
}
```

## 5. HMAC을 이용한 데이터 검증 사례

### 5.1. API 요청 인증
```java
public class ApiAuthenticationExample {
    
    private static final String API_KEY = "your-api-key";
    private static final String API_SECRET = "your-api-secret";
    
    public static String createAuthHeader(String httpMethod, String endpoint, String timestamp, String requestBody) throws Exception {
        // 인증 문자열 생성
        String stringToSign = httpMethod + "\n" + endpoint + "\n" + timestamp + "\n" + requestBody;
        
        // HMAC 생성
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(API_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String signature = Base64.getEncoder().encodeToString(hmacBytes);
        
        // 인증 헤더 생성
        return "APIAuth " + API_KEY + ":" + signature;
    }
    
    // 서버 측 검증 예제
    public static boolean validateRequest(String httpMethod, String endpoint, String timestamp, 
                                         String requestBody, String authHeader) throws Exception {
        // 헤더에서 API 키와 서명 추출
        String[] authParts = authHeader.replace("APIAuth ", "").split(":");
        String apiKey = authParts[0];
        String receivedSignature = authParts[1];
        
        // API 키 검증
        if (!API_KEY.equals(apiKey)) {
            return false;
        }
        
        // 타임스탬프 검증 (5분 이내)
        long requestTime = Long.parseLong(timestamp);
        long currentTime = System.currentTimeMillis() / 1000;
        if (currentTime - requestTime > 300) {  // 5분 = 300초
            return false;
        }
        
        // 서명 검증
        String stringToSign = httpMethod + "\n" + endpoint + "\n" + timestamp + "\n" + requestBody;
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(API_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8));
        String calculatedSignature = Base64.getEncoder().encodeToString(hmacBytes);
        
        return calculatedSignature.equals(receivedSignature);
    }
}
```

### 5.2. 웹훅 검증
```java
public class WebhookValidationExample {
    
    private static final String WEBHOOK_SECRET = "your-webhook-secret";
    
    public static boolean validateWebhook(String payload, String receivedSignature) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
        String calculatedSignature = "sha256=" + bytesToHex(hmacBytes);
        
        return calculatedSignature.equals(receivedSignature);
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
```

### 5.3. JWT 서명
```java
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

public class JwtHmacExample {
    
    private static final Key SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    public static String createJwt(String subject, String issuer, long ttlMillis) {
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        Date expiration = new Date(nowMillis + ttlMillis);
        
        return Jwts.builder()
                .setSubject(subject)
                .setIssuer(issuer)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(SECRET_KEY)
                .compact();
    }
    
    public static boolean validateJwt(String jwt) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(jwt);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### 5.4. 파일 무결성 검증
```java
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileIntegrityExample {
    
    private static final String SECRET_KEY = "file-integrity-secret-key";
    
    public static String calculateFileHmac(String filePath) throws Exception {
        byte[] fileData = Files.readAllBytes(Paths.get(filePath));
        
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] hmacBytes = mac.doFinal(fileData);
        
        return Base64.getEncoder().encodeToString(hmacBytes);
    }
    
    public static boolean verifyFileIntegrity(String filePath, String storedHmac) throws Exception {
        String calculatedHmac = calculateFileHmac(filePath);
        return calculatedHmac.equals(storedHmac);
    }
}
```

## 6. HMAC 사용 시 모범 사례

### 6.1. 키 관리
- 충분히 긴 키 사용 (최소 256비트 이상)
- 키를 안전하게 저장 (환경 변수, 키 관리 서비스 등)
- 정기적인 키 교체
- 키를 소스 코드에 하드코딩하지 않기

### 6.2. 알고리즘 선택
- 최신 해시 알고리즘 사용 (SHA-256 이상)
- MD5, SHA-1 기반 HMAC은 피하기
- 보안 요구사항에 맞는 알고리즘 선택

### 6.3. 타임스탬프 활용
- 요청에 타임스탬프 포함
- 일정 시간이 지난 요청은 거부 (재생 공격 방지)
- 서버와 클라이언트 간 시간 동기화 고려

### 6.4. 상수 시간 비교
- HMAC 비교 시 타이밍 공격 방지를 위해 상수 시간 비교 사용
```java
public static boolean constantTimeEquals(String a, String b) {
    byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
    byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
    
    if (aBytes.length != bBytes.length) {
        return false;
    }
    
    int result = 0;
    for (int i = 0; i < aBytes.length; i++) {
        result |= aBytes[i] ^ bBytes[i];
    }
    
    return result == 0;
}
```

## 7. HMAC vs 다른 인증 방식 비교

### 7.1. HMAC vs 디지털 서명
- HMAC: 대칭키 기반, 빠른 속도, 키 공유 필요
- 디지털 서명: 비대칭키 기반, 느린 속도, 공개키만 공유

### 7.2. HMAC vs 단순 해시
- HMAC: 키를 사용하여 메시지 출처 인증 가능
- 단순 해시: 무결성만 검증, 출처 인증 불가능

### 7.3. HMAC vs OAuth
- HMAC: 단순한 메시지 인증
- OAuth: 사용자 인증 및 권한 부여 프레임워크

## 8. 결론
HMAC은 메시지의 무결성과 출처를 검증하는 효과적인 방법입니다. 적절한 키 관리와 알고리즘 선택을 통해 API 인증, 웹훅 검증, 파일 무결성 확인 등 다양한 상황에서 안전하게 데이터를 검증할 수 있습니다. 특히 대칭키 기반으로 동작하기 때문에 빠른 속도가 요구되는 환경에서 유용하게 활용될 수 있습니다.