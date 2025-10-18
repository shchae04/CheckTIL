# 실전 예시: 데이터 모델링 워크스루 (전자상거래 주문 도메인)

## 1. 한 줄 요약
- 요구사항에서 시작해 개념→논리→물리 모델을 단계적으로 정제하고, 제약·인덱스·쿼리·운영(스키마 진화/파티셔닝)까지 고려한 설계를 예시로 보여준다.

---

## 2. 문제 정의와 범위
- 도메인: 전자상거래(상품, 고객, 장바구니, 주문, 결제, 배송)
- 범위: 핵심 트랜잭션 흐름(상품 조회→장바구니→주문→결제→배송)
- 비범위(Out of Scope): 추천, 광고, 반품/환불 상세 로직(간단 언급만)

---

## 3. 요구사항 정리(요약)
- 상품(가격, 재고, 옵션) 조회가 빠를 것
- 장바구니는 고객별로 여러 아이템을 포함, 옵션/수량 변경 가능
- 주문은 결제 전후 상태 전이를 가진다(PLACED→PAID→FULFILLING→SHIPPED→DELIVERED / CANCELLED)
- 주문 항목은 주문 시점의 상품 스냅샷(이름/가격/옵션)을 보존해야 한다
- 결제는 외부 PG와 연동, 멱등키로 중복 결제 방지
- 재고 차감은 주문 확정 혹은 결제 확정 시점에 일관되게 수행
- 운영: 일별 주문 리포트, 고객별 최근 주문 조회가 빈번, 1년 지난 주문은 아카이브 고려

---

## 4. 액터와 주요 유스케이스
- 액터: 고객(Customer), 관리자(Admin), 결제게이트웨이(PG)
- UC1 상품 검색/조회, UC2 장바구니 담기/수정, UC3 주문 생성, UC4 결제 승인/실패 처리, UC5 배송 처리, UC6 주문 조회/리포트

---

## 5. 개념 모델(Conceptual)
- 엔티티: Customer, Product, ProductOption, Inventory, Cart, CartItem, Order, OrderItem, Payment, Shipment
- 관계(요지):
  - Customer 1–N Cart, 1–N Order, 1–N Payment
  - Product 1–N ProductOption, 1–1 Inventory(혹은 SKU 단위로 Inventory)
  - Cart 1–N CartItem (CartItem — ProductOption 참조)
  - Order 1–N OrderItem (OrderItem — ProductOption 스냅샷)
  - Order 1–1 Payment, Order 1–N Shipment(부분 배송 가능 시)

텍스트 ERD(요약):
- Customer(cust_id) ⟶ Cart(cart_id, cust_id)
- Product(prod_id) ⟶ ProductOption(opt_id, prod_id)
- ProductOption(opt_id) ⟶ Inventory(inv_id, opt_id)
- Cart(cart_id) ⟶ CartItem(ci_id, cart_id, opt_id)
- Order(order_id, cust_id) ⟶ OrderItem(oi_id, order_id, opt_id_snapshot)
- Order(order_id) ⟶ Payment(pay_id, order_id)
- Order(order_id) ⟶ Shipment(ship_id, order_id)

---

## 6. 논리 모델(Logical) & 정규화 포인트
- 기본 원칙: 3NF/BCNF 수준 정규화, 스냅샷 필드는 중복 허용(탈정규화) 근거 명확화
- 키 선택: 대부분 서퍼게이트 PK(BIGINT, AUTO INCREMENT 또는 UUIDv7), 자연키에는 UNIQUE

예시 속성 결정(요지):
- Product(prod_id, name, brand, category_id, created_at)
- ProductOption(opt_id, prod_id, option_name, price, currency)
- Inventory(inv_id, opt_id, stock_qty)
- Customer(cust_id, email UNIQUE, name, created_at)
- Cart(cart_id, cust_id, updated_at), CartItem(ci_id, cart_id, opt_id, qty)
- Order(order_id, cust_id, status, placed_at, paid_at NULLABLE, cancelled_at NULLABLE)
- OrderItem(oi_id, order_id, opt_id, product_name_snapshot, option_name_snapshot, unit_price_snapshot, qty)
- Payment(pay_id, order_id UNIQUE, pg_txn_id UNIQUE, status, amount, currency, idempotency_key UNIQUE, approved_at NULLABLE)
- Shipment(ship_id, order_id, carrier, tracking_no UNIQUE NULLABLE, status, shipped_at NULLABLE, delivered_at NULLABLE)

