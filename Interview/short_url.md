# 단축 URL을 어떻게 만들 수 있을까요??

단축 URL은 긴 URL을 짧고 간결한 형태로 변환하는 방식입니다.
URL 단축 서비스를 이용할 수 있지만 직접 구현할 수 있습니다.


1. 긴 URL 입력
2. 서버 URL 저장, 고유한 단축 URL(Key) 생성
3. 단축 URL을 DB에 저장하고, Key로 요청이 들어오면 원래 URL로 Redirection

## 단축 키 생성 방식
- Hash 함수 사용 : 입력된 URL을 Hashing
- Base62 인코딩 : 숫자와 대소문자를 조합하여 짧고 고유한 키 생성
- 자동 증가값 사용 : DB에서 자동 증가하는 숫자를 활용하여 키 생성

## 개요
단축 URL을 만들 때 발생할 수 있는 주요 단점들은 다음과 같다.
1. **단축 키 충돌 가능성** → 해시 기반 방식에서 동일한 해시값이 생성될 수 있음.
2. **단축 키가 너무 길거나 예측 가능함** → 보안 및 가독성 문제.
3. **데이터베이스 부하** → 대량의 요청이 있을 경우 성능 저하.
4. **만료 기능 없음** → 단축 URL이 영구적으로 남아 리소스 낭비.
5. **보안 문제** → 스팸 URL이 단축될 위험.

이를 해결하기 위해 **고유하고 짧으며 충돌이 없는 단축 URL 생성 방식**을 설계합니다.

---

## 단축 URL 생성 방식

### 1. Base62 + UUID 기반 고유 키 생성
- **UUID**를 생성한 뒤 Base62로 변환하여 짧고 충돌이 없는 단축 키 생성.
- UUID는 고유성이 보장되며, Base62를 사용하여 키 길이를 줄인다.

```java
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

public class ShortUrlGenerator {
    private static final String BASE62 = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    public static String generateShortKey() {
        UUID uuid = UUID.randomUUID();
        return base62Encode(uuidToBigInteger(uuid)).substring(0, 8); // 8자리 단축 키
    }

    private static String base62Encode(BigInteger value) {
        StringBuilder sb = new StringBuilder();
        while (value.compareTo(BigInteger.ZERO) > 0) {
            sb.append(BASE62.charAt(value.mod(BigInteger.valueOf(62)).intValue()));
            value = value.divide(BigInteger.valueOf(62));
        }
        return sb.reverse().toString();
    }

    private static BigInteger uuidToBigInteger(UUID uuid) {
        return new BigInteger(uuid.toString().replace("-", ""), 16);
    }
}

```
#### 장점 
- 충돌 가능성이 거의 없음
- 8자리 고유한 단축 키 생성
- UUID 기반으로 예측이 어렵고 보안성이 높음.


### 2. 데이터베이스 + Redis 캐싱
- 데이터베이스(MySQL, PostgreSQL)와 Redis를 조합하여 성능 최적화.
- Redis는 단축 URL 조회 시 캐싱하여 빠른 응답 제공.
- 만료 기능을 Redis에서 TTL(Time To Live) 설정을 통해 자동 관리.


``` sql
CREATE TABLE url_mapping (
    short_key VARCHAR(10) PRIMARY KEY,
    original_url TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP NULL
);

```


```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/shorten")
public class UrlShortenerService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private UrlRepository urlRepository;

    @PostMapping
    public ResponseEntity<String> shortenUrl(@RequestBody String longUrl) {
        String shortKey = ShortUrlGenerator.generateShortKey();
        UrlEntity urlEntity = new UrlEntity(shortKey, longUrl);
        urlRepository.save(urlEntity);

        // Redis 캐싱 (24시간 만료)
        redisTemplate.opsForValue().set(shortKey, longUrl, 24, TimeUnit.HOURS);
        return ResponseEntity.ok("http://short.ly/" + shortKey);
    }

    @GetMapping("/{shortKey}")
    public ResponseEntity<Void> redirect(@PathVariable String shortKey) {
        String longUrl = redisTemplate.opsForValue().get(shortKey);

        if (longUrl == null) {
            longUrl = urlRepository.findByShortKey(shortKey)
                    .map(UrlEntity::getOriginalUrl)
                    .orElse(null);
            if (longUrl == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", longUrl)
                .build();
    }
}


```

#### 장점
- 조회 속도 최적화 : Redis 캐싱을 사용하여 빠른 응답
- 만료 기능 : Redis TTL을 이용해 오래된 단축 URL 자동 삭제.


추가로 보안 강화, 사용자 통계등을 위해 아래와 같은 방식을 추가할 수 있습니다.

| 기능 | 적용 방법 | 해결되는 문제 |
|------|---------|-------------|
| **고유 단축 키 생성** | UUID + Base62 인코딩 | 충돌 가능성 제거, 예측 불가능 |
| **빠른 조회** | Redis 캐싱 + MySQL | 성능 최적화, DB 부하 감소 |
| **만료 기능** | Redis TTL, DB 만료 필드 | 리소스 절약 |
| **보안 강화** | 스팸 URL 필터링, 사용자 제한 | 악용 방지 |
| **사용자 통계** | 클릭 수 저장 | 마케팅 분석 가능 |


