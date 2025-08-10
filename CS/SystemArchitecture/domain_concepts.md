# 도메인이란? (소프트웨어 도메인 vs 인터넷 도메인)

업무/문제의 맥락에 따라 "도메인"은 두 가지로 주로 쓰입니다. 혼동을 피하기 위해 각각을 간단히 정리합니다.

---

## 1) 소프트웨어/DDD에서의 도메인
- 정의: 소프트웨어가 해결하려는 비즈니스 문제 영역(Problem Space). 예: 전자상거래, 결제, 배송, 정산 등.
- 도메인 모델: 비즈니스 개념, 규칙, 제약을 코드로 명시적으로 표현한 모델. 엔티티(Entity), 값 객체(Value Object), 도메인 서비스(Domain Service), 리포지토리(Repository) 등으로 구성됩니다.
- 서브도메인: 큰 비즈니스 안의 하위 영역.
  - 코어(Core): 차별화되는 핵심 경쟁력.
  - 서포팅(Supporting): 핵심을 보조하는 기능.
  - 제너릭(Generic): 범용/공통(예: 결제 게이트웨이 연동, 인증 등).
- 바운디드 컨텍스트(Bounded Context): 특정 모델과 용어(Ubiquitous Language)가 일관되게 통용되는 경계. 컨텍스트 간에는 명시적 통신(계약)으로 결합도를 관리합니다.
  - 대표 패턴: ACL(Anti‑Corruption Layer), Shared Kernel, Conformist, Partnership 등.
- 계층에서의 위치(전형적 레이어드 아키텍처):
  - Presentation(인터페이스) → Application(유스케이스/흐름 조율) → Domain(규칙/정책) → Infrastructure(영속성/외부시스템)
  - 비즈니스 규칙은 Domain에, 트랜잭션/흐름 조율은 Application에 배치하는 것이 일반적입니다.
- 간단 예시(전자상거래):
  - 서브도메인: 상품, 주문, 결제, 배송, 정산
  - 각 서브도메인마다 별도 바운디드 컨텍스트를 두고, 컨텍스트 간에는 이벤트/동기 API 등으로 통신합니다.

Tip: DTO는 계층/컨텍스트 경계를 넘나드는 데이터 전달용 구조체이고, 도메인 모델은 규칙과 불변식을 가진 풍부한 모델이어야 합니다.

---

## 2) 인터넷에서의 도메인(도메인 이름, DNS)
- 정의: 사람이 읽기 쉬운 이름으로, IP 주소를 간접적으로 가리킵니다(예: example.com → 93.184.216.34).
- 구조: [서브도메인].[SLD].[TLD]
  - 예: www.shop.example.co.kr
  - TLD: com, net, org, kr 등
  - SLD: example, co 등 레지스트리 정책에 따라 구성
  - 서브도메인: www, shop, api 등 조직 내부 용도로 분화
- 동작(아주 간단히): Resolver → 루트/권한 네임서버 순회 → 권한 응답 획득 → 캐싱(TTL)
- 주요 레코드: A/AAAA(주소), CNAME(별칭), MX(메일), TXT(임의 텍스트: SPF/도메인 검증 등), NS(권한 위임)
- 등록: Registrar(등록사업자)와 Registry(등록관리기관) 체계를 통해 소유/기간을 관리합니다.

---

## 3) 두 의미의 비교 요약
- 공통점: "무엇인가의 경계/이름이 붙은 영역"을 가리킵니다.
- 차이점:
  - 소프트웨어/DDD의 도메인: 비즈니스 문제 공간, 모델과 규칙의 경계(바운디드 컨텍스트) 중심.
  - 인터넷 도메인(DNS): 네트워크 식별자(이름)로서의 경로 지정/해석 중심.

---

## 4) 언제 어떤 뜻으로 쓰는지 구분하는 방법
- 문맥에 비즈니스 규칙/모델/DDD/바운디드 컨텍스트가 보이면 → 소프트웨어 도메인.
- 문맥에 DNS, A레코드, 네임서버, 소유권/등록이 보이면 → 인터넷 도메인.

---

## 5) 키워드/더 읽을거리
- DDD: Domain Model, Ubiquitous Language, Subdomain, Bounded Context, Context Map, Domain Event
- DNS: TLD/SLD/Subdomain, Authoritative/Recursive, TTL, Anycast, Zone Delegation, SPF/DKIM/DMARC

Last updated: 2025-08-10
