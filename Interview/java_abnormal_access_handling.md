# Java에서 비정상 접근 처리 하는 법

## 1. 개요
비정상 접근(Abnormal Access)은 애플리케이션에 대한 권한이 없거나 악의적인 접근을 의미합니다. 이러한 접근은 보안 취약점을 노출시키고 시스템의 무결성을 위협할 수 있습니다. 이 문서에서는 Java에서 비정상 접근을 탐지하고 처리하는 다양한 방법에 대해 알아보겠습니다.

## 2. 비정상 접근의 유형

### 2.1. 인증 우회 시도
사용자가 적절한 인증 없이 시스템에 접근하려는 시도입니다.

### 2.2. 권한 상승 시도
사용자가 자신에게 부여된 권한 이상의 작업을 수행하려는 시도입니다.

### 2.3. 비정상적인 요청 패턴
일반적인 사용 패턴과 다른 비정상적인 API 호출이나 요청 패턴입니다.

### 2.4. 자원 남용
시스템 자원을 과도하게 사용하여 서비스 거부(DoS) 상태를 유발하려는 시도입니다.

### 2.5. 데이터 탈취 시도
민감한 데이터에 무단으로 접근하려는 시도입니다.

## 3. 비정상 접근 탐지 방법

### 3.1. 로깅과 모니터링
```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityMonitor {
    private static final Logger logger = LoggerFactory.getLogger(SecurityMonitor.class);

    public void logAccessAttempt(String username, String resource, boolean isSuccessful) {
        if (!isSuccessful) {
            logger.warn("비정상 접근 시도: 사용자={}, 리소스={}", username, resource);
            // 추가적인 알림 또는 대응 조치
        } else {
            logger.info("정상 접근: 사용자={}, 리소스={}", username, resource);
        }
    }
}
```

### 3.2. 접근 패턴 분석
```java
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AccessPatternAnalyzer {
    // 사용자별 접근 시도 횟수를 추적
    private final Map<String, AtomicInteger> accessAttempts = new ConcurrentHashMap<>();
    // 사용자별 마지막 접근 시간
    private final Map<String, LocalDateTime> lastAccessTime = new ConcurrentHashMap<>();

    // 접근 시도 임계값
    private static final int THRESHOLD = 5;
    // 시간 간격 (초)
    private static final int TIME_WINDOW_SECONDS = 60;

    public boolean isAbnormalAccess(String username) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime last = lastAccessTime.getOrDefault(username, now.minusHours(1));

        // 마지막 접근 시간 업데이트
        lastAccessTime.put(username, now);

        // 시간 간격 내 접근인 경우 카운트 증가
        if (last.plusSeconds(TIME_WINDOW_SECONDS).isAfter(now)) {
            AtomicInteger count = accessAttempts.computeIfAbsent(username, k -> new AtomicInteger(0));
            if (count.incrementAndGet() > THRESHOLD) {
                return true; // 비정상 접근으로 판단
            }
        } else {
            // 시간 간격을 초과한 경우 카운트 초기화
            accessAttempts.put(username, new AtomicInteger(1));
        }

        return false;
    }
}
```

### 3.3. Spring Security를 활용한 인증 실패 탐지
```java
import org.springframework.context.ApplicationListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFailureListener implements 
        ApplicationListener<AuthenticationFailureBadCredentialsEvent> {

    private final AccessPatternAnalyzer accessPatternAnalyzer;

    public AuthenticationFailureListener(AccessPatternAnalyzer accessPatternAnalyzer) {
        this.accessPatternAnalyzer = accessPatternAnalyzer;
    }

    @Override
    public void onApplicationEvent(AuthenticationFailureBadCredentialsEvent event) {
        String username = event.getAuthentication().getName();

        // 비정상 접근 패턴 분석
        if (accessPatternAnalyzer.isAbnormalAccess(username)) {
            // 계정 잠금, 관리자 알림 등의 조치
            lockAccount(username);
        }
    }

    private void lockAccount(String username) {
        // 계정 잠금 로직 구현
    }
}
```

