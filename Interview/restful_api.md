# RESTful API 구현 방법

RESTful API(Representational State Transfer API)는 웹 서비스를 설계하고 구현하기 위한 아키텍처 스타일입니다. 

## REST란 무엇인가요?
REST(Representational State Transfer)는 자원의 "표현"을 이용해 상태를 주고받는 아키텍처 스타일을 의미합니다. 여기서 자원(resource)은 소프트웨어가 관리하는 모든 것을 뜻하며, 자원의 표현(representation)은 그 자원을 식별하기 위한 이름 또는 형태를 의미합니다. 예를 들어 서버가 관리하는 주문 데이터라는 자원은 `order`로 표현할 수 있습니다. REST는 HTTP 프로토콜을 기반으로 하며, HTTP URI로 자원을 명시하고 HTTP 메서드(GET, POST, PUT, PATCH, DELETE 등)를 통해 자원에 CRUD 연산을 적용합니다.

## API와 REST API
API(Application Programming Interface)는 프로그램 간 데이터를 주고받을 수 있도록 하는 일종의 출입구(인터페이스)입니다. API가 REST 원칙을 기반으로 구현되어 있다면 이를 REST API라고 부릅니다.

## REST의 장단점
- 장점
  - 클라이언트와 서버의 역할을 명확히 분리하여 독립적인 진화를 돕습니다.
  - HTTP 표준을 따르므로 대부분의 플랫폼과 언어에서 폭넓게 사용할 수 있습니다.
  - curl, Postman 등으로 손쉽게 요청/응답을 테스트할 수 있습니다.
- 단점
  - 요청-응답(request/response) 스타일의 통신만 기본적으로 지원합니다.
  - HTTP 메서드만으로 복잡한 행위를 표현하기 어려울 수 있습니다.
  - 한 번의 요청으로 여러 종류의 자원을 조합해 가져오기 어렵습니다.

## REST와 JSON: 장단점
REST 방식에서는 자원의 상태를 전송하기 위해 일반적으로 JSON을 사용합니다. JSON과 같은 텍스트 포맷은 자기 서술적(self-descriptive)이며, 소비자(클라이언트)가 관심 있는 값만 선택적으로 사용하고 나머지는 무시할 수 있어 메시지 구조 변경 시 하위 호환성 유지에 유리합니다. 반면 메시지 길이가 상대적으로 길어 네트워크 트래픽을 더 소모할 수 있고, 전송 속도가 느려질 수 있으며, 메시지 파싱(해석)에 따른 오버헤드가 발생할 수 있습니다.

## RESTful API란?

REST는 Representational State Transfer의 약자로, 2000년 Roy Fielding의 박사 논문에서 처음 소개되었습니다. REST는 웹의 기존 기술과 HTTP 프로토콜을 그대로 활용하여 웹 서비스를 설계하는 아키텍처 원칙의 모음입니다.

RESTful API는 다음과 같은 특징을 가집니다:

1. **자원(Resource) 기반**: 모든 것을 자원으로 표현하고, 각 자원은 고유한 URI(Uniform Resource Identifier)를 가집니다.
2. **표현(Representation)**: 자원의 상태는 JSON, XML 등의 형식으로 표현됩니다.
3. **무상태(Stateless)**: 각 요청은 이전 요청과 독립적이며, 서버는 클라이언트의 상태를 저장하지 않습니다.
4. **균일한 인터페이스(Uniform Interface)**: 자원에 대한 조작은 HTTP 메서드(GET, POST, PUT, DELETE 등)를 통해 일관되게 이루어집니다.

## REST 아키텍처의 6가지 제약 조건

Roy Fielding이 정의한 REST 아키텍처의 6가지 제약 조건:

