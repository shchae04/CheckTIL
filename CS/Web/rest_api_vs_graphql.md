# REST API vs GraphQL: 백엔드 개발자 관점에서의 5단계 비교 분석

## 1. 한 줄 정의
- REST API는 HTTP 프로토콜을 기반으로 리소스 중심의 상태 없는 아키텍처 스타일이며, GraphQL은 클라이언트가 필요한 데이터를 정확히 요청할 수 있는 쿼리 언어이자 런타임이다. 백엔드 관점에서 REST는 다수의 엔드포인트를 통한 CRUD 중심 설계이고, GraphQL은 단일 엔드포인트를 통한 스키마 기반 데이터 페칭 시스템이다.

---

## 2. REST API vs GraphQL 5단계 비교

### 2-1. 1단계: 데이터 요청 방식

#### REST API
- **개념**: 리소스별로 독립적인 HTTP 엔드포인트 제공
- **백엔드 관점**: 각 리소스마다 별도의 컨트롤러/핸들러 함수 구현
- **핵심 포인트**:
  - HTTP 메서드(GET, POST, PUT, DELETE)로 CRUD 연산 구분
  - URL 경로로 리소스 식별 (`/users/123`, `/posts/456`)
  - 고정된 응답 구조로 예측 가능한 데이터 반환

```javascript
// REST API 요청 예시
GET /api/users/123
GET /api/users/123/posts
GET /api/posts/456/comments

// 응답 구조 (고정)
{
  "id": 123,
  "name": "김개발",
  "email": "kim@example.com",
  "created_at": "2023-01-01T00:00:00Z"
}
```

#### GraphQL
- **개념**: 단일 엔드포인트로 클라이언트가 원하는 데이터 구조 명시
- **백엔드 관점**: 스키마 정의와 리졸버 함수로 데이터 페칭 로직 구현
- **핵심 포인트**:
  - 클라이언트가 필요한 필드만 선택적으로 요청
  - 중첩된 관계 데이터를 한 번의 요청으로 획득
  - 스키마 기반의 강타입 시스템

```graphql
# GraphQL 쿼리 예시
query {
  user(id: 123) {
    name
    email
    posts {
      title
      comments {
        content
        author {
          name
        }
      }
    }
  }
}
```

### 2-2. 2단계: 데이터 페칭 전략

#### REST API - Over/Under Fetching
- **Over-fetching**: 불필요한 데이터까지 전송
- **Under-fetching**: 여러 요청으로 필요한 데이터 수집
- **백엔드 관점**: 각 엔드포인트마다 고정된 응답 스키마

```javascript
// Over-fetching 예시 - 이름만 필요하지만 모든 데이터 전송
GET /api/users/123
{
  "id": 123,
  "name": "김개발",
  "email": "kim@example.com",
  "bio": "긴 자기소개...",
  "avatar_url": "https://...",
  "created_at": "2023-01-01T00:00:00Z"
}

// Under-fetching 예시 - 여러 요청 필요
GET /api/users/123         // 사용자 정보
GET /api/users/123/posts   // 게시글 목록
GET /api/posts/456/comments // 댓글 목록
```

#### GraphQL - 정확한 데이터 페칭
- **개념**: 요청한 필드만 정확히 반환
- **백엔드 관점**: 리졸버 체인을 통한 효율적 데이터 로딩

```graphql
# 이름만 필요한 경우
query {
  user(id: 123) {
    name
  }
}

# 응답
{
  "data": {
    "user": {
      "name": "김개발"
    }
  }
}
```

### 2-3. 3단계: 스키마와 타입 시스템

#### REST API
- **개념**: API 문서나 OpenAPI(Swagger)로 스키마 정의
- **백엔드 관점**: 런타임에 타입 검증, 문서화와 구현이 분리
- **핵심 포인트**:
  - 문서와 실제 구현의 싱크 문제 가능
  - 각 엔드포인트마다 별도 문서화 필요
  - 클라이언트가 API 변경사항 추적 어려움

```yaml
# OpenAPI 스키마 예시
paths:
  /users/{id}:
    get:
      parameters:
        - name: id
          in: path
          required: true
          schema:
            type: integer
      responses:
        '200':
          description: 사용자 정보
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
```

