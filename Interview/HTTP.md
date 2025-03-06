# HTTP (Hypertext Transfer Protocol)

HTTP는 World Wide Web의 기초가 되는 프로토콜로, 클라이언트와 서버 간의 통신을 위한 규약입니다.

## 1. HTTP 개요

### 정의
- HTTP는 분산, 협력적, 하이퍼미디어 정보 시스템을 위한 애플리케이션 레벨의 프로토콜입니다.
- TCP/IP 기반의 클라이언트-서버 프로토콜입니다.
- 기본적으로 TCP 포트 80을 사용합니다.

### 특징
1. **Stateless (무상태성)**
   - 각각의 요청이 독립적으로 처리됨
   - 이전 요청과 현재 요청 간의 관계가 없음

2. **Connectionless (비연결성)**
   - 요청에 대한 응답을 마치면 연결을 종료
   - HTTP/1.1부터는 keep-alive를 통해 일정 시간 동안 연결 유지 가능

## 2. HTTP 버전별 특징

```
HTTP/1.0 (1996)  →  HTTP/1.1 (1997)  →  HTTP/2.0 (2015)  →  HTTP/3.0 (2022)
   │                    │                    │                    │
   │                    │                    │                    │
   ▼                    ▼                    ▼                    ▼
기본 기능          Persistent Connection   Multiplexing        QUIC (UDP)
TCP 연결          Pipeline                Binary Protocol     향상된 멀티플렉싱
상태 코드          Host 헤더                HPACK               빠른 연결 설정
                 캐시 제어                 Server Push         내장된 TLS 1.3
```

### HTTP/1.0 (1996)
- 기본적인 HTTP 프로토콜의 첫 번째 표준 버전
- 매 요청마다 새로운 TCP 연결 필요 (Connection: close)
- 단순한 헤더 구조
- 상태 코드, 헤더, Content-Type 등 기본적인 기능 도입
- 한 번에 하나의 요청만 처리 가능

### HTTP/1.1 (1997)
- **Persistent Connection** 도입
  - keep-alive가 기본 설정
  - 동일한 TCP 연결을 재사용하여 여러 요청 처리 가능
- **Pipeline** 도입
  - 응답을 기다리지 않고 여러 요청을 연속적으로 보낼 수 있음
  - Head of Line Blocking 문제 존재
- **Host 헤더** 필수화
  - 동일 IP에서 여러 도메인을 호스팅 가능
- 청크 전송 인코딩 지원
- 캐시 제어 매커니즘 향상
- 추가 메서드 도입 (PUT, DELETE, OPTIONS 등)

### HTTP/2.0 (2015)
- **Multiplexing** 도입
  - 하나의 TCP 연결로 여러 요청/응답을 동시에 처리
  - HOL Blocking 문제 해결
- **Binary Protocol**
  - 텍스트 기반에서 이진 프로토콜로 변경
  - 더 효율적인 파싱
- **Header Compression (HPACK)**
  - 헤더 정보를 압축하여 전송
  - 중복 헤더 전송 방지
- **Server Push**
  - 클라이언트 요청 없이도 서버가 리소스를 보낼 수 있음
- **Stream Prioritization**
  - 요청에 우선순위 부여 가능

### HTTP/3.0 (2022)
- **QUIC 프로토콜** 사용
  - TCP 대신 UDP 기반
  - 연결 설정 시간 단축
- **향상된 멀티플렉싱**
  - TCP HOL Blocking 완전 해결
- **개선된 오류 처리**
  - 패킷 손실 시 해당 스트림만 영향
- **향상된 이동성**
  - IP 주소가 변경되어도 연결 유지
- **TLS 1.3 기본 내장**
  - 보안 강화
  - 연결 설정 시간 단축

## 3. HTTP 메시지 구조

### 요청 (Request) 구조
```
GET /path/to/resource HTTP/1.1
Host: www.example.com
User-Agent: Mozilla/5.0
Accept: text/html

[메시지 본문]
```

### 응답 (Response) 구조
```
HTTP/1.1 200 OK
Date: Mon, 23 May 2023 22:38:34 GMT
Content-Type: text/html; charset=UTF-8
Content-Length: 138

[메시지 본문]
```

## 3. HTTP 메서드

1. **GET**
   - 리소스 조회
   - 서버의 데이터를 변경하지 않음

2. **POST**
   - 새로운 리소스 생성
   - 서버의 상태 변경

3. **PUT**
   - 리소스 전체 수정
   - 해당 리소스가 없으면 생성

4. **PATCH**
   - 리소스 부분 수정

5. **DELETE**
   - 리소스 삭제

6. **HEAD**
   - GET과 동일하지만 응답 본문을 제외하고 헤더만 반환

7. **OPTIONS**
   - 서버가 지원하는 메서드 확인

## 4. 상태 코드

### 1xx (정보)
- 100 Continue: 클라이언트가 계속 요청을 이어가야 함

### 2xx (성공)
- 200 OK: 요청 성공
- 201 Created: 리소스 생성 성공
- 204 No Content: 성공했지만 응답 본문 없음

### 3xx (리다이렉션)
- 301 Moved Permanently: 영구 이동
- 302 Found: 임시 이동
- 304 Not Modified: 캐시된 리소스가 여전히 유효함

### 4xx (클라이언트 오류)
- 400 Bad Request: 잘못된 요청
- 401 Unauthorized: 인증 필요
- 403 Forbidden: 권한 없음
- 404 Not Found: 리소스를 찾을 수 없음

### 5xx (서버 오류)
- 500 Internal Server Error: 서버 내부 오류
- 502 Bad Gateway: 게이트웨이 오류
- 503 Service Unavailable: 서비스 이용 불가

## 5. 주요 헤더

### 일반 헤더
- Date: 메시지 생성 시각
- Connection: 연결 관리
- Cache-Control: 캐시 제어

### 요청 헤더
- Host: 요청하는 호스트명
- User-Agent: 클라이언트 정보
- Accept: 클라이언트가 받을 수 있는 컨텐츠 타입
- Authorization: 인증 정보

### 응답 헤더
- Server: 서버 정보
- Content-Type: 응답 본문의 타입
- Content-Length: 응답 본문의 길이
- Set-Cookie: 쿠키 설정

## 참고 문서
- [RFC 2616: HTTP/1.1](https://datatracker.ietf.org/doc/html/rfc2616)
- [RFC 7230: HTTP/1.1 Message Syntax and Routing](https://datatracker.ietf.org/doc/html/rfc7230)
- [RFC 7231: HTTP/1.1 Semantics and Content](https://datatracker.ietf.org/doc/html/rfc7231)
- [RFC 7232: HTTP/1.1 Conditional Requests](https://datatracker.ietf.org/doc/html/rfc7232)
- [RFC 7233: HTTP/1.1 Range Requests](https://datatracker.ietf.org/doc/html/rfc7233)
- [RFC 7234: HTTP/1.1 Caching](https://datatracker.ietf.org/doc/html/rfc7234)
- [RFC 7235: HTTP/1.1 Authentication](https://datatracker.ietf.org/doc/html/rfc7235)
- [RFC 7540: HTTP/2](https://datatracker.ietf.org/doc/html/rfc7540)
- [RFC 9110: HTTP Semantics](https://datatracker.ietf.org/doc/html/rfc9110)
