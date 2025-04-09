# Spring Security의 구조

Spring Security는 Java/Spring 애플리케이션의 인증(Authentication)과 인가(Authorization)를 담당하는 강력한 보안 프레임워크입니다. 이 문서에서는 Spring Security의 아키텍처와 주요 구성 요소에 대해 알아보겠습니다.

## 1. Spring Security 개요

Spring Security는 다음과 같은 주요 기능을 제공합니다:

- **인증(Authentication)**: 사용자가 누구인지 확인
- **인가(Authorization)**: 인증된 사용자가 어떤 리소스에 접근할 수 있는지 결정
- **보호(Protection)**: CSRF, XSS 등 다양한 웹 보안 공격으로부터 애플리케이션 보호

## 2. 핵심 아키텍처

Spring Security의 아키텍처는 크게 다음과 같은 구성 요소로 이루어져 있습니다:

### 1) 인증(Authentication) 아키텍처

```
AuthenticationFilter → AuthenticationManager → AuthenticationProvider → UserDetailsService/UserDetails
```

#### 인증 처리 흐름

1. **SecurityFilterChain**: 모든 요청은 일련의 필터를 통과합니다.
2. **AuthenticationFilter**: 사용자 인증 정보를 추출하여 `Authentication` 객체를 생성합니다.
3. **AuthenticationManager**: 인증 처리를 관리하는 인터페이스로, 실제 인증은 `AuthenticationProvider`에 위임합니다.
4. **AuthenticationProvider**: 실제 인증 로직을 수행합니다. 여러 Provider를 등록할 수 있습니다.
5. **UserDetailsService**: 사용자 정보를 로드하는 인터페이스입니다.
6. **UserDetails**: 인증에 필요한 사용자 정보를 담는 인터페이스입니다.
7. **SecurityContext**: 인증된 사용자 정보를 저장합니다.

### 2) 인가(Authorization) 아키텍처

```
FilterSecurityInterceptor → AccessDecisionManager → AccessDecisionVoter
```

#### 인가 처리 흐름

1. **FilterSecurityInterceptor**: 요청에 대한 권한 검사를 수행합니다.
2. **AccessDecisionManager**: 접근 결정을 관리하는 인터페이스입니다.
3. **AccessDecisionVoter**: 접근 허용 여부를 투표하는 인터페이스입니다.
4. **SecurityMetadataSource**: 보안 객체에 대한 메타데이터를 제공합니다.

## 3. 주요 필터와 처리 순서

Spring Security는 필터 체인을 통해 요청을 처리합니다. 주요 필터는 다음과 같습니다:

1. **SecurityContextPersistenceFilter**: SecurityContext를 로드하고 저장합니다.
2. **CsrfFilter**: CSRF 공격을 방어합니다.
3. **LogoutFilter**: 로그아웃 요청을 처리합니다.
4. **UsernamePasswordAuthenticationFilter**: 폼 기반 인증을 처리합니다.
5. **BasicAuthenticationFilter**: HTTP Basic 인증을 처리합니다.
6. **RequestCacheAwareFilter**: 인증 후 원래 요청으로 리다이렉트합니다.
7. **SecurityContextHolderAwareRequestFilter**: 서블릿 API 보안 메서드를 구현합니다.
8. **AnonymousAuthenticationFilter**: 인증되지 않은 사용자를 익명 사용자로 처리합니다.
9. **SessionManagementFilter**: 세션 고정 보호, 동시 세션 제어 등을 담당합니다.
10. **ExceptionTranslationFilter**: Spring Security 예외를 HTTP 응답으로 변환합니다.
11. **FilterSecurityInterceptor**: 접근 제어 결정을 내립니다.

## 4. 설정 방법

### 1) Java 설정

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/", "/home").permitAll()
                .antMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
                .and()
            .formLogin()
                .loginPage("/login")
                .permitAll()
                .and()
            .logout()
                .permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 2) Spring Boot 자동 설정

Spring Boot는 `spring-boot-starter-security` 의존성을 추가하면 기본 보안 설정을 자동으로 적용합니다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-security'
```

## 5. 인증 방식

Spring Security는 다양한 인증 방식을 지원합니다:

### 1) 폼 기반 인증

가장 일반적인 인증 방식으로, 사용자가 로그인 폼을 통해 인증합니다.

```java
http.formLogin()
    .loginPage("/login")
    .defaultSuccessUrl("/home")
    .failureUrl("/login?error=true");
```

### 2) HTTP Basic 인증

HTTP 헤더를 통해 사용자 이름과 비밀번호를 전송하는 방식입니다.

```java
http.httpBasic();
```

### 3) OAuth 2.0 / JWT 인증

외부 서비스 인증이나 토큰 기반 인증에 사용됩니다.

```java
http.oauth2Login()
    .authorizationEndpoint()
    .baseUri("/oauth2/authorize");
```

## 6. 고급 기능

### 1) Method Security

메서드 레벨에서 보안을 적용할 수 있습니다.

```java
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration {
    // ...
}

@Service
public class UserService {
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        // ...
    }
}
```

### 2) CORS 설정

Cross-Origin Resource Sharing 설정을 통해 다른 도메인의 리소스 접근을 제어합니다.

```java
http.cors().configurationSource(corsConfigurationSource());

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOrigins(Arrays.asList("https://example.com"));
    configuration.setAllowedMethods(Arrays.asList("GET", "POST"));
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 3) CSRF 보호

Cross-Site Request Forgery 공격을 방지합니다.

```java
http.csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse());
```

## 7. Spring Security 5.x의 새로운 기능

Spring Security 5.x에서는 다음과 같은 새로운 기능이 추가되었습니다:

1. **OAuth 2.0 클라이언트**: 소셜 로그인 등 OAuth 2.0 기반 인증을 쉽게 구현할 수 있습니다.
2. **WebFlux 지원**: 리액티브 프로그래밍 모델을 지원합니다.
3. **암호화 개선**: 최신 암호화 알고리즘과 패스워드 인코딩 기능이 강화되었습니다.
4. **CSRF 보호 개선**: SameSite 쿠키 속성 지원 등 CSRF 보호 기능이 강화되었습니다.

## 8. 모범 사례

1. **항상 HTTPS 사용**: 모든 인증 통신은 HTTPS를 통해 이루어져야 합니다.
2. **강력한 비밀번호 정책 적용**: 비밀번호 복잡성, 만료 정책 등을 설정합니다.
3. **최소 권한 원칙 적용**: 사용자에게 필요한 최소한의 권한만 부여합니다.
4. **세션 관리 강화**: 세션 고정 보호, 동시 세션 제어 등을 설정합니다.
5. **보안 헤더 설정**: X-XSS-Protection, X-Content-Type-Options 등의 보안 헤더를 설정합니다.

## 참고 자료

- [Spring Security 공식 문서](https://docs.spring.io/spring-security/site/docs/current/reference/html5/)
- [Spring Security Architecture](https://spring.io/guides/topicals/spring-security-architecture)
- [OWASP Top 10](https://owasp.org/www-project-top-ten/)
