# WAS와 웹서버의 차이점

WAS(Web Application Server)와 웹서버(Web Server)는 웹 애플리케이션 아키텍처에서 중요한 역할을 담당하는 구성 요소입니다. 이 문서에서는 두 시스템의 차이점과 각각의 역할에 대해 자세히 알아보겠습니다.

## 1. 기본 개념

### 1.1. 웹서버(Web Server)

웹서버는 **정적 콘텐츠를 제공**하는 것이 주된 역할입니다.

**주요 특징:**
- HTTP 프로토콜을 통해 클라이언트 요청을 처리
- HTML, CSS, JavaScript, 이미지 등 정적 파일 제공
- 빠른 응답 속도와 높은 처리량
- 상대적으로 단순한 구조

**대표적인 웹서버:**
- Apache HTTP Server
- Nginx
- Microsoft IIS
- LiteSpeed

### 1.2. WAS(Web Application Server)

WAS는 **동적 콘텐츠를 생성하고 제공**하는 것이 주된 역할입니다.

**주요 특징:**
- 웹서버의 기능을 포함
- 애플리케이션 로직 실행
- 데이터베이스 연동
- 트랜잭션 관리
- 세션 관리

**대표적인 WAS:**
- Apache Tomcat
- JBoss/WildFly
- WebLogic
- WebSphere
- Jetty

## 2. 상세 비교

### 2.1. 기능적 차이점

| 구분 | 웹서버 | WAS |
|------|--------|-----|
| **주요 역할** | 정적 콘텐츠 제공 | 동적 콘텐츠 생성 및 제공 |
| **처리 방식** | 파일 시스템에서 직접 제공 | 애플리케이션 로직 실행 후 결과 제공 |
| **프로그래밍 언어** | 해당 없음 | Java, .NET, PHP, Python 등 |
| **데이터베이스 연동** | 불가능 | 가능 |
| **트랜잭션 관리** | 불가능 | 가능 |
| **세션 관리** | 제한적 | 완전한 세션 관리 |

### 2.2. 성능적 차이점

```
웹서버 처리 과정:
클라이언트 요청 → 웹서버 → 파일 시스템 → 응답

WAS 처리 과정:
클라이언트 요청 → WAS → 애플리케이션 로직 실행 → 데이터베이스 조회 → 동적 콘텐츠 생성 → 응답
```

**웹서버의 장점:**
- 빠른 응답 속도
- 낮은 리소스 사용량
- 높은 동시 접속 처리 능력

**WAS의 장점:**
- 복잡한 비즈니스 로직 처리
- 데이터베이스와의 효율적인 연동
- 확장 가능한 애플리케이션 구조

## 3. 실제 사용 예시

### 3.1. 웹서버만 사용하는 경우

