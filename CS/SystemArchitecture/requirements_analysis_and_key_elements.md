# 요구사항 분석과 핵심 요소 식별: 백엔드 개발자 관점에서의 5단계 접근

## 1. 한 줄 정의
- 요구사항 분석과 핵심 요소 식별은 시스템 설계 전 문제의 본질을 파악하고 구현해야 할 필수 기능과 제약조건을 명확히 정의하는 과정이다. 백엔드 관점에서는 비즈니스 요구사항을 기술적 스펙으로 변환하고 우선순위를 결정하는 구조화된 분석 프로세스로 이해할 수 있다.

---

## 2. 요구사항 분석 5단계 프로세스

### 2-1. 1단계: 비즈니스 요구사항 수집(Business Requirement Gathering)
- **개념**: 이해관계자로부터 해결하고자 하는 문제와 비즈니스 목표를 수집
- **백엔드 관점**: API 명세 작성 전 엔드포인트가 제공해야 할 가치를 정의하는 단계
- **핵심 포인트**:
  - 정량적 목표 설정 (MAU, TPS, 응답시간 등)
  - 사용자 스토리(User Story) 형식으로 요구사항 문서화
  - 명확하지 않은 요구사항은 질문을 통해 구체화

```markdown
# 요구사항 수집 예시
AS-IS: 사용자가 대시보드에서 데이터를 보는데 5초 이상 소요
TO-BE: 실시간으로 업데이트되는 대시보드 제공 (1초 이내)

User Story:
- As a 백엔드 개발자
- I want to 캐싱 시스템을 구현하여
- So that 대시보드 응답 시간을 1초 이내로 단축할 수 있다
```

### 2-2. 2단계: 기능적 요구사항 vs 비기능적 요구사항 분류
- **개념**: 수집한 요구사항을 기능(Functional)과 품질(Non-Functional) 요구사항으로 분리
- **백엔드 관점**: CRUD 연산과 시스템 품질 속성을 구분하는 프로세스
- **핵심 포인트**:
  - 기능적 요구사항: 시스템이 "무엇을" 해야 하는가
  - 비기능적 요구사항: 시스템이 "얼마나 잘" 해야 하는가
  - 비기능적 요구사항이 아키텍처 결정에 더 큰 영향

```python
# 기능적 요구사항 예시
functional_requirements = {
    "user_registration": "이메일과 비밀번호로 회원가입",
    "user_login": "JWT 토큰 기반 인증",
    "post_creation": "게시글 작성 및 수정",
    "comment_system": "댓글 작성 및 답글 기능"
}

# 비기능적 요구사항 예시
non_functional_requirements = {
    "performance": "API 응답시간 < 200ms (P95)",
    "scalability": "동시접속자 10,000명 처리",
    "availability": "99.9% uptime (연간 다운타임 < 8.76시간)",
    "security": "OWASP Top 10 보안 취약점 대응",
    "maintainability": "코드 커버리지 > 80%"
}
```

### 2-3. 3단계: 핵심 엔티티 및 도메인 모델 식별
- **개념**: 시스템의 핵심 비즈니스 개체(Entity)와 그들 간의 관계를 파악
- **백엔드 관점**: ERD 설계 전 도메인 주도 설계(DDD)의 애그리게잇 루트를 찾는 과정
- **핵심 포인트**:
  - 명사 추출법: 요구사항 문서에서 반복되는 명사가 엔티티 후보
  - 관계 정의: 1:N, N:M 등 엔티티 간 관계 매핑
  - 바운디드 컨텍스트로 도메인 경계 구분

```python
# 도메인 모델 식별 예시 (전자상거래)
class Order:  # 핵심 엔티티 (Aggregate Root)
    order_id: str
    user_id: str  # User 엔티티와의 관계
    order_items: List[OrderItem]  # 1:N 관계
    total_amount: Decimal
    status: OrderStatus
    created_at: datetime

class OrderItem:  # Value Object
    product_id: str  # Product 엔티티와의 관계
    quantity: int
    price: Decimal

# 핵심 비즈니스 규칙
def validate_order(order: Order) -> bool:
    # 재고 확인, 결제 검증 등
    return order.total_amount > 0 and len(order.order_items) > 0
```

### 2-4. 4단계: 제약조건 및 트레이드오프 분석
- **개념**: 시스템 설계 시 고려해야 할 기술적/비즈니스적 제약사항 파악
- **백엔드 관점**: CAP 정리, 일관성 vs 가용성 등의 트레이드오프 결정
- **핵심 포인트**:
  - 기술 스택 제약: 기존 인프라, 팀 역량
  - 비용 제약: 클라우드 예산, 개발 기간
  - 규정 준수: GDPR, 개인정보보호법 등

