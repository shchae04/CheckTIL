# 웹사이트 접근 과정은 어떻게 되나요??

웹사이트에 처음 접근할 때 발생하는 일련의 과정을 설명합니다. 이 과정은 URL 입력부터 웹페이지 렌더링까지의 전체 흐름을 포함합니다.

## 1. 웹사이트 접근 과정 개요

```
URL 입력 → DNS 조회 → TCP 연결 → TLS 핸드셰이크 → HTTP 요청 → 서버 처리 → HTTP 응답 → 브라우저 렌더링
```

## 2. 상세 과정

### 1) URL 입력
- 사용자가 브라우저 주소창에 URL(예: `https://www.example.com`)을 입력합니다.
- 브라우저는 입력된 URL을 파싱하여 프로토콜(https), 도메인(www.example.com), 경로(/) 등으로 분리합니다.

### 2) DNS 조회
- 브라우저는 도메인 이름(www.example.com)을 IP 주소로 변환하기 위해 DNS 조회를 수행합니다.
- DNS 조회 과정:
    1. 브라우저 캐시 확인
    2. OS 캐시 확인
    3. 라우터 캐시 확인
    4. ISP DNS 서버 조회
    5. 재귀적 DNS 조회 (Root → TLD → Authoritative DNS 서버)

### 3) TCP 연결 수립
- 브라우저는 얻은 IP 주소로 서버와 TCP 연결을 수립합니다.
- **3-way 핸드셰이크** 과정:
    1. 클라이언트가 SYN 패킷 전송
    2. 서버가 SYN-ACK 패킷으로 응답
    3. 클라이언트가 ACK 패킷 전송하여 연결 확립

### 4) TLS 핸드셰이크 (HTTPS의 경우)
- HTTPS 사이트의 경우, 암호화된 연결을 위한 TLS 핸드셰이크가 진행됩니다.
- TLS 핸드셰이크 과정:
    1. 클라이언트가 지원하는 암호화 방식 전송
    2. 서버가 암호화 방식 선택 및 인증서 전송
    3. 클라이언트가 인증서 검증
    4. 대칭 키 교환 및 암호화 통신 시작

### 5) HTTP 요청 전송
- 브라우저는 서버에 HTTP 요청을 전송합니다.
- 요청 예시:
  ```
  GET / HTTP/1.1
  Host: www.example.com
  User-Agent: Mozilla/5.0
  Accept: text/html,application/xhtml+xml
  Accept-Language: ko-KR,ko
  Connection: keep-alive
  ```

### 6) 서버 처리
- 웹 서버는 HTTP 요청을 수신하고 처리합니다.
- 처리 과정:
    1. 요청 분석 (메서드, 경로, 헤더 등)
    2. 인증/인가 확인 (필요한 경우)
    3. 요청된 리소스 검색
    4. 동적 콘텐츠 생성 (필요한 경우)
    5. 응답 생성

### 7) HTTP 응답 수신
- 서버는 처리 결과를 HTTP 응답으로 클라이언트에게 전송합니다.
- 응답 예시:
  ```
  HTTP/1.1 200 OK
  Date: Mon, 23 May 2023 22:38:34 GMT
  Content-Type: text/html; charset=UTF-8
  Content-Length: 138
  Cache-Control: max-age=3600
  
  <!DOCTYPE html>
  <html>
    <head>
      <title>Example Domain</title>
      ...
  ```

### 8) 브라우저 렌더링
- 브라우저는 수신한 HTML, CSS, JavaScript를 처리하여 웹페이지를 렌더링합니다.
- 렌더링 과정:
    1. **DOM 트리 구축**: HTML 파싱하여 DOM(Document Object Model) 트리 생성
    2. **CSSOM 트리 구축**: CSS 파싱하여 CSSOM(CSS Object Model) 트리 생성
    3. **렌더 트리 구축**: DOM과 CSSOM을 결합하여 렌더 트리 생성
    4. **레이아웃(리플로우)**: 각 요소의 크기와 위치 계산
    5. **페인팅**: 화면에 픽셀 렌더링
    6. **컴포지팅**: 레이어를 합성하여 최종 화면 생성

### 9) 추가 리소스 로딩
- HTML 파싱 중 이미지, CSS, JavaScript 등의 추가 리소스가 발견되면 각각에 대해 위 과정(DNS 조회부터)을 반복합니다.
- 최신 브라우저는 병렬 연결을 통해 여러 리소스를 동시에 로드합니다.

## 3. 최적화 기법

### 성능 최적화
1. **DNS 프리페칭**
   ```html
   <link rel="dns-prefetch" href="//example.com">
   ```

2. **프리로딩/프리커넥트**
   ```html
   <link rel="preload" href="style.css" as="style">
   <link rel="preconnect" href="https://example.com">
   ```

3. **HTTP/2 활용**
    - 멀티플렉싱으로 여러 요청 병렬 처리
    - 헤더 압축으로 오버헤드 감소

4. **브라우저 캐싱**
    - 적절한 Cache-Control 헤더 설정
    - ETag 활용

## 4. 보안 고려사항

### 주요 보안 요소
1. **HTTPS 사용**
    - 데이터 암호화로 중간자 공격 방지
    - 웹사이트 신뢰성 향상

2. **보안 헤더 설정**
   ```
   Strict-Transport-Security: max-age=31536000
   Content-Security-Policy: default-src 'self'
   X-Content-Type-Options: nosniff
   ```

3. **쿠키 보안**
   ```
   Set-Cookie: sessionId=abc123; HttpOnly; Secure; SameSite=Strict
   ```

## 참고 자료
- [MDN: 웹사이트가 작동하는 방법](https://developer.mozilla.org/ko/docs/Learn/Getting_started_with_the_web/How_the_Web_works)
- [Google Developers: 중요 렌더링 경로](https://developers.google.com/web/fundamentals/performance/critical-rendering-path)
- [High Performance Browser Networking](https://hpbn.co/)
- [Web.dev: 웹 성능 최적화](https://web.dev/performance-optimizing-content-efficiency/)