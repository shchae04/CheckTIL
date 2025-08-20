# DDD 아키텍처란?

도메인 주도 설계(DDD, Domain-Driven Design)는 소프트웨어가 해결하려는 "도메인" 자체를 중심에 두고 모델링하며, 코드 구조와 팀의 협업 방식까지 도메인 언어에 맞춰 정렬하는 접근법입니다. DDD 아키텍처는 이 철학을 뒷받침하는 구조적 원칙과 전술 패턴들의 조합을 의미합니다.

목표는 다음과 같습니다:
- 복잡한 비즈니스 규칙과 불변식을 코드로 명확히 표현한다.
- 변경이 일어날 때 영향 범위를 바운디드 컨텍스트로 한정해 민첩하게 진화한다.
- 기술(프레임워크/DB)보다 도메인 모델이 중심이 되게 한다.

---

## 핵심 개념(전략적 설계)

- 유비쿼터스 언어(Ubiquitous Language)
  - 도메인 전문가와 개발자가 공유하는 공통 언어를 정의하고, 이 언어로 코드(클래스/메서드/모듈 이름)를 일치시킨다.
- 바운디드 컨텍스트(Bounded Context)
  - 특정 모델이 유효하게 통용되는 경계. 동일한 용어라도 컨텍스트가 다르면 의미가 달라질 수 있다.
  - 컨텍스트 간 통신은 명시적인 통합 패턴(ACL, 이벤트, API 계약)으로 연결한다.
- 컨텍스트 매핑(Context Mapping)
  - 컨텍스트 간 관계를 정의: 파트너십, 고객/공급자, Anti-Corruption Layer 등.
- 서브도메인(Subdomain)
  - 코어/지원/일반 서브도메인으로 분류하고, 코어에 역량을 집중한다.

---

## 전술 패턴(코드 레벨 구성요소)

- 엔티티(Entity)
  - 식별자(ID)로 구분되는 변경 가능한 도메인 객체. 불변식은 애그리게잇 규칙을 따른다.
- 값 객체(Value Object)
  - 식별자 없이 값으로 동등성이 결정되는 불변 객체. Money, Email, Period 등.
- 애그리게잇(Aggregate)와 애그리게잇 루트
  - 강한 일관성 경계. 외부에서는 루트를 통해서만 내부 상태를 변경한다.
  - 트랜잭션/락의 기본 단위. 크기를 작게 유지하고 불변식을 루트에서 보장.
- 도메인 이벤트(Domain Event)
  - 도메인에서 의미 있는 사건을 불변 객체로 캡처. 명명은 과거 시제로.
- 리포지토리(Repository)
  - 애그리게잇 단위의 영속성 추상화. 인터페이스는 도메인 계층에, 구현은 인프라 계층에.
- 도메인 서비스(Domain Service)
  - 특정 엔티티/값 객체 하나에 귀속되지 않는 도메인 규칙(연산)을 캡슐화.

---

## 레이어드 아키텍처와 의존 규칙

- Presentation(UI/API) → Application → Domain → Infrastructure
- 의존성 흐름은 위에서 아래로만. Domain은 어떤 기술에도 의존하지 않도록 유지(가능하면 순수 Java/Kotlin 코드).
- Application 레이어
  - 유스케이스 조합/흐름 제어, 트랜잭션 경계 설정, 권한/검증 조정, 외부 시스템과의 오케스트레이션.
- Domain 레이어
  - 엔티티/값 객체/도메인 서비스/도메인 이벤트/리포지토리 인터페이스.
- Infrastructure 레이어
  - 영속성 구현(JPA, MyBatis), 메시징, 외부 API 클라이언트, 설정/기술 상세.

Tip: 의존성 역전(DIP)을 적용해 도메인 → 인프라 의존을 인터페이스로 끊고, 구현체 바인딩은 상위 레이어나 구성 루트에서 한다.

---

## 헥사고날(육각형)/클린 아키텍처와의 관계

- 포트와 어댑터(헥사고날):
  - 포트(도메인 관점의 인터페이스)를 통해 인바운드(사용자/메시지)와 아웃바운드(리포지토리/외부 API)를 추상화.
  - 어댑터가 포트를 구현하여 기술 세부를 격리.
- 클린 아키텍처:
  - 엔티티/유즈케이스(Core)와 인터페이스 어댑터/프레임워크로 동심원. 의존성은 안쪽으로만.
- DDD는 모델/경계/언어에 초점을, 헥사고날/클린은 의존성 제어에 초점을 둔다. 실무에서는 함께 조합해 사용.

---

## CQRS와 이벤트 소싱(선택)

- CQRS(Command Query Responsibility Segregation)
  - 쓰기 모델(도메인 불변식 강함)과 읽기 모델(조회 최적화)을 분리. 스케일/성능/복잡도 트레이드오프.
- 이벤트 소싱(Event Sourcing)
  - 상태를 스냅샷이 아닌 이벤트 시퀀스로 저장. 이벤트 재생으로 현재 상태 복원.
  - 장점: 감사 추적, 과거 재현, 강한 도메인 히스토리. 단점: 복잡성, 마이그레이션/버전 관리 비용.

