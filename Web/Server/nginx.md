# Nginx란?

## 1. 한 줄 정의
Nginx는 경량 웹 서버로 비동기 이벤트 기반 아키텍처를 통해 높은 동시 처리 성능을 제공하며, 리버스 프록시와 로드 밸런서로도 사용되는 다목적 서버 소프트웨어이다.

---

## 2. Nginx의 핵심 특성

### 2-1. 아키텍처(Architecture)
- **이벤트 기반(Event-Driven)**: 비동기 방식으로 요청 처리
- **싱글 마스터 프로세스**: 하나의 마스터 프로세스와 다수의 워커 프로세스
- **Non-Blocking I/O**: 블로킹 없이 여러 연결을 동시 처리

```nginx
# nginx.conf 예시
events {
    worker_connections 1024;  # 워커 프로세스당 최대 연결 수
    use epoll;  # Linux에서 효율적인 이벤트 처리 방식
}
```

### 2-2. Apache와의 비교

| 특성 | Nginx | Apache |
|------|-------|--------|
| **아키텍처** | 이벤트 기반 | 프로세스/스레드 기반 |
| **동시 연결 처리** | 수만 개 이상 | 수백~수천 개 |
| **메모리 사용량** | 낮음 | 높음 |
| **정적 파일 처리** | 매우 빠름 | 보통 |
| **동적 콘텐츠** | 외부 처리 필요 | 내장 모듈 지원 |
| **설정 방식** | 선언적 | 절차적 (.htaccess) |

```bash
# Apache - 프로세스 기반 (요청당 프로세스/스레드 생성)
# 1000개 요청 = 1000개 프로세스/스레드 → 높은 메모리 사용

# Nginx - 이벤트 기반 (소수 워커 프로세스로 모든 요청 처리)
# 1000개 요청 = 4~8개 워커 프로세스 → 낮은 메모리 사용
```

### 2-3. 성능(Performance)
- **C10K 문제 해결**: 10,000개 동시 연결 처리 가능
- **낮은 메모리 사용**: 프로세스/스레드 생성 오버헤드 없음
- **빠른 정적 파일 서빙**: 직접 파일 시스템에서 제공

```nginx
# 정적 파일 서빙 설정
server {
    listen 80;
    server_name example.com;

    location /static/ {
        root /var/www;
        expires 30d;  # 캐시 만료 시간
        access_log off;  # 로그 비활성화로 성능 향상
    }
}
```

### 2-4. 확장성(Scalability)
- **수평 확장**: 로드 밸런싱으로 여러 서버에 부하 분산
- **수직 확장**: 워커 프로세스 수 조정으로 리소스 활용

```nginx
# 로드 밸런싱 설정
upstream backend {
    server backend1.example.com;
    server backend2.example.com;
    server backend3.example.com;
}

server {
    location / {
        proxy_pass http://backend;
    }
}
```

---

## 3. Nginx의 주요 기능

### 3-1. 웹 서버(Web Server)
- **정적 콘텐츠 제공**: HTML, CSS, JS, 이미지 등
- **HTTP/HTTPS 지원**: SSL/TLS 암호화 통신

```nginx
server {
    listen 443 ssl http2;
    server_name example.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        root /var/www/html;
        index index.html;
    }
}
```

### 3-2. 리버스 프록시(Reverse Proxy)
- **백엔드 서버 숨김**: 클라이언트는 Nginx만 인식
- **요청 전달**: 백엔드 애플리케이션 서버로 프록시

```nginx
# Node.js 애플리케이션 프록시
server {
    listen 80;
    server_name api.example.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

### 3-3. 로드 밸런서(Load Balancer)
- **부하 분산 알고리즘**: Round-Robin, Least Connections, IP Hash
- **헬스 체크**: 서버 상태 모니터링

```nginx
upstream app_servers {
    least_conn;  # 연결 수가 가장 적은 서버로 전달

    server app1.example.com weight=3;  # 가중치 설정
    server app2.example.com;
    server app3.example.com backup;  # 백업 서버
}

server {
    location / {
        proxy_pass http://app_servers;
    }
}
```

### 3-4. 캐싱(Caching)
- **프록시 캐시**: 백엔드 응답 저장
- **성능 향상**: 반복 요청 시 빠른 응답

```nginx
proxy_cache_path /var/cache/nginx levels=1:2 keys_zone=my_cache:10m max_size=1g inactive=60m;