#### GraphQL
- **개념**: 스키마 우선(Schema-first) 접근 방식
- **백엔드 관점**: 스키마가 API의 계약이자 문서 역할
- **핵심 포인트**:
  - 스키마 정의 언어(SDL)로 타입 정의
  - 인트로스펙션 쿼리로 실시간 스키마 탐색
  - 강타입 시스템으로 컴파일 타임 검증

```graphql
# GraphQL 스키마 정의
type User {
  id: ID!
  name: String!
  email: String!
  posts: [Post!]!
}

type Post {
  id: ID!
  title: String!
  content: String!
  author: User!
  comments: [Comment!]!
}

type Query {
  user(id: ID!): User
  users: [User!]!
}
```

### 2-4. 4단계: 캐싱 전략

#### REST API
- **개념**: HTTP 캐싱과 CDN 활용에 최적화
- **백엔드 관점**: URL 기반 캐싱으로 구현 단순
- **핵심 포인트**:
  - HTTP 헤더(ETag, Cache-Control) 활용
  - 리버스 프록시(Nginx, CloudFlare) 캐싱
  - CDN 엣지 캐싱으로 글로벌 성능 향상

```javascript
// REST API 캐싱 예시
app.get('/api/users/:id', cache('1 hour'), (req, res) => {
  res.set('Cache-Control', 'public, max-age=3600');
  res.set('ETag', generateETag(userData));
  res.json(userData);
});
```

#### GraphQL
- **개념**: 쿼리별 맞춤 캐싱 필요
- **백엔드 관점**: 쿼리 복잡도로 인한 캐싱 전략 복잡
- **핵심 포인트**:
  - 쿼리 파싱과 정규화 필요
  - 필드 수준 캐싱(DataLoader 패턴)
  - Apollo Cache, Relay 등 전용 캐싱 솔루션

```javascript
// GraphQL DataLoader 패턴
const userLoader = new DataLoader(async (userIds) => {
  const users = await User.findByIds(userIds);
  return userIds.map(id => users.find(user => user.id === id));
});

// 리졸버에서 캐싱된 데이터 로딩
const resolvers = {
  Post: {
    author: (post) => userLoader.load(post.authorId)
  }
};
```

### 2-5. 5단계: 실시간 데이터 처리

#### REST API
- **개념**: 폴링, 웹소켓, Server-Sent Events 별도 구현
- **백엔드 관점**: 실시간 기능을 위한 추가 인프라 필요
- **핵심 포인트**:
  - HTTP 요청-응답 모델의 한계
  - 웹소켓 연결 관리 복잡도
  - 실시간 이벤트와 REST API 분리 설계

```javascript
// REST + WebSocket 조합
app.get('/api/posts', (req, res) => {
  res.json(posts);
});

// 별도 웹소켓 서버
io.on('connection', (socket) => {
  socket.on('subscribe-post', (postId) => {
    socket.join(`post-${postId}`);
  });
});
```

#### GraphQL
- **개념**: Subscription을 통한 내장 실시간 지원
- **백엔드 관점**: 단일 프로토콜로 쿼리/뮤테이션/구독 통합
- **핵심 포인트**:
  - 구독 스키마로 실시간 이벤트 정의
  - 웹소켓 위에서 GraphQL 프로토콜 동작
  - 통합된 개발 경험

```graphql
# GraphQL Subscription 스키마
type Subscription {
  postAdded: Post!
  commentAdded(postId: ID!): Comment!
}

# 클라이언트 구독
subscription {
  commentAdded(postId: "123") {
    id
    content
    author {
      name
    }
  }
}
```

---

## 3. 백엔드 개발자 관점에서의 시스템 특성

### 3-1. 개발 복잡도

#### REST API
- **장점**:
  - 직관적인 HTTP 기반 설계
  - 프레임워크 생태계 성숙
  - 개발자 학습 곡선 낮음
- **단점**:
  - 다수 엔드포인트 관리 부담
  - API 버전 관리 복잡성
  - 클라이언트별 요구사항 대응 어려움

#### GraphQL
- **장점**:
  - 단일 엔드포인트로 관리 단순화
  - 스키마 진화 용이성
  - 클라이언트 요구사항 유연 대응
