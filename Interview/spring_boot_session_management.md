# Spring Boot에서의 세션 관리: State vs Stateless

Spring Boot 애플리케이션에서 사용자 세션을 관리하는 방법은 크게 상태 유지(Stateful)와 상태 비유지(Stateless) 두 가지 접근 방식으로 나눌 수 있습니다. 이 문서에서는 두 접근 방식의 차이점, 쿠키 기반 세션 관리, Redis를 활용한 세션 관리 등에 대해 알아보겠습니다.

## 목차
1. [상태 유지(Stateful) vs 상태 비유지(Stateless)](#상태-유지stateful-vs-상태-비유지stateless)
2. [쿠키 기반 세션 관리](#쿠키-기반-세션-관리)
3. [Redis를 활용한 세션 관리](#redis를-활용한-세션-관리)
4. [Spring Boot에서 세션 구현 예제](#spring-boot에서-세션-구현-예제)
5. [세션 관리 시 고려사항](#세션-관리-시-고려사항)

## 상태 유지(Stateful) vs 상태 비유지(Stateless)

### 상태 유지(Stateful) 방식

상태 유지 방식은 서버가 클라이언트의 상태 정보를 저장하고 관리하는 방식입니다.

**특징:**
- 서버가 사용자의 상태 정보(세션)를 메모리나 데이터베이스에 저장
- 클라이언트는 세션 ID만 가지고 있으며, 이를 통해 서버에 저장된 세션 정보에 접근
- 전통적인 웹 애플리케이션에서 많이 사용되는 방식

**장점:**
- 서버에서 사용자 상태를 쉽게 관리할 수 있음
- 클라이언트는 최소한의 정보만 저장하므로 보안에 유리
- 구현이 상대적으로 간단함

**단점:**
- 서버 확장성(Scalability)에 제약이 있음 (세션 정보를 공유해야 함)
- 서버 메모리 사용량 증가
- 서버 장애 시 세션 정보가 손실될 수 있음

### 상태 비유지(Stateless) 방식

상태 비유지 방식은 서버가 클라이언트의 상태 정보를 저장하지 않고, 클라이언트가 필요한 모든 정보를 가지고 있는 방식입니다.

**특징:**
- 서버는 클라이언트의 상태 정보를 저장하지 않음
- 클라이언트가 요청할 때마다 인증 정보와 필요한 모든 데이터를 함께 전송
- 주로 토큰 기반 인증(JWT 등)을 사용

**장점:**
- 서버 확장성이 뛰어남 (어떤 서버로 요청이 가도 상관없음)
- 서버 부하 감소 (세션 저장소 불필요)
- 마이크로서비스 아키텍처에 적합

**단점:**
- 클라이언트 요청에 더 많은 데이터가 포함되어 네트워크 부하 증가
- 토큰 관리의 복잡성 (만료, 갱신 등)
- 로그아웃 처리가 복잡할 수 있음 (토큰 무효화)

## 쿠키 기반 세션 관리

쿠키 기반 세션 관리는 Spring Boot에서 기본적으로 제공하는 세션 관리 방식입니다.

### 동작 원리

1. 사용자가 로그인하면 서버는 세션을 생성하고 고유한 세션 ID를 발급
2. 세션 ID는 쿠키에 저장되어 클라이언트에게 전송
3. 클라이언트는 이후 요청 시 쿠키에 저장된 세션 ID를 함께 전송
4. 서버는 세션 ID를 통해 사용자의 세션 정보를 조회하여 인증 상태 확인

### Spring Boot에서의 구현

Spring Boot에서는 `spring-boot-starter-web`을 사용하면 기본적인 세션 관리 기능이 포함됩니다.

```properties
# application.properties 또는 application.yml 설정
server.servlet.session.timeout=30m  # 세션 타임아웃 설정 (기본값: 30분)
server.servlet.session.cookie.http-only=true  # HttpOnly 쿠키 설정
server.servlet.session.cookie.secure=true  # HTTPS에서만 쿠키 전송
```

세션에 데이터 저장 및 조회:

```java
@Controller
public class SessionController {

    @GetMapping("/set-session")
    public String setSession(HttpSession session) {
        session.setAttribute("username", "user1");
        return "sessionSet";
    }

    @GetMapping("/get-session")
    public String getSession(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username);
        return "sessionGet";
    }
}
```

## Redis를 활용한 세션 관리

여러 서버 인스턴스가 있는 환경에서는 세션 정보를 공유해야 합니다. Redis는 인메모리 데이터 저장소로, 세션 저장소로 많이 사용됩니다.

### Redis 세션 저장소의 장점

- 고성능: 인메모리 데이터 저장소로 빠른 읽기/쓰기 속도
- 확장성: 여러 서버 간 세션 공유 가능
- 영속성: 디스크에 데이터 저장 가능 (장애 복구)
- 자동 만료: TTL(Time-To-Live) 기능으로 세션 자동 만료 처리

### Spring Boot에서 Redis 세션 저장소 구현

1. 의존성 추가:

```gradle
// Gradle
implementation 'org.springframework.boot:spring-boot-starter-data-redis'
implementation 'org.springframework.session:spring-session-data-redis'
```

```xml
<!-- Maven -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
```

2. 설정:

```java
@Configuration
@EnableRedisHttpSession
public class SessionConfig {

    @Bean
    public LettuceConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory();
    }
}
```

3. application.properties 설정:

```properties
spring.redis.host=localhost
spring.redis.port=6379
spring.session.store-type=redis
spring.session.redis.flush-mode=on-save
spring.session.redis.namespace=spring:session
```

이제 세션은 자동으로 Redis에 저장되며, 여러 서버 인스턴스 간에 세션이 공유됩니다.

## Spring Boot에서 세션 구현 예제

### 1. 기본 세션 관리 예제

```java
@RestController
@RequestMapping("/api/session")
public class SessionController {

    @GetMapping("/login")
    public ResponseEntity<String> login(HttpSession session, @RequestParam String username) {
        // 사용자 인증 로직 (생략)

        // 세션에 사용자 정보 저장
        session.setAttribute("user", username);
        session.setMaxInactiveInterval(1800); // 30분

        return ResponseEntity.ok("로그인 성공: " + username);
    }

    @GetMapping("/info")
    public ResponseEntity<String> getSessionInfo(HttpSession session) {
        String username = (String) session.getAttribute("user");

        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그인이 필요합니다.");
        }

        return ResponseEntity.ok("현재 로그인 사용자: " + username);
    }

    @GetMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("로그아웃 성공");
    }
}
```

### 2. Redis 세션 관리 예제

Redis를 사용한 세션 관리는 설정만 다르고 코드는 동일합니다. 위의 Redis 설정을 적용하면 세션이 자동으로 Redis에 저장됩니다.

### 3. JWT를 사용한 Stateless 인증 예제

```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest loginRequest) {
        // 사용자 인증 로직
        User user = userService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());

        // JWT 토큰 생성
        String token = tokenProvider.createToken(user.getUsername(), user.getRoles());

        return ResponseEntity.ok(new TokenResponse(token));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@RequestHeader("Authorization") String header) {
        // 토큰에서 사용자 정보 추출
        String token = header.substring(7); // "Bearer " 제거
        String username = tokenProvider.getUsernameFromToken(token);

        User user = userService.findByUsername(username);

        return ResponseEntity.ok(new UserResponse(user));
    }
}
```

JWT 토큰 제공자 예제:

```java
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);

        Date now = new Date();
        Date validity = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(SignatureAlgorithm.HS256, jwtSecret)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
```

## 세션 관리 시 고려사항

### 보안 고려사항

1. **세션 하이재킹 방지**
   - HttpOnly 쿠키 사용
   - Secure 플래그 설정 (HTTPS만 허용)
   - 세션 ID 정기적 갱신

2. **CSRF(Cross-Site Request Forgery) 방지**
   - Spring Security의 CSRF 보호 기능 활성화
   - CSRF 토큰 사용

3. **세션 고정 공격 방지**
   - 인증 후 세션 ID 변경: `session.invalidate();` 후 `session = request.getSession(true);` 호출

### 성능 고려사항

1. **세션 크기 최소화**
   - 필요한 데이터만 세션에 저장
   - 큰 객체는 세션에 저장하지 않음

2. **세션 타임아웃 설정**
   - 적절한 세션 만료 시간 설정
   - 비활성 세션 자동 정리

3. **세션 저장소 선택**
   - 단일 서버: 인메모리 세션
   - 다중 서버: Redis, Hazelcast 등 분산 세션 저장소

### 확장성 고려사항

1. **세션 클러스터링**
   - 로드 밸런서 뒤에 여러 서버가 있는 경우 세션 공유 필요
   - Sticky Session vs 분산 세션 저장소

2. **세션 직렬화**
   - 세션 객체는 직렬화 가능해야 함 (Serializable 구현)
   - 직렬화/역직렬화 성능 고려

3. **세션 vs JWT**
   - 마이크로서비스 아키텍처에서는 JWT가 더 적합할 수 있음
   - 하이브리드 접근 방식 고려 (중요 정보는 서버 세션, 인증 정보는 JWT)

---

Spring Boot에서 세션 관리는 애플리케이션의 요구사항, 아키텍처, 보안 요구사항에 따라 다양한 방식으로 구현할 수 있습니다. 상태 유지(Stateful) 방식과 상태 비유지(Stateless) 방식 각각의 장단점을 이해하고, 적절한 세션 저장소(인메모리, Redis 등)를 선택하는 것이 중요합니다.