정규화/탈정규화 근거:
- 상품/옵션/재고는 정규화(중복 최소화)
- 주문 항목은 스냅샷 필드로 당시 가격/이름 보존(회계/감사 목적)

---

## 7. 물리 모델(Physical) 설계 요점
- 자료형: BIGINT PK, VARCHAR 길이 제한, 금액 DECIMAL(18,2) 등
- 제약: NOT NULL, CHECK(status in ...), FK ON UPDATE/DELETE 정책 명확화
- 인덱스:
  - Order(cust_id, placed_at DESC) 커버링(조회 최적화)
  - Payment(idempotency_key), Payment(pg_txn_id) UNIQUE
  - ProductOption(prod_id), Inventory(opt_id) FK 인덱스
  - Shipment(order_id, shipped_at)
- 트랜잭션 경계: 주문 생성+결제 준비, 결제 승인 웹훅 처리, 재고 차감 원자성 보장

관련 읽기: [동시성 제어](./database_concurrency_control.md)

---

## 8. 샘플 DDL (PostgreSQL 방언 예)
```sql
CREATE TABLE customer (
  cust_id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product (
  prod_id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  brand VARCHAR(100),
  category_id BIGINT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE product_option (
  opt_id BIGSERIAL PRIMARY KEY,
  prod_id BIGINT NOT NULL REFERENCES product(prod_id) ON DELETE CASCADE,
  option_name VARCHAR(200) NOT NULL,
  price DECIMAL(18,2) NOT NULL,
  currency CHAR(3) NOT NULL DEFAULT 'KRW'
);

CREATE TABLE inventory (
  inv_id BIGSERIAL PRIMARY KEY,
  opt_id BIGINT NOT NULL UNIQUE REFERENCES product_option(opt_id) ON DELETE CASCADE,
  stock_qty INTEGER NOT NULL CHECK (stock_qty >= 0)
);

CREATE TABLE cart (
  cart_id BIGSERIAL PRIMARY KEY,
  cust_id BIGINT NOT NULL REFERENCES customer(cust_id) ON DELETE CASCADE,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE cart_item (
  ci_id BIGSERIAL PRIMARY KEY,
  cart_id BIGINT NOT NULL REFERENCES cart(cart_id) ON DELETE CASCADE,
  opt_id BIGINT NOT NULL REFERENCES product_option(opt_id),
  qty INTEGER NOT NULL CHECK (qty > 0)
);

CREATE TABLE "order" (
  order_id BIGSERIAL PRIMARY KEY,
  cust_id BIGINT NOT NULL REFERENCES customer(cust_id),
  status VARCHAR(20) NOT NULL CHECK (status IN ('PLACED','PAID','FULFILLING','SHIPPED','DELIVERED','CANCELLED')),
  placed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  paid_at TIMESTAMPTZ,
  cancelled_at TIMESTAMPTZ
);

CREATE INDEX idx_order_cust_placed ON "order" (cust_id, placed_at DESC);

CREATE TABLE order_item (
  oi_id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES "order"(order_id) ON DELETE CASCADE,
  opt_id BIGINT NOT NULL REFERENCES product_option(opt_id),
  product_name_snapshot VARCHAR(200) NOT NULL,
  option_name_snapshot VARCHAR(200) NOT NULL,
  unit_price_snapshot DECIMAL(18,2) NOT NULL,
  qty INTEGER NOT NULL CHECK (qty > 0)
);

CREATE TABLE payment (
  pay_id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL UNIQUE REFERENCES "order"(order_id) ON DELETE CASCADE,
  pg_txn_id VARCHAR(100) UNIQUE,
  status VARCHAR(20) NOT NULL CHECK (status IN ('INIT','APPROVED','FAILED','CANCELLED')),
  amount DECIMAL(18,2) NOT NULL,
  currency CHAR(3) NOT NULL,
  idempotency_key VARCHAR(100) NOT NULL UNIQUE,
  approved_at TIMESTAMPTZ
);

CREATE TABLE shipment (
  ship_id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES "order"(order_id) ON DELETE CASCADE,
  carrier VARCHAR(50) NOT NULL,
  tracking_no VARCHAR(100) UNIQUE,
  status VARCHAR(20) NOT NULL CHECK (status IN ('READY','SHIPPED','DELIVERED')),
  shipped_at TIMESTAMPTZ,
  delivered_at TIMESTAMPTZ
);
```