## 4. 비정상 접근 처리 방법

### 4.1. 예외 처리를 통한 비정상 접근 관리
```java
public class SecurityException extends RuntimeException {
    private final String username;
    private final String resource;
    private final String action;

    public SecurityException(String message, String username, String resource, String action) {
        super(message);
        this.username = username;
        this.resource = resource;
        this.action = action;
    }

    // Getters
}

// 예외 처리 예시
public class AccessController {
    private final Logger logger = LoggerFactory.getLogger(AccessController.class);

    public ResponseEntity<?> accessResource(String username, String resource) {
        try {
            if (!hasAccess(username, resource)) {
                throw new SecurityException("접근 권한이 없습니다", username, resource, "READ");
            }
            // 정상 로직 수행
            return ResponseEntity.ok("리소스 데이터");
        } catch (SecurityException e) {
            // 로깅
            logger.error("보안 위반: {}, 사용자={}, 리소스={}, 작업={}",
                    e.getMessage(), e.getUsername(), e.getResource(), e.getAction());
            // 알림 발송
            notifySecurityTeam(e);
            // 클라이언트에게 적절한 응답
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
    }

    private boolean hasAccess(String username, String resource) {
        // 접근 권한 확인 로직
        return false; // 예시로 항상 거부
    }

    private void notifySecurityTeam(SecurityException e) {
        // 보안팀 알림 로직
    }
}
```

### 4.2. Spring Security를 활용한 접근 제어
```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/public/**").permitAll()
                .antMatchers("/api/admin/**").hasRole("ADMIN")
                .antMatchers("/api/user/**").hasRole("USER")
                .anyRequest().authenticated()
            .and()
            .exceptionHandling()
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    // 접근 거부 처리 로직
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("접근이 거부되었습니다");
                })
                .authenticationEntryPoint((request, response, authException) -> {
                    // 인증되지 않은 사용자 처리 로직
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("인증이 필요합니다");
                });

        return http.build();
    }
}
```

### 4.3. IP 기반 접근 제한
```java
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

public class IpFilter implements Filter {

    private final Set<String> allowedIps;
    private final SecurityMonitor securityMonitor;

    public IpFilter(Set<String> allowedIps, SecurityMonitor securityMonitor) {
        this.allowedIps = allowedIps;
        this.securityMonitor = securityMonitor;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String clientIp = getClientIp(httpRequest);

        if (!allowedIps.contains(clientIp)) {
            securityMonitor.logAccessAttempt("Unknown", httpRequest.getRequestURI(), false);
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpResponse.getWriter().write("IP 주소가 허용 목록에 없습니다");
            return;
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

### 4.4. 속도 제한(Rate Limiting)
```java
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    // 기본 버킷 설정: 1분당 30개 요청 허용
    private Bucket createBucket() {
        Bandwidth limit = Bandwidth.classic(30, Refill.intervally(30, Duration.ofMinutes(1)));
        return Bucket4j.builder().addLimit(limit).build();
    }

    public boolean allowRequest(String clientId) {
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> createBucket());
        return bucket.tryConsume(1);
    }
}

// 사용 예시
@RestController
public class ApiController {

    private final RateLimiter rateLimiter;
    private final SecurityMonitor securityMonitor;

    @Autowired
    public ApiController(RateLimiter rateLimiter, SecurityMonitor securityMonitor) {
        this.rateLimiter = rateLimiter;
        this.securityMonitor = securityMonitor;
    }