---

## 트랜잭션과 일관성

- 애그리게잇은 단일 트랜잭션의 기본 단위. 한 트랜잭션에 여러 애그리게잇을 묶는 것을 피하라.
- 사가/프로세스 매니저로 분산 트랜잭션을 대체(최종 일관성). 도메인 이벤트로 협력.
- 동시성: 낙관적 락 버전 필드를 사용, 충돌 시 재시도/사용자 조정.

---

## Spring 관점의 구현 가이드

- 패키지 기준
  - by layer는 의존 역전이 약해지기 쉬움. by feature(by bounded context / aggregate) 패키징을 권장.
  - 예: com.example.order(도메인), com.example.payment(도메인) 등으로 수직 슬라이스.
- 애그리게잇 설계
  - 엔티티 컬렉션 변경은 루트에서 메서드로 노출(add/remove)하고 컬렉션을 불변 뷰로 제공.
  - ID는 루트에서 생성/부여. 값 객체는 불변.
- 리포지토리 인터페이스는 도메인에, 구현은 인프라에 배치.
- 트랜잭션 경계는 Application 서비스에(@Transactional). 도메인 모델은 프레임워크 어노테이션 최소화.
- 도메인 이벤트
  - 순수 객체(Record/클래스)로 발행 → Application 레이어에서 메시징/비동기 발행으로 연결.

### 간단 예시(Java, Spring)

```java
// domain (ICS2 ENS)
public enum EnsStatus { DRAFT, SUBMITTED, AMENDED, INVALIDATED }

public record CountryCode(String value) {
    public CountryCode {
        if (value == null || value.length() != 2) throw new IllegalArgumentException("ISO-2 country");
    }
}

public record Party(String name, String addressLine, CountryCode countryCode, String eori) {
    public Party {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name required");
    }
}

public record GoodsItem(String hsCode, double grossWeightKg, String description) {
    public GoodsItem {
        if (hsCode == null || hsCode.length() < 6) throw new IllegalArgumentException("hsCode>=6");
        if (grossWeightKg <= 0) throw new IllegalArgumentException("weight>0");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("desc required");
    }
}

public final class EnsDeclaration {
    private final String temporaryId; // 내부 식별자(제출 전)
    private String mrn;               // 제출 후 부여됨
    private EnsStatus status = EnsStatus.DRAFT;

    private String transportDocumentId; // AWB/HAWB/BL
    private CountryCode arrivalCountry; // 예: NL
    private String entryOffice;         // 입경 세관 사무소 코드
    private String modeOfTransport;     // 예: 4=항공
    private Instant expectedArrivalTime; // UTC

    private Party consignor;
    private Party consignee;
    private final List<GoodsItem> goodsItems = new ArrayList<>();

    public EnsDeclaration(String temporaryId) { this.temporaryId = Objects.requireNonNull(temporaryId); }

    public void setHeader(String transportDocumentId, CountryCode arrivalCountry, String entryOffice,
                          String modeOfTransport, Instant expectedArrivalTime, Party consignor, Party consignee) {
        if (transportDocumentId == null || transportDocumentId.isBlank()) throw new IllegalArgumentException("docId");
        this.transportDocumentId = transportDocumentId;
        this.arrivalCountry = arrivalCountry;
        this.entryOffice = entryOffice;
        this.modeOfTransport = modeOfTransport;
        this.expectedArrivalTime = expectedArrivalTime;
        this.consignor = consignor;
        this.consignee = consignee;
    }

    public void addItem(GoodsItem item) { this.goodsItems.add(Objects.requireNonNull(item)); }

    public void submit(String assignedMrn) {
        if (status != EnsStatus.DRAFT) throw new IllegalStateException("only DRAFT can submit");
        if (goodsItems.isEmpty()) throw new IllegalStateException("at least 1 item");
        this.mrn = Objects.requireNonNull(assignedMrn);
        this.status = EnsStatus.SUBMITTED;
        // DomainEvent: new EnsSubmitted(mrn)
    }

    public void amend(List<GoodsItem> updatedItems, String reasonCode) {
        if (status != EnsStatus.SUBMITTED && status != EnsStatus.AMENDED)
            throw new IllegalStateException("can amend only after submit");
        if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reason required");
        this.goodsItems.clear();
        this.goodsItems.addAll(updatedItems);
        this.status = EnsStatus.AMENDED;
        // DomainEvent: new EnsAmended(mrn, reasonCode)
    }

    public void invalidate(String reasonCode) {
        if (status == EnsStatus.INVALIDATED) throw new IllegalStateException("already invalidated");
        if (reasonCode == null || reasonCode.isBlank()) throw new IllegalArgumentException("reason required");
        this.status = EnsStatus.INVALIDATED;
        // DomainEvent: new EnsInvalidated(mrn, reasonCode)
    }

    public Optional<String> mrn() { return Optional.ofNullable(mrn); }
    public EnsStatus status() { return status; }
}

public interface EnsDeclarationRepository {
    Optional<EnsDeclaration> findByMrn(String mrn);
    Optional<EnsDeclaration> findByTemporaryId(String tempId);
    void save(EnsDeclaration decl);
}
```

