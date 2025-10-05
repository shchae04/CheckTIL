# 비즈니스 로직에서 애그리게잇과 애그리게잇 루트란?

## 0. 한눈에 보기(초간단)
- 애그리게잇(Aggregate): 강한 일관성을 함께 유지해야 하는 도메인 객체들의 묶음(경계).
- 애그리게잇 루트(Aggregate Root): 그 묶음의 대표자. 외부에서는 루트를 통해서만 내부 상태를 조회/변경한다.
- 트랜잭션 기본 단위: 한 트랜잭션은 보통 하나의 애그리게잇만 변경한다(충돌/락을 줄이고 일관성 보장).

## 1. 왜 필요한가?
- 비즈니스 불변식(invariant)을 어디에서, 어떤 단위로 강하게 보장할지 경계를 정하기 위해.
- DB 테이블 중심 설계는 변경 경계가 흐려짐 → 동시성/성능/복잡도 문제. 애그리게잇은 "함께 바뀌는 것"을 묶어 모델의 결정을 명확히 한다.

## 2. 핵심 정의
- 애그리게잇: 하나 이상의 엔티티/값 객체로 이루어진 변경 경계. 내부는 자유롭게 참조하지만, 외부는 루트만 참조한다.
- 애그리게잇 루트: 외부 공개 창구이자 불변식의 집행자. 컬렉션 변경(add/remove), 상태 전이(승인/취소) 같은 도메인 메서드를 제공한다.

## 3. 경계(모델링) 설정 가이드
1) 함께 바뀌는 것만 묶기
- 한 사용자의 명령으로 원자적으로 함께 검증/변경되어야 하는 데이터 묶음인가?
- 불변식을 검사하려면 항상 같이 있어야 하는가? (검증 시 다른 애그리게잇을 불러오지 않아도 되는가?)

2) 크기는 작게 유지하기
- 크기가 커질수록 락 경쟁/트랜잭션 시간이 길어지고 스케일이 안 나온다.
- 조회 요구가 다양한 경우, 쓰기 모델(애그리게잇)과 읽기 모델(view/프로젝션)을 분리(CQRS) 고려.

3) 참조는 ID로(느슨한 결합)
- 다른 애그리게잇을 필드로 직접 보유하지 말고, 그 ID만 보유한다.
- 필요 시 리포지토리로 로드하거나 도메인 서비스/애플리케이션 서비스 협력으로 처리.

## 4. 불변식과 규칙(실무 체크포인트)
- 변경은 루트를 통해서만: 내부 엔티티/컬렉션은 캡슐화. 외부에서 직접 수정 금지.
- 트랜잭션 경계 = 애그리게잇 경계: 한 트랜잭션에서 여러 애그리게잇을 동시에 변경하지 않기(필요 시 사가/프로세스 매니저로 분해).
- 외부 참조는 식별자(primitive/VO)로: ORM에서 양방향 연관관계 남발 금지.
- 동시성: 낙관적 락(@Version 등)으로 충돌 감지 → 재시도/사용자 조정.

## 5. 예시로 이해하기
1) 주문 도메인
- Order(루트) ── contains ── OrderLine(엔티티/VO)
- 불변식: "주문 상태가 PAID이면 라인은 수정 불가", "총액 = 라인 금액 합계".
- 외부: Payment, Shipment은 다른 애그리게잇. Order는 paymentId, shipmentId만 가진다.

2) 게시판 도메인
- Post(루트) ── contains ── Comment(엔티티)
- 불변식: "게시글이 잠김이면 댓글 추가 불가".
- 외부: Author는 User 애그리게잇. Post는 authorId만 가진다.

