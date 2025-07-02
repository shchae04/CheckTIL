# NGINX

NGINX(엔진엑스)는 고성능 웹 서버, 리버스 프록시, 로드 밸런서로 사용되는 오픈 소스 소프트웨어입니다. 웹 서버와 프록시 서버로 널리 사용되고 있습니다.

## NGINX의 핵심 특징

NGINX가 Apache와 같은 다른 웹 서버와 차별화되는 가장 큰 특징은 **이벤트 기반 아키텍처**입니다. 이 접근 방식은 다음과 같은 이점을 제공합니다:

- 비동기 처리로 적은 리소스로 많은 연결 처리 가능
- 단일 워커 프로세스가 수천 개의 연결을 동시에 처리
- 각 연결마다 별도 스레드를 생성하지 않아 메모리 효율성이 높음

## 주요 활용 사례

NGINX는 다양한 상황에서 활용할 수 있습니다:

1. **웹 서버**: 정적 파일을 매우 효율적으로 제공
2. **리버스 프록시**: 백엔드 서버 보호 및 SSL 종료 처리
3. **로드 밸런서**: 여러 서버 간 트래픽 분산
4. **API 게이트웨이**: 마이크로서비스 아키텍처에서 중앙 진입점 역할

## NGINX vs Apache 비교

NGINX와 Apache는 가장 널리 사용되는 웹 서버이지만, 아키텍처와 성능 특성에 차이가 있습니다:

| 특성 | NGINX | Apache |
|------|-------|--------|
| 아키텍처 | 이벤트 기반, 비동기 | 프로세스/스레드 기반 |
| 동시성 처리 | 매우 효율적 | 상대적으로 리소스 집약적 |
| 정적 콘텐츠 | 매우 빠름 | 빠름 |
| 메모리 사용량 | 낮음 | 상대적으로 높음 |

## ALB -> NGINX -> Application 아키텍처

AWS Application Load Balancer(ALB), NGINX, 애플리케이션 서버로 구성된 아키텍처 구현 시 고려해야 할 중요한 개념과 문제점들이 있습니다.

### 아키텍처 흐름

```
클라이언트 → ALB → NGINX → 애플리케이션 서버
```

이 구조에서 각 계층은 다음 역할을 담당합니다:

1. **ALB**: 트래픽 분산, SSL 종료, 헬스 체크
2. **NGINX**: 요청 필터링, 정적 콘텐츠 서빙, 캐싱, 라우팅
3. **애플리케이션 서버**: 비즈니스 로직 처리, 동적 콘텐츠 생성

이 아키텍처는 확장성, 보안, 성능 최적화, 고가용성 측면에서 많은 장점을 제공합니다.

## 클라이언트 IP 보존 문제

이 구조에서 발생하는 주요 문제 중 하나는 **원본 클라이언트 IP 주소가 손실**되는 것입니다. 요청이 ALB → NGINX → 애플리케이션 서버로 전달되는 과정에서 각 단계마다 소스 IP가 변경되어, 결국 애플리케이션은 실제 클라이언트 IP를 알 수 없게 됩니다.

### 해결책

이 문제는 다음 방법으로 해결할 수 있습니다:

1. **X-Forwarded-For 헤더 활용**
   ```nginx
   proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
   proxy_set_header X-Real-IP $remote_addr;
   ```

2. **NGINX의 real_ip 모듈 사용**
   ```nginx
   set_real_ip_from 10.0.0.0/8;  # ALB의 IP 범위
   real_ip_header X-Forwarded-For;
   real_ip_recursive on;
   ```

## HTTPS → HTTP 리다이렉션 문제

이 아키텍처에서 발생할 수 있는 또 다른 문제는 **SSL 종료 후 리다이렉션 루프**입니다. ALB에서 SSL을 종료하고 NGINX로 HTTP 요청을 보내는데, NGINX가 모든 HTTP 요청을 HTTPS로 리다이렉션하도록 설정되어 있으면 무한 리다이렉션 루프가 발생합니다.

### 해결책

이 문제는 다음과 같이 해결할 수 있습니다:

1. **X-Forwarded-Proto 헤더 확인**
   ```nginx
   # 이미 HTTPS로 왔던 요청은 리다이렉션하지 않음
   if ($http_x_forwarded_proto != 'https') {
       return 301 https://$host$request_uri;
   }
   ```

2. **애플리케이션 설정 조정**
   - Spring Boot: `server.forward-headers-strategy=native`
   - Express.js: `app.set('trust proxy', true)`
   - Django: `SECURE_PROXY_SSL_HEADER = ('HTTP_X_FORWARDED_PROTO', 'https')`

## 주요 고려사항

ALB, NGINX, 애플리케이션 서버로 구성된 아키텍처를 구현할 때 고려해야 할 중요한 점은 다음과 같습니다:

1. **프록시 계층이 많을수록 헤더 관리가 중요해진다**
2. **원본 클라이언트 정보 보존을 위한 설정이 필수적이다**
3. **SSL 종료 지점과 리다이렉션 로직을 명확히 구분해야 한다**

이러한 문제들을 이해하고 적절히 해결하면 더 안정적이고 보안이 강화된 웹 인프라를 구축할 수 있습니다.