```java
// application (ICS2 ENS)
@Service
public class EnsApplicationService {
    private final EnsDeclarationRepository declarations;
    private final CustomsGateway customsGateway; // 아웃바운드 포트(외부 세관 시스템 호출)

    public EnsApplicationService(EnsDeclarationRepository declarations, CustomsGateway customsGateway) {
        this.declarations = declarations;
        this.customsGateway = customsGateway;
    }

    @Transactional
    public String submitDeclaration(String tempId) {
        EnsDeclaration decl = declarations.findByTemporaryId(tempId).orElseThrow();
        String assignedMrn = customsGateway.submit(decl); // 외부 시스템이 MRN 부여
        decl.submit(assignedMrn);
        declarations.save(decl);
        return assignedMrn;
    }

    @Transactional
    public void amendDeclaration(String mrn, List<GoodsItem> updated, String reasonCode) {
        EnsDeclaration decl = declarations.findByMrn(mrn).orElseThrow();
        customsGateway.amend(mrn, updated, reasonCode);
        decl.amend(updated, reasonCode);
        declarations.save(decl);
    }

    @Transactional
    public void invalidateDeclaration(String mrn, String reasonCode) {
        EnsDeclaration decl = declarations.findByMrn(mrn).orElseThrow();
        customsGateway.invalidate(mrn, reasonCode);
        decl.invalidate(reasonCode);
        declarations.save(decl);
    }
}

// 아웃바운드 포트(헥사고날 Port)
public interface CustomsGateway {
    String submit(EnsDeclaration declaration);
    void amend(String mrn, List<GoodsItem> updated, String reasonCode);
    void invalidate(String mrn, String reasonCode);
}
```

```java
// infrastructure (ICS2 ENS)
@Repository
class JpaEnsDeclarationRepository implements EnsDeclarationRepository {
    private final SpringDataEnsJpa repo;

    @Override public Optional<EnsDeclaration> findByMrn(String mrn) { return repo.findByMrn(mrn).map(JpaEns::toDomain); }
    @Override public Optional<EnsDeclaration> findByTemporaryId(String tempId) { return repo.findByTemporaryId(tempId).map(JpaEns::toDomain); }
    @Override public void save(EnsDeclaration decl) { repo.save(JpaEns.of(decl)); }
}

// 아웃바운드 어댑터 예시(포트 구현)
@Component
class RestCustomsGateway implements CustomsGateway {
    private final WebClient webClient;
    RestCustomsGateway(WebClient.Builder builder) { this.webClient = builder.baseUrl("https://customs.eu").build(); }
    @Override public String submit(EnsDeclaration declaration) { /* call ICS2 API */ return "MRN123"; }
    @Override public void amend(String mrn, List<GoodsItem> updated, String reasonCode) { /* call ICS2 API */ }
    @Override public void invalidate(String mrn, String reasonCode) { /* call ICS2 API */ }
}
```

---

## 모듈 경계와 팀 구조

- 바운디드 컨텍스트를 팀 경계와 정렬(Conway's Law). 각 컨텍스트는 독립적으로 배포/스케일링.
- 컨텍스트 간 통합은 명시적 계약(API/이벤트/ACL)으로 관리.

---

## 안티 패턴 주의

- 데이터 주도 설계: 엔티티를 DB 테이블 구조에 종속적으로 설계.
- 거대 애그리게잇: 한 트랜잭션에 너무 많은 불변식을 묶어 락/성능 문제 유발.
- 빈약한 도메인 모델: 모든 비즈니스 로직이 서비스 계층에만 존재.
- 과도한 추상화: 초기부터 CQRS/ES 도입으로 복잡성만 증가.

---

## 체크리스트

- 바운디드 컨텍스트와 컨텍스트 매핑이 문서화되어 있는가?
- 코어 서브도메인이 무엇이며, 팀/백로그가 여기에 집중되는가?
- 애그리게잇 경계와 불변식이 코드로 표현되어 있는가?
- 리포지토리 인터페이스가 도메인에 있고, 인프라 세부 구현이 분리되어 있는가?
- 트랜잭션 경계가 Application 레이어에 정의되어 있는가?
- 도메인 이벤트 명세와 통합 패턴(ACL/이벤트/API 계약)이 정해졌는가?

---

## 요약

- DDD 아키텍처는 도메인 모델과 경계를 중심으로 설계를 조직하고, 레이어/포트-어댑터로 기술 의존을 격리한다.
- 애그리게잇을 단위로 일관성을 관리하고, 변경 가능성을 바운디드 컨텍스트 내부로 국한시켜 민첩하게 진화한다.
- Spring 등 실무에서는 by-feature 패키징, 트랜잭션은 Application, 도메인은 순수 모델을 유지하는 것이 핵심이다.