## 6. 간단 코드(개념 중심 Java 예시)
```java
public class Order { // Aggregate Root
    private Long id;
    private OrderStatus status = OrderStatus.CREATED;
    private final List<OrderLine> lines = new ArrayList<>();

    public void addLine(ProductId productId, int qty, Money price) {
        requireModifiable();
        lines.add(new OrderLine(productId, qty, price));
        validateTotal();
    }
    public void pay(PaymentId paymentId) {
        requireModifiable();
        if (lines.isEmpty()) throw new IllegalStateException("empty order");
        this.status = OrderStatus.PAID;
        // DomainEvent: OrderPaid(id, paymentId)
    }
    private void requireModifiable() {
        if (status == OrderStatus.PAID || status == OrderStatus.CANCELLED)
            throw new IllegalStateException("order not modifiable");
    }
    private void validateTotal() {
        Money total = lines.stream().map(OrderLine::subtotal).reduce(Money::plus).orElse(Money.ZERO);
        if (total.isNegative()) throw new IllegalStateException("invalid total");
    }
    public List<OrderLine> linesView() { return List.copyOf(lines); } // 불변 뷰
}
class OrderLine { /* 엔티티 or VO. equals/hashCode 기준에 따라 */ }
record ProductId(Long value) {}
record PaymentId(String value) {}
```
포인트: 컬렉션 수정은 루트 메서드로만. 외부는 lines 리스트를 직접 수정할 수 없고, 도메인 규칙은 루트에서 보장한다.

## 7. 트랜잭션/일관성 전략
- 단일 애그리게잇 내에서는 강한 일관성(동일 트랜잭션)으로 불변식 보장.
- 여러 애그리게잇의 변경은 도메인 이벤트 + 사가/프로세스 매니저로 최종 일관성(Eventual Consistency) 설계.
- 예: Order.PAID → PaymentConfirmed 이벤트 발행 → Shipment 애플리케이션 서비스가 수신해 Shipment 생성.

## 8. JPA/ORM 실무 팁
- 연관 매핑
  - 다른 애그리게잇은 @ManyToOne 지양하고 식별자 값(예: Long paymentId)로 보유. 조회 최적화는 별도 조회/리드 모델로 해결.
  - 내부 엔티티 컬렉션은 루트에서 Cascade.ALL + orphanRemoval=true를 사용하되, 외부 참조가 없도록 캡슐화.
- fetch 전략: 기본 LAZY, N+1은 케이스별 페치 조인/전용 조회 쿼리로 해결(읽기 모델 분리 고려).
- @Version으로 낙관적 락 적용. 충돌 시 재시도 로직/사용자 피드백 설계.
- 리포지토리는 "애그리게잇 단위"로 설계: save(Order), findById(OrderId).

## 9. 안티패턴
- 거대한 애그리게잇: "편해서 한 군데에 다 묶기"는 락 지옥/성능 급락.
- 양방향 연관 남발: 애그리게잇 간 직접 객체 참조/그래프 순환으로 로딩 폭발.
- 빈약한 도메인 모델: 모든 규칙이 서비스에만 있고 모델은 DTO처럼 비어 있음.
- 다중 루트 동시 변경: 한 트랜잭션에서 여러 루트를 함께 update.

## 10. 실전 체크리스트
- 이 변경이 원자적으로 함께 일어나야 하는가? → Yes면 같은 애그리게잇.
- 불변식을 루트 메서드에서 검증하고 있는가?
- 다른 애그리게잇은 ID만 참조하는가?
- 한 트랜잭션에 하나의 애그리게잇만 변경하는가?
- 낙관적 락/충돌 처리 전략이 있는가?
- 읽기 최적화는 리드 모델/전용 쿼리로 분리했는가?

## 11. 면접 한 줄 답변 예시
- 애그리게잇은 강한 일관성 경계이고, 애그리게잇 루트는 그 경계의 유일한 진입점입니다. 외부는 루트를 통해서만 변경하며, 한 트랜잭션에서 하나의 애그리게잇만 변경하는 것을 원칙으로 합니다. 다른 애그리게잇은 ID로만 느슨하게 연결하고, 교차 변경은 이벤트/사가로 최종 일관성을 맞춥니다.

## 12. 참고
- 개요: ../CS/SystemArchitecture/ddd_architecture.md
- Evans, Domain-Driven Design; Vernon, Implementing DDD


## 13. 바운디드 컨텍스트란? (범위 포함 한눈에)
- 정의: 특정 도메인 모델과 유비쿼터스 언어가 일관되게 적용되는 경계. 같은 용어도 컨텍스트가 다르면 의미/모델이 달라질 수 있다.
- 목적: 변경의 파급 범위를 제한하고 팀/코드/데이터의 경계를 정렬해 민첩하게 진화하도록 돕는다.
- 스코프(범위):
  - 비즈니스 능력(Capability) 단위로 끊는다. 예: 주문(Ordering), 결제(Payment), 배송(Shipping).
  - 모델/언어/일관성 수준/트랜잭션 경계/데이터 보유권이 컨텍스트 내부에서 통일된다.

