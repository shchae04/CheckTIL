# POST가 GET보다 안전한가?

HTTP 메서드 중 POST와 GET의 보안성을 비교하는 것은 웹 개발에서 중요한 주제입니다. 단순히 "POST가 더 안전하다"고 말할 수는 없으며, 각각의 특성과 사용 목적에 따라 보안 측면에서 다른 장단점을 가집니다.

## 1. 기본 개념

### GET 메서드
- **목적**: 서버로부터 데이터를 조회
- **특징**: 
  - 멱등성(Idempotent) - 여러 번 호출해도 결과가 동일
  - 안전성(Safe) - 서버의 상태를 변경하지 않음
  - 캐시 가능

### POST 메서드
- **목적**: 서버에 데이터를 전송하여 리소스 생성/수정
- **특징**:
  - 비멱등성 - 호출할 때마다 새로운 결과 생성 가능
  - 비안전성 - 서버의 상태를 변경
  - 캐시 불가능 (일반적으로)

## 2. 보안 측면 비교

### 2.1 데이터 노출 위험

#### GET의 위험성
```http
GET /login?username=admin&password=123456 HTTP/1.1
Host: example.com
```

**문제점:**
- URL에 민감한 정보가 노출됨
- 브라우저 히스토리에 저장됨
- 서버 로그에 기록됨
- 레퍼러 헤더를 통해 다른 사이트로 전송될 수 있음

#### POST의 상대적 안전성
```http
POST /login HTTP/1.1
Host: example.com
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456
```

**장점:**
- 요청 본문(body)에 데이터 포함
- URL에 민감한 정보가 노출되지 않음
- 브라우저 히스토리에 파라미터가 저장되지 않음

### 2.2 브라우저 히스토리와 캐싱

#### GET
```javascript
// 브라우저 히스토리에 저장되는 예시
history.pushState(null, null, '/search?query=confidential_data&token=abc123');
```

**위험 요소:**
- 브라우저 히스토리에 URL 파라미터가 저장됨
- 공용 컴퓨터에서 다음 사용자가 히스토리를 통해 정보 확인 가능
- 브라우저와 프록시 서버에서 캐싱됨

#### POST
- 요청 본문은 브라우저 히스토리에 저장되지 않음
- 일반적으로 캐싱되지 않음
- 뒤로가기 시 "다시 제출하시겠습니까?" 경고 표시

### 2.3 서버 로그

#### GET 요청 로그
```log
192.168.1.100 - - [25/Dec/2023:10:15:30 +0000] "GET /api/users?ssn=123-45-6789&creditcard=1234567890123456 HTTP/1.1" 200 1234
```

#### POST 요청 로그
```log
192.168.1.100 - - [25/Dec/2023:10:15:30 +0000] "POST /api/users HTTP/1.1" 200 1234
```

**POST의 장점:**
- 민감한 데이터가 액세스 로그에 기록되지 않음
- 로그 파일 유출 시 데이터 노출 위험 감소

### 2.4 CSRF (Cross-Site Request Forgery) 취약점

#### GET의 CSRF 위험성
```html
<!-- 악성 사이트에서 GET 요청으로 CSRF 공격 -->
<img src="http://bank.com/transfer?to=attacker&amount=1000000" />
```

**문제점:**
- 단순한 HTML 태그로도 요청 실행 가능
- 사용자가 인지하지 못하는 상태에서 요청 발생

#### POST의 CSRF 방어
```html
<!-- POST는 단순한 태그로 실행 불가능 -->
<form action="http://bank.com/transfer" method="POST">
    <input type="hidden" name="to" value="attacker" />
    <input type="hidden" name="amount" value="1000000" />
    <input type="hidden" name="csrf_token" value="random_token" />
</form>
```

**장점:**
- 단순한 GET 요청보다 CSRF 공격이 어려움
- CSRF 토큰과 함께 사용하면 더욱 안전

## 3. 실제 보안 고려사항

### 3.1 HTTPS의 중요성

```http
# HTTP (위험)
POST http://example.com/login HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456

# HTTPS (안전)
POST https://example.com/login HTTP/1.1
Content-Type: application/x-www-form-urlencoded

username=admin&password=123456
```

**중요 포인트:**
- HTTP에서는 GET, POST 모두 평문으로 전송됨
- HTTPS 사용 시 GET의 URL 파라미터도 암호화됨
- 네트워크 레벨에서는 HTTPS가 더 중요한 보안 요소

### 3.2 적절한 메서드 선택

#### GET 사용이 적절한 경우
```http
GET /api/products?category=electronics&page=1 HTTP/1.1
```
- 데이터 조회
- 검색 기능
- 페이징
- 필터링

#### POST 사용이 필수인 경우
```http
POST /api/users HTTP/1.1
Content-Type: application/json

{
    "username": "newuser",
    "password": "securepassword",
    "email": "user@example.com"
}
```
- 사용자 생성/수정
- 로그인 처리
- 결제 처리
- 파일 업로드

## 4. 보안 모범 사례

### 4.1 민감한 데이터 처리
```java
// 잘못된 예시 - GET으로 민감한 데이터 전송
@GetMapping("/login")
public String login(@RequestParam String username, 
                   @RequestParam String password) {
    // 위험: URL에 비밀번호 노출
}

// 올바른 예시 - POST로 민감한 데이터 전송
@PostMapping("/login")
public String login(@RequestBody LoginRequest request) {
    // 안전: 요청 본문에 데이터 포함
}
```

### 4.2 CSRF 방어
```java
@PostMapping("/transfer")
@CsrfToken
public String transfer(@RequestBody TransferRequest request,
                      @RequestHeader("X-CSRF-TOKEN") String csrfToken) {
    // CSRF 토큰 검증
    if (!csrfService.validateToken(csrfToken)) {
        throw new SecurityException("Invalid CSRF token");
    }
    // 송금 처리
}
```

### 4.3 입력 검증 및 인코딩
```java
@PostMapping("/search")
public String search(@RequestParam String query) {
    // 입력 검증
    if (query.length() > 100) {
        throw new IllegalArgumentException("Query too long");
    }
    
    // XSS 방어를 위한 인코딩
    String encodedQuery = HtmlUtils.htmlEscape(query);
    
    return searchService.search(encodedQuery);
}
```

## 5. 결론

### POST가 GET보다 상대적으로 안전한 경우:
1. **민감한 데이터 전송**: 비밀번호, 개인정보 등
2. **서버 상태 변경**: 데이터 생성, 수정, 삭제
3. **CSRF 공격 방어**: 상태 변경 작업에서
4. **로그 보안**: 액세스 로그에 민감한 정보 노출 방지

### 하지만 절대적이지 않은 이유:
1. **HTTPS 미사용 시**: 두 방법 모두 평문 전송
2. **부적절한 사용**: POST로 조회 작업을 하는 경우
3. **추가 보안 조치 필요**: CSRF 토큰, 입력 검증 등

### 핵심 원칙:
- **GET**: 데이터 조회, 멱등성이 보장되는 안전한 작업
- **POST**: 데이터 변경, 민감한 정보 전송
- **HTTPS**: 네트워크 레벨 보안의 필수 요소
- **종합적 보안**: 메서드 선택 + HTTPS + 입력 검증 + CSRF 방어

결국 "POST가 GET보다 안전한가?"라는 질문에 대한 답은 **"상황에 따라 다르며, 적절한 메서드를 선택하고 추가적인 보안 조치를 함께 적용해야 한다"**입니다.