```nginx
# Nginx 설정 예시
server {
    listen 80;
    server_name example.com;
    
    location / {
        root /var/www/html;
        index index.html;
    }
    
    location ~* \.(css|js|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

**적합한 상황:**
- 정적 웹사이트
- 단순한 랜딩 페이지
- CDN 역할
- 파일 다운로드 서비스

### 3.2. WAS만 사용하는 경우

```java
// Spring Boot 애플리케이션 예시
@RestController
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/api/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/api/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }
}
```

**적합한 상황:**
- 소규모 애플리케이션
- 개발 및 테스트 환경
- 마이크로서비스 아키텍처
- 컨테이너 기반 배포

## 4. 웹서버 + WAS 조합 아키텍처

### 4.1. 일반적인 구성

```
클라이언트 → 웹서버 → WAS → 데이터베이스
```

이 구성에서 각 계층의 역할:

**웹서버 (예: Nginx):**
- 정적 파일 직접 제공
- SSL 종료
- 로드 밸런싱
- 리버스 프록시
- 요청 필터링

**WAS (예: Tomcat):**
- 동적 콘텐츠 생성
- 비즈니스 로직 처리
- 데이터베이스 연동
- 세션 관리

### 4.2. 설정 예시

```nginx
# Nginx에서 WAS로 프록시 설정
upstream tomcat_servers {
    server 127.0.0.1:8080;
    server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name example.com;
    
    # 정적 파일은 Nginx에서 직접 처리
    location ~* \.(css|js|png|jpg|jpeg|gif|ico)$ {
        root /var/www/static;
        expires 1y;
    }
    
    # 동적 요청은 WAS로 전달
    location / {
        proxy_pass http://tomcat_servers;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 4.3. 조합 사용의 장점

1. **성능 최적화**
   - 정적 파일은 웹서버에서 빠르게 처리
   - 동적 요청만 WAS에서 처리하여 리소스 효율성 증대

2. **보안 강화**
   - 웹서버가 외부 요청을 먼저 받아 필터링
   - WAS를 내부 네트워크에 배치하여 직접 노출 방지

3. **확장성**
   - 웹서버에서 여러 WAS 인스턴스로 로드 밸런싱
   - 각 계층을 독립적으로 스케일링 가능

4. **장애 격리**
   - 한 계층의 장애가 다른 계층에 미치는 영향 최소화
   - 정적 콘텐츠는 WAS 장애와 무관하게 서비스 가능

## 5. 선택 기준

### 5.1. 웹서버만 사용해야 하는 경우

- 정적 웹사이트 (블로그, 포트폴리오, 회사 소개 페이지)
- SPA(Single Page Application)의 정적 파일 호스팅
- CDN 엣지 서버
- 파일 다운로드 서비스

### 5.2. WAS만 사용해도 되는 경우

- 소규모 애플리케이션 (사용자 수 < 1000명)
- 개발/테스트 환경
- 마이크로서비스의 개별 서비스
- 정적 파일이 거의 없는 API 서버

### 5.3. 웹서버 + WAS 조합을 사용해야 하는 경우

- 대규모 웹 애플리케이션
- 높은 트래픽이 예상되는 서비스
- 정적 파일과 동적 콘텐츠가 모두 많은 경우
- 보안이 중요한 서비스
- 고가용성이 필요한 서비스

## 6. 실무 고려사항

### 6.1. 성능 모니터링

```bash
# 웹서버 성능 확인
nginx -t  # 설정 파일 검증
curl -w "@curl-format.txt" -o /dev/null -s "http://example.com"

# WAS 성능 확인
jstat -gc [PID]  # JVM 가비지 컬렉션 상태
jstack [PID]     # 스레드 덤프
```

### 6.2. 로그 관리

```nginx
# Nginx 로그 설정
log_format main '$remote_addr - $remote_user [$time_local] "$request" '
                '$status $body_bytes_sent "$http_referer" '
                '"$http_user_agent" "$http_x_forwarded_for"';

access_log /var/log/nginx/access.log main;
error_log /var/log/nginx/error.log warn;
```

### 6.3. 보안 설정

```nginx
# 보안 헤더 설정
add_header X-Frame-Options "SAMEORIGIN" always;
add_header X-XSS-Protection "1; mode=block" always;
add_header X-Content-Type-Options "nosniff" always;
add_header Referrer-Policy "no-referrer-when-downgrade" always;
add_header Content-Security-Policy "default-src 'self'" always;
```

## 7. 결론

WAS와 웹서버는 각각 고유한 역할과 장점을 가지고 있습니다:

- **웹서버**: 정적 콘텐츠 제공에 특화, 빠른 성능
- **WAS**: 동적 콘텐츠 생성, 복잡한 애플리케이션 로직 처리

실제 운영 환경에서는 두 시스템을 조합하여 사용하는 것이 일반적이며, 이를 통해 성능, 보안, 확장성 측면에서 최적의 결과를 얻을 수 있습니다. 프로젝트의 규모, 요구사항, 예산 등을 종합적으로 고려하여 적절한 아키텍처를 선택하는 것이 중요합니다.

## 참고 자료

- [Apache HTTP Server 공식 문서](https://httpd.apache.org/docs/)
- [Nginx 공식 문서](https://nginx.org/en/docs/)
- [Apache Tomcat 공식 문서](https://tomcat.apache.org/tomcat-9.0-doc/)
- [웹 애플리케이션 아키텍처 패턴](https://martinfowler.com/architecture/)