```yaml
# 제약조건 분석 예시
constraints:
  technical:
    - database: PostgreSQL (기존 시스템과 통일)
    - language: Java 17 (팀 주력 언어)
    - infrastructure: AWS (기존 계약 유지)

  business:
    - budget: $10,000/month (클라우드 비용)
    - timeline: 3개월 (MVP 출시)
    - compliance: GDPR, ISO 27001

  tradeoffs:
    - consistency_vs_availability:
        choice: "Eventual Consistency"
        reason: "높은 가용성이 정합성보다 중요 (소셜 미디어)"

    - sql_vs_nosql:
        choice: "PostgreSQL + Redis"
        reason: "트랜잭션 보장 + 읽기 성능 향상"
```

### 2-5. 5단계: 우선순위 결정 및 MVP 정의
- **개념**: 식별된 요구사항에 우선순위를 부여하고 최소 기능 제품(MVP) 범위 결정
- **백엔드 관점**: MoSCoW 기법으로 API 개발 순서 결정
- **핵심 포인트**:
  - Must Have: 없으면 시스템이 작동하지 않는 필수 기능
  - Should Have: 중요하지만 1차 출시에 필수는 아닌 기능
  - Could Have: 있으면 좋지만 우선순위 낮음
  - Won't Have: 현재 범위에서 제외

```python
# 우선순위 매트릭스 (Impact vs Effort)
priority_matrix = {
    "high_impact_low_effort": [  # Quick Wins - 최우선 구현
        "JWT 기반 인증 시스템",
        "Redis 캐싱 도입",
        "데이터베이스 인덱스 최적화"
    ],
    "high_impact_high_effort": [  # Major Projects - MVP 범위
        "마이크로서비스 아키텍처 전환",
        "검색 엔진 (Elasticsearch) 통합",
        "실시간 알림 시스템 (WebSocket)"
    ],
    "low_impact_low_effort": [  # Fill-Ins - 여유 있을 때
        "로그 포맷 개선",
        "API 응답 메시지 다국어 지원"
    ],
    "low_impact_high_effort": [  # Thankless Tasks - 제외
        "레거시 시스템 전체 재작성"
    ]
}

# MVP 정의
mvp_features = [
    "사용자 인증/인가 (JWT)",
    "게시글 CRUD",
    "기본 검색 기능",
    "Redis 캐싱"
]
```

---

## 3. 백엔드 개발자 관점에서의 핵심 기법

### 3-1. API First Design
- **요구사항을 먼저 API 스펙으로 변환**: OpenAPI(Swagger) 명세서 작성
- **Contract-Driven Development**: 프론트엔드와 API 계약 먼저 합의
- **Mock Server**: 실제 구현 전 API 스펙 검증

### 3-2. Event Storming
- **도메인 이벤트 중심 분석**: "무엇이 일어나는가"에 집중
- **Command-Event-Actor 매핑**: 비즈니스 플로우 시각화
- **바운디드 컨텍스트 도출**: 마이크로서비스 경계 설정

### 3-3. 데이터 흐름 분석
- **Read vs Write 비율**: CQRS 패턴 적용 여부 결정
- **데이터 일관성 요구사항**: 동기 vs 비동기 처리
- **데이터 생명주기**: 보관 기간, 아카이빙 정책

---

## 4. 실제 프로젝트 적용 예시

### 4-1. 사례: 실시간 채팅 서비스 요구사항 분석

```markdown
## 1단계: 비즈니스 요구사항
- 사용자가 1:1 및 그룹 채팅을 할 수 있어야 함
- 메시지는 실시간으로 전달되어야 함
- 과거 메시지 히스토리 조회 가능
- 목표: DAU 100,000명, 동시접속 10,000명

## 2단계: 요구사항 분류
### 기능적 요구사항
- 사용자 인증 및 친구 관리
- 채팅방 생성/조회/삭제
- 메시지 전송/수신
- 읽음 확인 표시
- 이미지/파일 전송

### 비기능적 요구사항
- 메시지 전달 지연 < 100ms
- 99.95% 가용성
- 메시지 전달 보장 (최소 한 번)
- 확장 가능한 아키텍처

## 3단계: 핵심 엔티티
- User (사용자)
- ChatRoom (채팅방)
- Message (메시지)
- Participant (참여자)

## 4단계: 제약조건
- WebSocket 연결 수 제한: 서버당 10,000개
- 메시지 크기 제한: 10KB
- 파일 업로드 제한: 10MB

## 5단계: MVP
- Phase 1: 1:1 채팅, 텍스트 메시지만
- Phase 2: 그룹 채팅, 읽음 확인
- Phase 3: 파일 전송, 알림 기능
```