1. **클라이언트-서버 구조(Client-Server)**: 관심사의 분리를 통해 클라이언트와 서버가 독립적으로 진화할 수 있습니다.
2. **무상태(Stateless)**: 각 요청은 필요한 모든 정보를 포함해야 합니다.
3. **캐시 가능(Cacheable)**: 응답은 캐시 가능 여부를 명시해야 합니다.
4. **계층화 시스템(Layered System)**: 클라이언트는 서버와 직접 연결되었는지, 중간 서버를 통해 연결되었는지 알 수 없습니다.
5. **코드 온 디맨드(Code on Demand, 선택사항)**: 서버는 클라이언트에서 실행 가능한 코드를 전송할 수 있습니다.
6. **균일한 인터페이스(Uniform Interface)**: 자원 식별, 표현을 통한 자원 조작, 자기 서술적 메시지, HATEOAS(Hypermedia as the Engine of Application State) 등의 원칙을 따릅니다.

## HTTP 메서드와 CRUD 연산

RESTful API는 HTTP 메서드를 사용하여 자원에 대한 CRUD(Create, Read, Update, Delete) 연산을 수행합니다:

| HTTP 메서드 | CRUD 연산 | 설명 |
|------------|----------|------|
| GET | Read | 자원을 조회합니다. 서버의 상태를 변경하지 않습니다. |
| POST | Create | 새로운 자원을 생성합니다. |
| PUT | Update/Replace | 자원을 완전히 대체합니다. 멱등성을 가집니다. |
| PATCH | Update/Modify | 자원의 일부를 수정합니다. |
| DELETE | Delete | 자원을 삭제합니다. |

## URI 설계 원칙

효과적인 RESTful API를 위한 URI 설계 원칙:

1. **자원을 나타내는 명사 사용**: `/users`, `/products` 등
2. **복수형 명사 사용**: `/users`(O), `/user`(X)
3. **소문자 사용**: `/users`(O), `/Users`(X)
4. **하이픈(-) 사용**: `/user-profiles`(O), `/user_profiles`(X)
5. **행위를 URI에 포함시키지 않음**: `/users`(O), `/getUsers`(X)
6. **계층 관계 표현**: `/users/123/orders`

## HTTP 상태 코드

RESTful API는 적절한 HTTP 상태 코드를 사용하여 응답의 결과를 나타냅니다:

### 2xx (성공)
- **200 OK**: 요청이 성공적으로 처리됨
- **201 Created**: 자원이 성공적으로 생성됨
- **204 No Content**: 요청은 성공했지만 반환할 콘텐츠가 없음

### 3xx (리다이렉션)
- **301 Moved Permanently**: 자원이 영구적으로 다른 위치로 이동됨
- **304 Not Modified**: 클라이언트의 캐시된 자원이 여전히 유효함

### 4xx (클라이언트 오류)
- **400 Bad Request**: 잘못된 요청
- **401 Unauthorized**: 인증 필요
- **403 Forbidden**: 권한 없음
- **404 Not Found**: 자원을 찾을 수 없음
- **405 Method Not Allowed**: 허용되지 않은 HTTP 메서드
- **409 Conflict**: 자원의 현재 상태와 충돌

### 5xx (서버 오류)
- **500 Internal Server Error**: 서버 내부 오류
- **502 Bad Gateway**: 게이트웨이 오류
- **503 Service Unavailable**: 서비스 일시적 사용 불가

## RESTful API 구현 예시 (Spring Boot)