- **단점**:
  - 초기 학습 곡선 높음
  - 쿼리 복잡도 관리 필요
  - 리졸버 설계 복잡성

### 3-2. 성능 특성

#### REST API
- **네트워크**: 여러 요청으로 인한 라운드트립 증가
- **캐싱**: HTTP 레이어 캐싱 최적화
- **로드밸런싱**: URL 기반 라우팅 용이

#### GraphQL
- **네트워크**: 단일 요청으로 네트워크 호출 최소화
- **데이터베이스**: N+1 문제 해결 필요 (DataLoader)
- **쿼리 복잡도**: 악의적 쿼리 방지 필요

### 3-3. 보안 고려사항

#### REST API
- **인증**: JWT, OAuth2 등 표준 프로토콜 활용
- **인가**: URL 기반 권한 제어
- **보안**: CORS, Rate Limiting 등 HTTP 레벨 보안

#### GraphQL
- **쿼리 복잡도**: 깊이 제한, 비용 분석 필요
- **인가**: 필드 레벨 권한 제어
- **데이터 노출**: 인트로스펙션 비활성화 고려

---

## 4. 실제 서비스 선택 기준

### 4-1. REST API 적합한 경우
- **단순한 CRUD 중심 애플리케이션**
- **캐싱이 중요한 공개 API**
- **마이크로서비스 간 통신**
- **레거시 시스템과의 호환성 필요**

### 4-2. GraphQL 적합한 경우
- **모바일 앱 등 네트워크 효율성 중요**
- **다양한 클라이언트 요구사항**
- **실시간 기능이 많은 애플리케이션**
- **빠른 프로토타이핑과 개발 속도 중요**

### 4-3. 하이브리드 접근
- **공개 API**: REST로 안정성과 캐싱 확보
- **내부 API**: GraphQL로 개발 효율성 증대
- **실시간 기능**: GraphQL Subscription 활용
- **파일 업로드**: REST 멀티파트 업로드 사용

---

## 5. 예상 면접 질문

### 5-1. 기술적 질문
1. GraphQL의 N+1 문제는 무엇이고 어떻게 해결하나요?
2. REST API의 HATEOAS 원칙을 설명하고 실제 구현 방법은?
3. GraphQL에서 쿼리 복잡도를 제한하는 방법들을 설명해주세요.

### 5-2. 설계 질문
1. 전자상거래 플랫폼을 설계할 때 REST와 GraphQL 중 어떤 것을 선택하고 그 이유는?
2. 모바일 앱과 웹 클라이언트를 모두 지원하는 API 설계 전략은?
3. 기존 REST API를 GraphQL로 마이그레이션하는 전략을 수립해보세요.

### 5-3. 성능 최적화 질문
1. GraphQL에서 데이터베이스 쿼리 최적화 방법은?
2. REST API의 응답 시간을 개선하기 위한 캐싱 전략은?
3. API Gateway에서 REST와 GraphQL을 함께 운영하는 방법은?

---

## 6. 핵심 요약

### 6-1. 주요 특징 비교

| 구분 | REST API | GraphQL |
|------|----------|---------|
| **엔드포인트** | 다수의 URL | 단일 /graphql |
| **데이터 페칭** | 고정된 응답 구조 | 동적 필드 선택 |
| **캐싱** | HTTP 캐싱 최적화 | 복잡한 캐싱 전략 |
| **실시간** | 별도 웹소켓 필요 | 내장 Subscription |
| **학습 곡선** | 낮음 | 높음 |
| **도구 생태계** | 성숙 | 빠르게 발전 중 |

### 6-2. 백엔드 개발자의 핵심 이해사항
- REST는 HTTP를 활용한 상태 없는 아키텍처로, 리소스 중심의 설계가 핵심이다
- GraphQL은 스키마 우선 접근으로, 타입 시스템과 리졸버 패턴 이해가 중요하다
- 각 접근 방식은 트레이드오프가 있으며, 프로젝트 요구사항에 따른 선택이 중요하다

### 6-3. 실무 적용 포인트
- REST는 캐싱과 HTTP 인프라 활용이 뛰어나 공개 API에 적합
- GraphQL은 클라이언트 최적화와 개발 속도 향상에 유리
- 대규모 서비스에서는 하이브리드 접근으로 각각의 장점 활용 권장