---

## 9. 주요 쿼리 예시
- 고객 최근 주문 20건
```sql
SELECT o.order_id, o.status, o.placed_at, SUM(oi.unit_price_snapshot * oi.qty) AS total
FROM "order" o
JOIN order_item oi ON oi.order_id = o.order_id
WHERE o.cust_id = $1
GROUP BY o.order_id, o.status, o.placed_at
ORDER BY o.placed_at DESC
LIMIT 20;
```

- 결제 멱등 처리(INSERT ON CONFLICT)
```sql
INSERT INTO payment(order_id, pg_txn_id, status, amount, currency, idempotency_key)
VALUES ($1, $2, 'APPROVED', $3, $4, $5)
ON CONFLICT (idempotency_key) DO NOTHING;
```

- 재고 차감(낙관적 락 예)
```sql
UPDATE inventory
SET stock_qty = stock_qty - $2
WHERE opt_id = $1 AND stock_qty >= $2;
-- 갱신 행 수 = 1 이어야 성공
```

- 일별 주문 매출(리포트)
```sql
SELECT date_trunc('day', o.placed_at) AS day,
       SUM(oi.unit_price_snapshot * oi.qty) AS revenue
FROM "order" o
JOIN order_item oi ON oi.order_id = o.order_id
WHERE o.placed_at >= now() - interval '30 days'
GROUP BY day
ORDER BY day;
```

---

## 10. 상태 전이와 트랜잭션 경계(요약)
- 주문 생성: Order(PLACED) + OrderItem 삽입, (선예약 재고를 쓰는 경우 Reservation 테이블 도입 고려)
- 결제 승인 웹훅: Payment 멱등 처리 → Order.status=PAID → 재고 차감 → 후속 Fulfillment 이벤트 발행
- 취소: 상태 전이 유효성 체크(CHECK/트리거/애플리케이션) + 재고 롤백 규칙 명확화

---

## 11. 확장/운영 고려
- 파티셔닝: "order"를 placed_at 월별 파티션으로 분할, 오래된 파티션 아카이브/Detach
- 읽기 복제: 리포트는 리더/팔로워 읽기 분산 고려
- 스키마 진화: 추가→이중쓰기→마이그레이션→제거 절차 준수(예: 결제 status 추가 시 단계적 배포)

관련 읽기: [복제](./database_replication.md), [좋은 설계 원칙](./good_database_design.md)

---

## 12. 안티패턴과 교정
- 주문 항목에 현재 Product를 조인해 보여주기: 시간이 지나 이름/가격 변경 시 과거 주문 왜곡 → 스냅샷 컬럼 채택(이미 반영)
- 무분별한 UUIDv4 PK: 인덱스 파편화 → BIGSERIAL 또는 시간순 UUIDv7 권장
- 제약 미적용: CHECK/UNIQUE/FK로 데이터 품질을 DB 레벨에서 강제

---

## 13. 체크리스트
- [ ] 주요 쿼리의 인덱스 존재/실행계획 확인(EXPLAIN)
- [ ] FK/UNIQUE/NOT NULL/CHECK 제약 적용
- [ ] 멱등키/중복 방지 로직 검증
- [ ] 파티션/아카이브 전략 수립
- [ ] 마이그레이션 롤백 계획

---

## 14. 핵심 정리
- 정규화된 모델을 기본으로, 스냅샷/인덱스/파티셔닝 등 실용적 탈정규화를 ‘근거 있게’ 적용하라.
- 상태 전이와 트랜잭션 경계를 명확히 하고, 멱등성과 재시도로 운영 안전성을 확보하라.
- 리포트/조회 패턴을 초기부터 반영하면 DB와 애플리케이션 양쪽의 복잡도를 줄일 수 있다.