Spring Boot를 사용한 RESTful API 구현 예시:

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // 모든 사용자 조회
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    // 특정 사용자 조회
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 새 사용자 생성
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.save(user);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedUser.getId())
                .toUri();
        return ResponseEntity.created(location).body(savedUser);
    }

    // 사용자 정보 수정
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        return userService.findById(id)
                .map(existingUser -> {
                    User updatedUser = userService.update(id, user);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 사용자 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        return userService.findById(id)
                .map(user -> {
                    userService.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
```

## RESTful API 보안

RESTful API 보안을 위한 주요 방법:

1. **HTTPS 사용**: 모든 API 통신은 HTTPS를 통해 암호화되어야 합니다.
2. **인증과 인가**:
   - **JWT(JSON Web Token)**: 상태를 저장하지 않는 인증 메커니즘
   - **OAuth 2.0**: 제3자 애플리케이션에 대한 접근 권한 부여
   - **API 키**: 간단한 인증 방식
3. **입력 유효성 검사**: 모든 클라이언트 입력은 서버에서 검증해야 합니다.
4. **속도 제한(Rate Limiting)**: API 호출 횟수를 제한하여 DoS 공격 방지
5. **CORS(Cross-Origin Resource Sharing)**: 허용된 도메인만 API에 접근할 수 있도록 설정

## RESTful API 문서화

API 문서화는 개발자가 API를 이해하고 사용하는 데 중요합니다:

1. **Swagger/OpenAPI**: API 명세를 자동으로 생성하고 문서화
2. **Spring REST Docs**: 테스트 코드를 기반으로 API 문서 생성
3. **Postman**: API 테스트 및 문서화 도구

## RESTful API 설계 모범 사례

1. **버전 관리**: `/api/v1/users`와 같이 API 버전을 URI에 포함
2. **페이지네이션**: 대량의 데이터를 처리할 때 페이지네이션 제공
3. **필터링, 정렬, 검색**: 쿼리 파라미터를 통한 데이터 필터링 지원
4. **HATEOAS(Hypermedia as the Engine of Application State)**: 응답에 관련 리소스 링크 포함
5. **일관된 오류 처리**: 표준화된 오류 응답 형식 사용
6. **적절한 HTTP 메서드와 상태 코드 사용**

## 결론

RESTful API는 웹 서비스 설계를 위한 효과적인 아키텍처 스타일입니다. REST 원칙을 따르면 확장성이 높고, 유지보수가 용이하며, 클라이언트-서버 간 효율적인 통신이 가능한 API를 구현할 수 있습니다. 하지만 모든 상황에 REST가 최적의 솔루션은 아니며, GraphQL이나 gRPC와 같은 대안 기술도 고려할 수 있습니다.

RESTful API를 구현할 때는 HTTP 프로토콜의 특성을 최대한 활용하고, 자원 중심의 설계, 적절한 상태 코드 사용, 보안 고려 등 모범 사례를 따르는 것이 중요합니다.

## REST 원칙 위배 사례와 안티패턴

아래는 REST 원칙(무상태, 균일한 인터페이스, 적절한 HTTP 메서드/상태 코드 등)을 위배하는 흔한 사례들과, 왜 문제가 되는지 및 올바른 대안입니다.

### 1) HTTP 상태 코드와 응답 본문이 불일치 (예: 200 OK + 내부 코드 400)
- 나쁜 예시 (안티패턴): HTTP 상태는 200 OK이지만, 본문에 내부 에러 코드를 담아 전달
```
HTTP/1.1 200 OK
Content-Type: application/json

{
  "status": 400,
  "message": "유효하지 않은 요청입니다. 'email' 필드가 필요합니다."
}
```
문제점:
- 클라이언트, 프록시, 로드밸런서, 재시도/모니터링 도구가 정상으로 오인할 수 있습니다.
- 캐시 계층이 200 응답을 캐싱하여 오류를 영속화할 수 있습니다.
- HTTP 의미론(semantic)과 어긋나 유지보수성, 상호운용성이 떨어집니다.

- 올바른 예시: HTTP 상태 코드를 정확히 사용하고, 표준화된 오류 포맷을 사용
```
HTTP/1.1 400 Bad Request
Content-Type: application/problem+json

{
  "type": "https://example.com/problems/validation-error",
  "title": "Validation failed",
  "status": 400,
  "detail": "'email' 필드가 필요합니다.",
  "instance": "/api/users"
}
```
참고: RFC 7807(Problem Details for HTTP APIs)을 사용하면 오류 표현을 일관되게 유지할 수 있습니다. 커스텀 포맷을 쓰더라도 HTTP 상태는 반드시 실제 결과와 일치시켜야 합니다.

### 2) GET으로 상태 변경 (안전성/멱등성 위배)
- 나쁜 예시:
```
GET /users/123/delete
```
- 올바른 예시:
```
DELETE /users/123
```
GET은 안전(safe)해야 하며 서버 상태를 변경하지 않아야 합니다. 크롤러/프리패처가 GET을 호출할 수 있어 큰 사고로 이어질 수 있습니다.

### 3) 동사형 URI 사용 (균일한 인터페이스 위배)
- 나쁜 예시: `POST /createUser`, `GET /getUsers`
- 올바른 예시: `POST /users`, `GET /users`, `GET /users/{id}`
자원은 명사로 표현하고, 행위는 HTTP 메서드로 표현합니다.

### 4) 204 No Content에 본문 포함
- 나쁜 예시:
```
HTTP/1.1 204 No Content
Content-Type: application/json

{"result":"deleted"}
```
- 올바른 예시: 204일 경우 본문을 비워두거나, 본문이 필요하면 200 OK로 돌려줍니다.

### 5) PUT의 멱등성 위배
- 나쁜 예시: `PUT /counters/1`가 호출될 때마다 값이 +1 증가(부수효과 누적)
- 올바른 예시:
  - 멱등 업데이트: `PUT /counters/1`와 함께 전체 상태를 명시적으로 제공
  - 부분 변경: `PATCH /counters/1`로 증분 업데이트(payload: {"op":"increment","value":1})
  - 또는 명시적 행위 리소스 모델링: `POST /counters/1/increments`

### 6) 무상태(Stateless) 위배
- 나쁜 예시: 서버가 세션에 클라이언트 상태를 강하게 저장해 각 요청 간 의존
- 올바른 예시: 각 요청이 필요한 컨텍스트(인증 토큰, 필수 파라미터 등)를 자체 포함. 세션이 필요해도 서버 간 공유/외부 저장소를 통해 상태 의존을 낮춤.

### 7) 콘텐츠 협상 무시
- 나쁜 예시: `Accept: application/xml`을 보내도 무조건 JSON만 반환하거나, 지원하지 않는 미디어 타입 요청에도 200으로 응답
- 올바른 예시: `Accept`를 존중해 가능한 표현을 제공. 미지원인 경우 `406 Not Acceptable`을 반환.

### 8) 항상 200으로 래핑하는 내부 상태 코드 패턴
- 나쁜 예시: 모든 응답을 `{ "success": false, "code": 1234 }` 형태로 200과 함께 반환
- 문제점: 상호운용성 저하, 공통 라이브러리/중간계층(모니터링, 리트라이, 캐시)의 동작을 왜곡
- 권장: HTTP 상태 코드를 우선적으로 의미 있게 사용하고, 본문은 도메인 레벨의 세부 사유/코드를 제공

### 9) 상태 코드 남용/오사용
- 생성 성공을 200으로만 응답하고 `Location` 헤더를 제공하지 않음 → 가능하면 `201 Created` + `Location` 제공
- 본문이 있음에도 `204 No Content` 사용 → 200/201 등 적절한 코드 사용
- 충돌 상황(중복 키, 버전 충돌)에 400 사용 → `409 Conflict`가 더 적절
- 유효성 실패를 모호한 500으로 반환 → 클라이언트 오류 범주인 4xx(주로 400/422)를 고려

---

간단 체크리스트
- HTTP 상태 코드가 실제 결과와 정확히 일치하는가?
- 안전/멱등성 규칙(GET은 안전, PUT은 멱등 등)을 지키는가?
- URI는 자원 중심의 명사로 표현되었는가(행위는 메서드로)?
- 무상태성, 캐시 가능성, 콘텐츠 협상을 준수하는가?
- 201/204/409 등 의미 있는 상태 코드를 적시에 사용하고 있는가?

## 때로는 '그렇게' 설계하는 게 더 나을 때도 있다 (실무적 예외와 트레이드오프)

원칙을 지키는 것이 기본이지만, 현실 세계에서는 제약과 목표에 따라 "비정형" 설계가 더 효율적일 때가 있습니다. 아래는 그런 상황과, 함께 고려해야 할 보호장치입니다.

### 1) 200 OK + 본문 수준 에러 정보가 유리한 경우
- 스트리밍/롱폴링: HTTP 연결 자체는 성공(200)이고, 각 메시지 프레임에서 업무 상태를 개별적으로 전달해야 할 때(예: SSE, 이벤트 스트림).
- GraphQL 스타일: 단일 엔드포인트에서 부분 성공/실패를 함께 담아야 할 때(`errors` 필드).
- 레거시/게이트웨이 제약: 일부 프록시/SDK/게이트웨이가 4xx/5xx를 강제로 재시도·차단·알람 처리해 UX가 악화되는 경우.
권장 보호장치:
- 캐시 금지: `Cache-Control: no-store`(또는 최소한 `no-cache`).
- 모니터링/도구 호환: 커스텀 헤더(예: `X-Http-Status: 400`)나 표준 포맷(Problem Details의 축약형)을 함께 제공.
- 문서화: 왜 200을 사용하는지, 클라이언트 파싱 규칙과 재시도 정책을 명확히 문서화.

참고: 가능하면 HTTP 상태도 함께 사용하는 것이 상호운용성 측면에서 가장 안전합니다. 위 접근은 특정 제약 하에서의 예외로 간주하세요.

### 2) 행위(커맨드) 중심의 액션 엔드포인트
도메인에서 "자원 상태 교체"로 모델링하기 어려운 커맨드(결제 캡처, 주문 취소, 이메일 재전송 등)는 액션형 경로가 실용적입니다.
- 예시: `POST /orders/{id}/cancel`, `POST /payments/{id}/captures`
- 대안 모델링: 액션을 별도 리소스로 취급(`POST /orders/{id}/cancellations`), 혹은 내부 통신은 RPC(gRPC)로 설계.

### 3) 사용성 때문에 링크 클릭만으로 상태 변경이 필요한 경우(예: 이메일 구독 해지)
GET으로 상태 변경은 원칙적으로 지양해야 하나, 이메일 링크 UX 등에서 요구될 수 있습니다. 이때는 강력한 방어 장치를 둡니다.
- 일회성/짧은 TTL 토큰, 재사용 불가 처리, CSRF 위험 최소화.
- 가능한 경우 확인 페이지를 거쳐 `POST`로 최종 반영.
- 캐시 금지 및 부작용의 멱등성 보장(여러 번 클릭해도 같은 결과).

### 4) 서버 세션(상태) 사용이 단순성과 보안에 유리한 경우
완전 무상태보다 짧은 수명의 서버 세션이 안전·단순한 경우가 있습니다(예: 결제 3DS 플로우, OAuth 승인 과정 중 단계 상태 유지).
- 권장: 외부 세션 스토어(레디스 등), 만료 설정, 세션 고정 방지, 스티키 세션 최소화.

### 5) 하나의 미디어 타입만 지원(콘텐츠 협상 축소)
다양한 표현 대신 `application/json`만 제공해 복잡도를 낮출 수 있습니다.
- 권장: 문서에 명시하고, 미지원 `Accept`에는 406 또는 기본(JSON)으로 일관 응답.

### 6) 일괄 처리/배치 엔드포인트
네트워크 오버헤드가 큰 환경에서는 배치가 효과적입니다.
- 예시: `POST /users:batchGet` 또는 `POST /orders/search`로 복합 조회.
- 부분 성공 표현: `207 Multi-Status` 또는 결과 항목별 상태 코드를 본문에 포함.

### 7) 일관 응답 래핑(envelope) 채택
프론트엔드 통합을 위해 공통 래핑을 쓰기도 합니다.
- 예시: `{ "success": true|false, "data": ..., "error": {code, message} }`
- 권장: HTTP 상태 코드는 의미 있게 유지(오류는 4xx/5xx). 래핑은 클라이언트 편의를 위한 보조 수단.

---
현실적 가이드
- 예외는 분명한 제약/목표에 의해 정당화되어야 합니다.
- 예외를 택할 경우, 캐시/재시도/보안/모니터링 영향과 보호장치를 문서화합니다.
- 팀/소비자와 사전 합의하고, 가능하면 점진적으로 표준(정석) 설계로 회귀할 경로를 마련합니다.