## 14. 바운디드 컨텍스트 범위 설정 가이드(실무)
1) 언어/의미가 다르면 분리
- 예: "고객"이 청구 컨텍스트에선 계정/청구 주소 중심, 마케팅 컨텍스트에선 세그먼트/캠페인 반응 중심 → 분리.

2) 일관성 수준과 트랜잭션 요구로 자르기
- 강한 일관성이 필요한 규칙 묶음은 같은 컨텍스트로. 최종 일관성 허용이면 다른 컨텍스트로 분리 후 이벤트 연동.

3) 데이터 보유권과 규제/보안 경계 고려
- PII/결제 정보 등 규제가 다른 데이터는 별도 컨텍스트로 격리(예: Payment).

4) 팀 구조/배포 독립성
- 한 팀이 독립적으로 배포/스케일/의사결정 가능한 경계로 설계(Conway’s Law 정렬).

5) 인터페이스 우선 설계
- 컨텍스트 간 통신 계약(API/이벤트/ACL)을 먼저 정의. 내부 모델은 외부에 누출하지 않는다.

## 15. 컨텍스트 매핑과 통합 패턴
- 고객/공급자(Supplier-Consumer): 상류 컨텍스트의 변경이 하류에 영향. 계약/버전 전략 필요.
- 파트너십(Partnership): 양측이 공동으로 스키마/계약을 관리.
- Anti-Corruption Layer(ACL): 레거시 모델의 개념/용어를 번역/격리해 코어 모델 오염 방지.
- 이벤트 발행/구독: 상태 변경을 도메인 이벤트로 통지해 최종 일관성으로 협력.
- 요청/응답 API: 조회/명령을 명시적 계약으로 호출.

## 16. 예시: 커머스 컨텍스트와 애그리게잇 배치
- Ordering BC
  - 애그리게잇: Order(루트) -> OrderLine
  - 규칙: 주문은 PAID 이후 수정 불가, 총액 = 라인 합.
- Payment BC
  - 애그리게잇: Payment(루트)
  - 규칙: 승인/취소/환불 상태 전이, 결제 수단 토큰 보유.
- Shipping BC
  - 애그리게잇: Shipment(루트)
  - 규칙: 할당/픽업/배송완료 상태 전이.
- 연동 흐름(최종 일관성):
  - Order.PAID 이벤트 → Shipping이 수신해 Shipment 생성.
  - Payment에서 결제 실패 이벤트 → Ordering이 수신해 Order를 CANCELLED로 전이.
- 경계 규칙: Order는 paymentId, shipmentId만 보유. 서로의 내부 엔티티를 직접 참조하지 않음.

## 17. 실전 체크리스트(컨텍스트 관점)
- 우리 팀/시스템의 바운디드 컨텍스트 목록과 책임(캡ability)이 문서화됐는가?
- 컨텍스트마다 유비쿼터스 언어와 핵심 용어 정의가 있는가?
- 컨텍스트 간 관계/통합 방식(Context Map: 이벤트, API, ACL)이 명시됐는가?
- 각 컨텍스트 내부의 애그리게잇 경계와 불변식이 코드로 표현됐는가?
- 강한 일관성은 컨텍스트 내부, 컨텍스트 간은 최종 일관성으로 설계했는가?

## 18. 안티패턴(컨텍스트 관점)
- 엔터프라이즈 공용 모델(빅 볼 오브 머드): 모든 것을 하나의 거대 모델/DB로 공유.
- 컨텍스트 누수: 한 컨텍스트의 내부 엔티티가 다른 컨텍스트에 직접 노출/참조됨.
- 암묵 통합: 문서화되지 않은 DB 조회/공유 스키마로 은밀히 결합.

## 19. 면접 한 줄 답변(컨텍스트 포함)
- 애그리게잇은 강한 일관성 경계, 루트는 유일한 진입점입니다. 바운디드 컨텍스트는 모델/언어가 일관되게 적용되는 상위 경계로, 팀/배포/데이터 보유권과 정렬합니다. 컨텍스트 내부는 강한 일관성, 컨텍스트 간 협력은 이벤트/ACL/API로 최종 일관성을 설계합니다.

## 20. 추가 참고
- DDD 전체 구조/전략적 설계: ../CS/SystemArchitecture/ddd_architecture.md
- Context Mapping 참고: Evans, Vernon, Team Topologies