    @GetMapping("/api/resource")
    public ResponseEntity<?> getResource(HttpServletRequest request) {
        String clientId = request.getRemoteAddr();

        if (!rateLimiter.allowRequest(clientId)) {
            securityMonitor.logAccessAttempt(clientId, "/api/resource", false);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("요청 한도를 초과했습니다. 잠시 후 다시 시도해주세요.");
        }

        // 정상 로직 수행
        return ResponseEntity.ok("리소스 데이터");
    }
}
```

## 5. 비정상 접근에 대한 대응 전략

### 5.1. 점진적 대응
1. **경고**: 첫 번째 비정상 접근 시도에 대해 로깅 및 모니터링
2. **지연**: 반복된 시도에 대해 응답 지연 시간 증가
3. **일시적 차단**: 지속적인 시도에 대해 일정 시간 동안 접근 차단
4. **영구 차단**: 악의적인 패턴이 확인된 경우 영구적으로 접근 차단

### 5.2. 보안 이벤트 알림
```java
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

public class SecurityAlertService {

    private final JavaMailSender mailSender;

    public SecurityAlertService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendAlert(SecurityException exception) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("security-team@example.com");
        message.setSubject("보안 경고: 비정상 접근 탐지");
        message.setText(String.format(
                "비정상 접근이 탐지되었습니다.\n" +
                "사용자: %s\n" +
                "리소스: %s\n" +
                "작업: %s\n" +
                "시간: %s\n" +
                "메시지: %s",
                exception.getUsername(),
                exception.getResource(),
                exception.getAction(),
                LocalDateTime.now(),
                exception.getMessage()
        ));

        mailSender.send(message);
    }
}
```

### 5.3. 포렌식 데이터 수집
```java
public class ForensicDataCollector {

    private final Logger logger = LoggerFactory.getLogger(ForensicDataCollector.class);

    public void collectData(HttpServletRequest request, String username) {
        Map<String, String> forensicData = new HashMap<>();

        // 요청 정보 수집
        forensicData.put("timestamp", LocalDateTime.now().toString());
        forensicData.put("username", username);
        forensicData.put("ip", getClientIp(request));
        forensicData.put("userAgent", request.getHeader("User-Agent"));
        forensicData.put("requestURI", request.getRequestURI());
        forensicData.put("method", request.getMethod());
        forensicData.put("sessionId", request.getSession().getId());

        // 요청 헤더 수집
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            forensicData.put("header." + headerName, request.getHeader(headerName));
        }

        // 데이터 저장 또는 로깅
        logger.info("포렌식 데이터: {}", forensicData);

        // 데이터베이스에 저장하는 로직 추가 가능
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
```

## 6. 모범 사례

### 6.1. 심층 방어(Defense in Depth)
여러 보안 계층을 구축하여 하나의 계층이 뚫리더라도 다른 계층에서 방어할 수 있도록 합니다.

### 6.2. 최소 권한 원칙
사용자에게 필요한 최소한의 권한만 부여하여 비정상 접근의 영향을 제한합니다.

### 6.3. 보안 로깅
모든 중요한 작업과 접근 시도를 로깅하여 사후 분석이 가능하도록 합니다.

### 6.4. 정기적인 보안 검토
시스템의 보안 설정과 로그를 정기적으로 검토하여 비정상 패턴을 식별합니다.

### 6.5. 보안 업데이트
시스템과 라이브러리를 최신 상태로 유지하여 알려진 취약점을 패치합니다.

## 7. 결론
Java에서 비정상 접근을 효과적으로 처리하기 위해서는 다양한 탐지 및 방어 메커니즘을 조합하여 사용해야 합니다. 로깅, 모니터링, 접근 제어, 예외 처리, 속도 제한 등의 기술을 적절히 활용하면 대부분의 비정상 접근 시도를 방지하고 대응할 수 있습니다. 또한, 보안은 지속적인 과정이므로 정기적인 검토와 업데이트가 필요합니다.

## 참고 자료
- [OWASP Java Security Guidelines](https://owasp.org/www-project-web-security-testing-guide/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/index.html)
- [Java Security Coding Guidelines](https://www.oracle.com/java/technologies/javase/seccodeguide.html)
- [NIST Cybersecurity Framework](https://www.nist.gov/cyberframework)
