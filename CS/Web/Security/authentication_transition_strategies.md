# 쿠키 기반 세션 인증과 JWT 및 다른 세션 관리 방법으로의 전환

## 목차
1. [개요](#개요)
2. [쿠키 기반 세션 인증](#쿠키-기반-세션-인증)
3. [JWT(JSON Web Token) 인증](#jwtjson-web-token-인증)
4. [기타 세션 관리 방법](#기타-세션-관리-방법)
5. [인증 방식 전환 전략](#인증-방식-전환-전략)
6. [결론](#결론)

## 개요

웹 애플리케이션에서 사용자 인증은 핵심적인 보안 요소입니다. 전통적으로 많은 웹 애플리케이션은 쿠키 기반 세션 인증을 사용해왔지만, 최근에는 JWT(JSON Web Token)와 같은 토큰 기반 인증 방식이 인기를 얻고 있습니다. 이 문서에서는 다양한 인증 방식의 특징과 장단점을 살펴보고, 한 방식에서 다른 방식으로 전환하는 전략에 대해 알아보겠습니다.

## 쿠키 기반 세션 인증

### 작동 원리

쿠키 기반 세션 인증은 다음과 같은 단계로 작동합니다:

1. 사용자가 로그인 정보(아이디/비밀번호)를 서버에 제출합니다.
2. 서버는 사용자 정보를 검증하고, 유효한 경우 고유한 세션 ID를 생성합니다.
3. 서버는 이 세션 ID를 서버 측 저장소(메모리, 데이터베이스, 캐시 등)에 저장합니다.
4. 서버는 세션 ID를 쿠키에 담아 클라이언트에게 응답합니다.
5. 이후 클라이언트의 모든 요청에는 자동으로 이 쿠키가 포함됩니다.
6. 서버는 쿠키에서 세션 ID를 추출하고, 서버 측 저장소에서 해당 세션 정보를 조회하여 사용자를 식별합니다.

```
클라이언트                                서버
    |                                     |
    |--- 로그인 요청 (ID/PW) ------------>|
    |                                     |--- 사용자 검증
    |                                     |--- 세션 ID 생성
    |                                     |--- 세션 저장소에 저장
    |<-- 응답 (세션 ID가 담긴 쿠키) ------|
    |                                     |
    |--- 요청 (쿠키 포함) --------------->|
    |                                     |--- 세션 ID 검증
    |                                     |--- 세션 정보 조회
    |<-- 응답 ----------------------------|
```

### 장점

1. **구현 용이성**: 대부분의 웹 프레임워크에서 기본적으로 지원합니다.
2. **보안성**: 세션 ID만 클라이언트에 노출되고, 실제 사용자 정보는 서버에 안전하게 보관됩니다.
3. **세션 무효화**: 서버에서 세션을 쉽게 무효화할 수 있어 로그아웃 처리가 간단합니다.
4. **메모리 효율성**: 클라이언트는 작은 세션 ID만 저장하면 됩니다.

### 단점

1. **확장성 문제**: 여러 서버에 걸쳐 세션을 공유해야 하는 경우 추가적인 인프라(Redis, Memcached 등)가 필요합니다.
2. **CSRF 취약점**: 쿠키는 자동으로 요청에 포함되므로 CSRF(Cross-Site Request Forgery) 공격에 취약할 수 있습니다.
3. **상태 유지**: 서버가 상태를 유지해야 하므로 무상태(Stateless) 아키텍처에 적합하지 않습니다.
4. **모바일/API 호환성**: 모바일 앱이나 API 기반 서비스에서는 쿠키 관리가 번거로울 수 있습니다.

### 구현 예시 (Spring Boot)

```java
@Configuration
public class SessionConfig extends WebSecurityConfigurerAdapter {
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .sessionManagement()
                .maximumSessions(1)
                .expiredUrl("/login?expired");
    }
    
    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
```

## JWT(JSON Web Token) 인증

### 작동 원리

JWT 인증은 다음과 같은 단계로 작동합니다:

1. 사용자가 로그인 정보(아이디/비밀번호)를 서버에 제출합니다.
2. 서버는 사용자 정보를 검증하고, 유효한 경우 JWT를 생성합니다.
3. JWT는 헤더(Header), 페이로드(Payload), 서명(Signature)의 세 부분으로 구성됩니다.
4. 서버는 생성된 JWT를 클라이언트에게 응답합니다.
5. 클라이언트는 JWT를 저장(로컬 스토리지, 세션 스토리지, 쿠키 등)하고, 이후 요청의 Authorization 헤더에 포함시킵니다.
6. 서버는 JWT의 서명을 검증하고, 페이로드에서 사용자 정보를 추출하여 인증을 처리합니다.

```
클라이언트                                서버
    |                                     |
    |--- 로그인 요청 (ID/PW) ------------>|
    |                                     |--- 사용자 검증
    |                                     |--- JWT 생성 (헤더.페이로드.서명)
    |<-- 응답 (JWT) ---------------------|
    |                                     |
    |--- 요청 (Authorization: Bearer JWT) ->|
    |                                     |--- JWT 서명 검증
    |                                     |--- 페이로드에서 정보 추출
    |<-- 응답 ----------------------------|
```

### 장점

1. **무상태(Stateless)**: 서버는 세션 상태를 유지할 필요가 없어 확장성이 좋습니다.
2. **범용성**: 다양한 클라이언트(웹, 모바일, API)에서 일관되게 사용할 수 있습니다.
3. **교차 도메인**: 여러 도메인에서 동일한 토큰을 사용할 수 있습니다.
4. **자체 포함적**: 토큰 자체에 필요한 정보가 포함되어 있어 추가 조회가 필요 없습니다.
5. **마이크로서비스 친화적**: 서비스 간 인증 정보 공유가 용이합니다.

### 단점

1. **토큰 크기**: 포함된 정보에 따라 쿠키보다 크기가 클 수 있습니다.
2. **보안 고려사항**: 토큰에 민감한 정보를 포함하면 안 됩니다(암호화되지 않음).
3. **토큰 무효화**: 발급된 토큰은 만료 전까지 무효화하기 어렵습니다(블랙리스트 관리 필요).
4. **토큰 갱신**: 만료된 토큰을 갱신하는 메커니즘이 필요합니다.

### 구현 예시 (Spring Boot + JWT)

```java
@Configuration
public class JwtConfig extends WebSecurityConfigurerAdapter {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/public/**", "/auth/**").permitAll()
                .anyRequest().authenticated()
            .and()
            .apply(new JwtConfigurer(jwtTokenProvider));
    }
}

@Component
public class JwtTokenProvider {
    
    @Value("${security.jwt.token.secret-key}")
    private String secretKey;
    
    @Value("${security.jwt.token.expire-length}")
    private long validityInMilliseconds;
    
    @PostConstruct
    protected void init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
    }
    
    public String createToken(String username, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put("roles", roles);
        
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
    
    public Authentication getAuthentication(String token) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(getUsername(token));
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }
    
    public String getUsername(String token) {
        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody().getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
            return !claims.getBody().getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidJwtAuthenticationException("Expired or invalid JWT token");
        }
    }
}
```

## 기타 세션 관리 방법

### OAuth 2.0 / OpenID Connect

OAuth 2.0은 인증 및 권한 부여를 위한 프레임워크로, 주로 제3자 서비스에 대한 접근 권한을 부여하는 데 사용됩니다. OpenID Connect는 OAuth 2.0 위에 구축된 인증 레이어입니다.

#### 장점
- 소셜 로그인 통합 용이
- 강력한 보안 표준 준수
- 다양한 인증 흐름 지원

#### 단점
- 구현 복잡성
- 외부 서비스 의존성

### SAML (Security Assertion Markup Language)

SAML은 주로 기업 환경에서 사용되는 XML 기반의 인증 및 권한 부여 표준입니다.

#### 장점
- 엔터프라이즈 환경에 적합
- 단일 로그인(SSO) 지원
- 풍부한 메타데이터 및 속성 전달

#### 단점
- 구현 복잡성
- XML 기반으로 상대적으로 무거움

### API 키

API 키는 간단한 문자열 토큰으로, 주로 서버 간 통신이나 공개 API에서 사용됩니다.

#### 장점
- 구현 단순성
- 낮은 오버헤드

#### 단점
- 제한된 보안 기능
- 키 관리 복잡성

### 다중 요소 인증 (MFA)

다중 요소 인증은 두 가지 이상의 인증 방법을 조합하여 보안을 강화하는 접근 방식입니다.

#### 장점
- 강화된 보안
- 피싱 공격 방지

#### 단점
- 사용자 경험 복잡성
- 구현 및 유지 관리 비용

## 인증 방식 전환 전략

### 쿠키 기반 세션에서 JWT로 전환

#### 점진적 전환 접근법

1. **병행 운영 단계**
   - 기존 세션 인증과 JWT 인증을 동시에 지원하는 미들웨어 구현
   - 새로운 로그인에 JWT 발급, 기존 세션은 유지

   ```java
   @Component
   public class DualAuthenticationFilter extends OncePerRequestFilter {
       
       @Autowired
       private SessionAuthenticationService sessionAuthService;
       
       @Autowired
       private JwtAuthenticationService jwtAuthService;
       
       @Override
       protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) 
               throws ServletException, IOException {
           
           // JWT 인증 시도
           boolean jwtAuthenticated = jwtAuthService.attemptAuthentication(request);
           
           // JWT 인증 실패 시 세션 인증 시도
           if (!jwtAuthenticated) {
               sessionAuthService.attemptAuthentication(request);
           }
           
           chain.doFilter(request, response);
       }
   }
   ```

2. **클라이언트 업데이트**
   - 프론트엔드 코드를 업데이트하여 JWT 처리 로직 추가
   - 로그인 성공 시 JWT를 저장하고 요청 헤더에 포함하는 로직 구현

   ```javascript
   // 로그인 함수
   async function login(username, password) {
       const response = await fetch('/api/auth/login', {
           method: 'POST',
           headers: { 'Content-Type': 'application/json' },
           body: JSON.stringify({ username, password })
       });
       
       const data = await response.json();
       
       if (data.token) {
           // JWT 저장
           localStorage.setItem('token', data.token);
       }
       
       return data;
   }
   
   // API 요청 함수
   async function apiRequest(url, options = {}) {
       const token = localStorage.getItem('token');
       
       if (token) {
           options.headers = {
               ...options.headers,
               'Authorization': `Bearer ${token}`
           };
       }
       
       return fetch(url, options);
   }
   ```

3. **세션 마이그레이션**
   - 로그인 시 기존 세션 정보를 JWT 클레임으로 변환
   - 세션 만료 시 JWT로 자동 전환하는 로직 구현

   ```java
   @Service
   public class AuthMigrationService {
       
       @Autowired
       private SessionRepository sessionRepository;
       
       @Autowired
       private JwtTokenProvider jwtTokenProvider;
       
       public String migrateSessionToJwt(HttpServletRequest request) {
           HttpSession session = request.getSession(false);
           if (session != null) {
               UserDetails userDetails = (UserDetails) session.getAttribute("USER_DETAILS");
               if (userDetails != null) {
                   List<String> roles = userDetails.getAuthorities().stream()
                       .map(GrantedAuthority::getAuthority)
                       .collect(Collectors.toList());
                   
                   return jwtTokenProvider.createToken(userDetails.getUsername(), roles);
               }
           }
           return null;
       }
   }
   ```

4. **완전 전환**
   - 모든 사용자가 JWT를 사용하게 되면 세션 관련 코드 제거
   - 세션 저장소 정리 및 관련 인프라 조정

#### 전환 시 고려사항

1. **보안 고려사항**
   - JWT 비밀키 관리 전략 수립
   - 토큰 만료 정책 설정 (짧은 액세스 토큰 + 리프레시 토큰 패턴 권장)
   - HTTPS 사용 필수

2. **성능 모니터링**
   - 전환 과정에서 성능 지표 모니터링
   - 토큰 크기가 네트워크 트래픽에 미치는 영향 분석

3. **사용자 경험**
   - 전환 과정에서 사용자 로그아웃 최소화
   - 오류 발생 시 명확한 피드백 제공

### JWT에서 다른 인증 방식으로 전환

JWT에서 다른 인증 방식으로 전환하는 경우에도 유사한 점진적 접근법을 적용할 수 있습니다:

1. **새 인증 시스템 구축**
   - 새로운 인증 시스템을 기존 시스템과 병행하여 구축
   - 통합 테스트 환경에서 충분히 검증

2. **인증 어댑터 구현**
   - JWT와 새 인증 방식을 모두 처리할 수 있는 어댑터 레이어 구현
   - 요청에 따라 적절한 인증 방식 적용

3. **점진적 사용자 마이그레이션**
   - 사용자 그룹을 단계적으로 새 인증 시스템으로 마이그레이션
   - A/B 테스트를 통한 영향 분석

4. **모니터링 및 롤백 계획**
   - 문제 발생 시 신속하게 롤백할 수 있는 계획 수립
   - 주요 지표 모니터링을 통한 성공 여부 평가

## 결론

인증 시스템은 웹 애플리케이션의 핵심 보안 요소로, 비즈니스 요구사항과 기술적 제약에 따라 적절한 방식을 선택해야 합니다. 쿠키 기반 세션 인증은 전통적이고 구현이 간단하지만 확장성 문제가 있으며, JWT는 무상태 아키텍처에 적합하지만 토큰 관리에 주의가 필요합니다.

인증 방식을 전환할 때는 점진적 접근법을 통해 사용자 경험을 해치지 않으면서 안전하게 마이그레이션하는 것이 중요합니다. 어떤 인증 방식을 선택하든 보안, 성능, 사용자 경험, 유지보수성 등 다양한 측면을 종합적으로 고려해야 합니다.

최신 웹 애플리케이션에서는 단일 인증 방식보다는 여러 방식을 조합하여 사용하는 경우가 많습니다. 예를 들어, JWT를 기본으로 하되 중요한 작업에는 추가 인증을 요구하거나, OAuth 2.0과 JWT를 함께 사용하는 방식 등이 있습니다. 각 프로젝트의 특성에 맞는 최적의 인증 전략을 수립하는 것이 중요합니다.