server {
    location / {
        proxy_cache my_cache;
        proxy_cache_valid 200 60m;
        proxy_cache_use_stale error timeout http_500 http_502 http_503;

        proxy_pass http://backend;
    }
}
```

---

## 4. 설치 및 기본 사용법

### 4-1. 설치

```bash
# Ubuntu/Debian
sudo apt update
sudo apt install nginx

# CentOS/RHEL
sudo yum install nginx

# macOS
brew install nginx

# 설치 확인
nginx -v
```

### 4-2. 기본 명령어

```bash
# Nginx 시작
sudo systemctl start nginx

# Nginx 중지
sudo systemctl stop nginx

# Nginx 재시작
sudo systemctl restart nginx

# 설정 파일 문법 검사
sudo nginx -t

# 설정 리로드 (무중단)
sudo nginx -s reload

# 상태 확인
sudo systemctl status nginx
```

### 4-3. 설정 파일 구조

```nginx
# /etc/nginx/nginx.conf
http {
    # HTTP 블록: 전역 설정

    include /etc/nginx/mime.types;
    default_type application/octet-stream;

    # 로그 설정
    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    # Gzip 압축
    gzip on;
    gzip_types text/plain text/css application/json;

    # 서버 블록 포함
    include /etc/nginx/conf.d/*.conf;
    include /etc/nginx/sites-enabled/*;
}
```

---

## 5. 실전 사용 사례

### 5-1. SPA(Single Page Application) 배포

```nginx
server {
    listen 80;
    server_name myapp.com;
    root /var/www/myapp/dist;

    location / {
        try_files $uri $uri/ /index.html;  # SPA 라우팅 처리
    }

    location /api/ {
        proxy_pass http://localhost:8080;  # API 서버로 프록시
    }
}
```

### 5-2. 마이크로서비스 아키텍처 (API Gateway)

```nginx
# API Gateway 역할
upstream user_service {
    server user-service:3001;
}

upstream order_service {
    server order-service:3002;
}

upstream product_service {
    server product-service:3003;
}

server {
    listen 80;

    location /api/users/ {
        proxy_pass http://user_service/;
    }

    location /api/orders/ {
        proxy_pass http://order_service/;
    }

    location /api/products/ {
        proxy_pass http://product_service/;
    }
}
```

### 5-3. HTTPS 리다이렉트 및 보안 설정

```nginx
# HTTP → HTTPS 리다이렉트
server {
    listen 80;
    server_name example.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS 서버
server {
    listen 443 ssl http2;
    server_name example.com;

    # SSL 인증서
    ssl_certificate /etc/letsencrypt/live/example.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/example.com/privkey.pem;

    # 보안 헤더
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;

    location / {
        root /var/www/html;
        index index.html;
    }
}
```

### 5-4. Rate Limiting (요청 제한)

```nginx
# IP당 초당 10개 요청으로 제한
limit_req_zone $binary_remote_addr zone=mylimit:10m rate=10r/s;

server {
    location /api/ {
        limit_req zone=mylimit burst=20 nodelay;
        proxy_pass http://backend;
    }
}
```

---

## 6. ALB → Nginx → Application 아키텍처 실무

### 6-1. 아키텍처 구성

```
클라이언트 → ALB → Nginx → 애플리케이션 서버
```

각 계층의 역할:
1. **ALB**: 트래픽 분산, SSL 종료, 헬스 체크
2. **Nginx**: 요청 필터링, 정적 콘텐츠 서빙, 캐싱, 라우팅
3. **애플리케이션 서버**: 비즈니스 로직 처리, 동적 콘텐츠 생성

### 6-2. 클라이언트 IP 보존 문제

**문제점**: 요청이 여러 계층을 거치면서 원본 클라이언트 IP 주소가 손실됨

**해결책**:

```nginx
# 1. X-Forwarded-For 헤더 활용
server {
    location / {
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_pass http://backend;
    }
}

# 2. real_ip 모듈 사용
set_real_ip_from 10.0.0.0/8;  # ALB의 IP 범위
real_ip_header X-Forwarded-For;
real_ip_recursive on;
```

### 6-3. HTTPS → HTTP 리다이렉션 루프 문제

**문제점**: ALB에서 SSL 종료 후 HTTP로 전달하는데, Nginx가 모든 HTTP를 HTTPS로 리다이렉션하면 무한 루프 발생

**해결책**:

```nginx
# X-Forwarded-Proto 헤더 확인
server {
    listen 80;

    # 이미 HTTPS로 왔던 요청은 리다이렉션하지 않음
    if ($http_x_forwarded_proto != 'https') {
        return 301 https://$host$request_uri;
    }

    location / {
        proxy_pass http://backend;
    }
}
```

**애플리케이션 레벨 설정**:

```java
// Spring Boot
server.forward-headers-strategy=native
```

```javascript
// Express.js
app.set('trust proxy', true);
```

```python
# Django
SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTO', 'https')
```

---

## 7. 백엔드 개발자 관점의 중요성

### 7-1. 애플리케이션 서버와의 분리
- **관심사 분리**: Nginx는 정적 파일, 애플리케이션은 비즈니스 로직
- **성능 최적화**: 정적 파일은 Nginx에서 직접 제공

### 7-2. 무중단 배포(Zero-Downtime Deployment)
- **리버스 프록시 활용**: 배포 중 트래픽을 정상 서버로 전달
- **헬스 체크**: 배포된 서버가 준비되면 트래픽 분산

```nginx
upstream blue_green {
    server blue-app:8080 max_fails=3 fail_timeout=30s;
    server green-app:8080 backup;
}
```

### 7-3. 보안 강화
- **DDoS 방어**: Rate Limiting, Connection Limiting
- **SSL/TLS 종료**: 애플리케이션 서버는 암호화 처리 불필요
- **요청 필터링**: 악의적 요청 차단

```nginx
# IP 화이트리스트
location /admin/ {
    allow 192.168.1.0/24;
    deny all;
}

# User-Agent 필터링
if ($http_user_agent ~* (bot|crawler|spider)) {
    return 403;
}
```

### 7-4. 모니터링 및 로깅
- **액세스 로그**: 요청 패턴 분석
- **에러 로그**: 문제 진단
- **상태 모니터링**: stub_status 모듈

```nginx
# 상태 페이지 활성화
location /nginx_status {
    stub_status on;
    access_log off;
    allow 127.0.0.1;
    deny all;
}
```

---

## 8. 핵심 요약

| 특성 | 설명 |
|------|------|
| **아키텍처** | 비동기 이벤트 기반 |
| **성능** | 높은 동시 연결 처리, 낮은 메모리 사용 |
| **주요 기능** | 웹 서버, 리버스 프록시, 로드 밸런서, 캐싱 |
| **장점** | C10K 문제 해결, 빠른 정적 파일 서빙, 확장성 |
| **사용 사례** | SPA 배포, API Gateway, HTTPS 종료, 무중단 배포 |
| **보안** | Rate Limiting, SSL/TLS, 요청 필터링 |

### 8-1. 선택 기준
- **정적 파일이 많으면** → Nginx (성능 우수)
- **동시 연결이 많으면** → Nginx (이벤트 기반)
- **Apache 호환성 필요하면** → Apache (.htaccess, 모듈)
- **단순한 설정 원하면** → Nginx (선언적 설정)

### 8-2. 실무 팁

**프록시 타임아웃 설정**: 백엔드 응답 시간에 맞춰 조정
```nginx
proxy_connect_timeout 60s;
proxy_send_timeout 60s;
proxy_read_timeout 60s;
```

**버퍼 크기 최적화**: 대용량 요청/응답 처리
```nginx
client_max_body_size 100M;
proxy_buffer_size 4k;
proxy_buffers 8 4k;
```

**프록시 계층 관리**:
- 프록시 계층이 많을수록 헤더 관리가 중요
- 원본 클라이언트 정보 보존을 위한 설정 필수
- SSL 종료 지점과 리다이렉션 로직 명확히 구분

**설정 관리**:
- 로그 로테이션으로 디스크 공간 관리
- Git으로 nginx.conf 버전 관리
- Docker로 Nginx 설정 테스트

```bash
docker run -d -p 80:80 -v $(pwd)/nginx.conf:/etc/nginx/nginx.conf nginx
```

### 8-3. 디버깅 팁

```bash
# 설정 파일 문법 검사
sudo nginx -t

# 에러 로그 실시간 확인
sudo tail -f /var/log/nginx/error.log

# 액세스 로그 분석
sudo tail -f /var/log/nginx/access.log | grep "GET /api"

# 설정 덤프 (실제 적용된 설정 확인)
sudo nginx -T
```

---

## 9. 참고 자료

### 9-1. 공식 문서
- [Nginx 공식 문서](https://nginx.org/en/docs/)
- [Nginx Beginner's Guide](https://nginx.org/en/docs/beginners_guide.html)

### 9-2. 추가 학습 주제
- WebSocket 프록시 설정
- HTTP/2 및 HTTP/3 지원
- Nginx Plus (상용 버전) 기능
- Lua 스크립팅 (OpenResty)
- 컨테이너 환경에서의 Nginx (Kubernetes Ingress)
