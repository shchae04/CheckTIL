# RESTful API 구현 방법

RESTful API(Representational State Transfer API)는 웹 서비스를 설계하고 구현하기 위한 아키텍처 스타일입니다. 
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