### 4-2. 기술 스택 결정 매트릭스

```python
technology_decision_matrix = {
    "real_time_communication": {
        "options": ["WebSocket", "Server-Sent Events", "Long Polling"],
        "chosen": "WebSocket",
        "reason": "양방향 통신 필요, 낮은 지연 시간"
    },
    "message_broker": {
        "options": ["RabbitMQ", "Kafka", "Redis Pub/Sub"],
        "chosen": "Kafka",
        "reason": "높은 처리량, 메시지 순서 보장, 확장성"
    },
    "database": {
        "options": ["PostgreSQL", "MongoDB", "Cassandra"],
        "chosen": "MongoDB + PostgreSQL",
        "reason": "MongoDB(메시지 저장), PostgreSQL(사용자/채팅방 메타데이터)"
    },
    "caching": {
        "options": ["Redis", "Memcached"],
        "chosen": "Redis",
        "reason": "Pub/Sub 기능 활용, 온라인 사용자 상태 관리"
    }
}
```

---

## 5. 예상 면접 질문

### 5-1. 분석 프로세스 질문
1. 요구사항이 모호할 때 어떻게 명확히 하시나요?
2. 기능적 요구사항과 비기능적 요구사항의 차이는 무엇인가요?
3. MVP 범위를 정할 때 어떤 기준을 사용하시나요?

### 5-2. 시스템 설계 질문
1. 새로운 기능 요구사항이 들어왔을 때 기존 시스템에 미치는 영향을 어떻게 분석하시나요?
2. 일관성(Consistency)과 가용성(Availability) 중 하나를 선택해야 한다면 어떻게 결정하시나요?
3. 마이크로서비스로 전환할 때 도메인 경계를 어떻게 설정하시나요?

### 5-3. 실무 적용 질문
1. 이해관계자와 기술적 트레이드오프를 논의할 때 어떻게 설명하시나요?
2. 레거시 시스템의 요구사항을 분석할 때 주의해야 할 점은?
3. 보안 요구사항을 어떻게 식별하고 우선순위를 매기시나요?

---

## 6. 핵심 요약

### 6-1. 주요 원칙
- **명확성 우선**: 모호한 요구사항은 가정하지 말고 질문
- **측정 가능한 목표**: "빠르게"가 아니라 "200ms 이내"
- **우선순위 기반 개발**: 모든 것을 한 번에 구현하려 하지 말 것

### 6-2. 백엔드 개발자의 핵심 이해사항
- 비즈니스 요구사항을 기술 스펙으로 변환하는 능력이 시니어의 핵심 역량
- 비기능적 요구사항이 아키텍처 결정의 80%를 차지
- 트레이드오프는 정답이 없으며, 컨텍스트에 따라 다름

### 6-3. 실무 적용 포인트
- API First Design으로 초기에 인터페이스 합의
- Event Storming으로 도메인 모델 시각화
- MoSCoW 기법으로 우선순위 결정하여 점진적 개발

### 6-4. 자주 하는 실수
- **너무 이른 최적화**: 성능 요구사항 없이 복잡한 캐싱 구조 도입
- **과도한 일반화**: 현재 필요 없는 확장성을 위해 복잡도 증가
- **요구사항 검증 누락**: 구현 후 "이게 아닌데요" 듣기
- **비기능적 요구사항 간과**: 기능은 완성했지만 성능/보안 문제 발생

---

## 7. 실전 체크리스트

### 7-1. 요구사항 분석 완료 기준
- [ ] 모든 이해관계자의 요구사항이 문서화되었는가?
- [ ] 정량적 목표(TPS, 응답시간 등)가 명시되었는가?
- [ ] 기능적/비기능적 요구사항이 분류되었는가?
- [ ] 핵심 엔티티와 관계가 정의되었는가?
- [ ] 제약조건과 트레이드오프가 문서화되었는가?
- [ ] MVP 범위가 명확히 정의되었는가?

### 7-2. 기술 스택 결정 체크리스트
- [ ] 선택한 기술이 비기능적 요구사항을 만족하는가?
- [ ] 팀이 해당 기술 스택에 숙련되어 있는가?
- [ ] 운영/유지보수 비용이 예산 내인가?
- [ ] 확장 가능한 아키텍처인가?
- [ ] 보안 요구사항을 충족하는가?
