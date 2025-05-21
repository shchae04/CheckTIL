# 조회 트래픽을 고려한 DB 인덱스 설계

## 1. 개요
데이터베이스 인덱스는 대용량 데이터에서 검색 성능을 향상시키는 핵심 요소입니다. 특히 높은 조회 트래픽이 발생하는 시스템에서는 인덱스 설계가 전체 시스템 성능에 결정적인 영향을 미칩니다. 이 문서에서는 조회 트래픽이 많은 환경에서 효율적인 인덱스 설계 방법에 대해 알아보겠습니다.

## 2. 조회 트래픽 분석의 중요성

### 2.1 트래픽 패턴 분석
- **쿼리 빈도**: 어떤 쿼리가 얼마나 자주 실행되는지 파악
- **쿼리 복잡성**: 단순 조회인지, 복잡한 조인과 필터링이 포함된 쿼리인지 분석
- **시간대별 트래픽 분포**: 피크 시간대와 평상시 트래픽 차이 파악

### 2.2 분석 도구
- **쿼리 로그 분석**: 데이터베이스의 쿼리 로그를 수집하고 분석
- **성능 모니터링 도구**: Prometheus, Grafana 등을 활용한 실시간 모니터링
- **실행 계획(Execution Plan) 분석**: EXPLAIN 명령어를 통한 쿼리 실행 계획 검토

## 3. 고트래픽 환경을 위한 인덱스 설계 원칙

### 3.1 선택성(Selectivity) 최적화
- **높은 선택성 컬럼 우선**: 고유한 값이 많은 컬럼을 인덱스의 선행 컬럼으로 배치
- **선택성 계산 방법**: `선택성 = 고유값 수 / 전체 행 수` (값이 1에 가까울수록 선택성이 높음)
- **예시**:
  ```sql
  -- 사용자 ID(높은 선택성)와 상태(낮은 선택성)가 있을 때
  -- 좋은 설계: 사용자 ID를 선행 컬럼으로
  CREATE INDEX idx_user_status ON users(user_id, status);
  ```

### 3.2 쿼리 패턴에 맞는 복합 인덱스 설계
- **WHERE 절 순서 고려**: WHERE 절에서 자주 함께 사용되는 컬럼들을 하나의 복합 인덱스로 설계
- **컬럼 순서의 중요성**: 가장 자주 필터링되는 컬럼을 앞에 배치
- **예시**:
  ```sql
  -- 자주 실행되는 쿼리가 아래와 같을 때
  SELECT * FROM orders WHERE customer_id = ? AND order_date BETWEEN ? AND ?;
  
  -- 효율적인 인덱스 설계
  CREATE INDEX idx_customer_date ON orders(customer_id, order_date);
  ```

### 3.3 인덱스 커버링(Index Covering) 활용
- **개념**: 쿼리에 필요한 모든 컬럼이 인덱스에 포함되어 테이블 접근 없이 인덱스만으로 쿼리 처리
- **장점**: 디스크 I/O 감소, 쿼리 성능 대폭 향상
- **예시**:
  ```sql
  -- 자주 실행되는 쿼리
  SELECT product_id, price FROM products WHERE category = 'electronics';
  
  -- 커버링 인덱스 설계
  CREATE INDEX idx_category_pid_price ON products(category, product_id, price);
  ```

### 3.4 인덱스 크기와 메모리 고려
- **인덱스 크기 제한**: 너무 많은 컬럼을 포함하면 인덱스 크기가 커져 메모리 효율성 감소
- **메모리 내 캐싱**: 자주 사용되는 인덱스는 메모리에 캐싱되어야 최상의 성능 발휘
- **인덱스 페이지 분할(Page Split) 최소화**: 데이터 삽입 패턴을 고려한 인덱스 설계

## 4. 읽기 집중 워크로드를 위한 특수 인덱스 기법

### 4.1 부분 인덱스(Partial Index)
- **개념**: 테이블의 일부 행에만 적용되는 인덱스
- **사용 사례**: 특정 조건에 해당하는 데이터만 자주 조회되는 경우
- **예시** (PostgreSQL):
  ```sql
  -- 활성 사용자만 자주 조회되는 경우
  CREATE INDEX idx_active_users ON users(username) WHERE status = 'active';
  ```

### 4.2 함수 기반 인덱스(Function-Based Index)
- **개념**: 컬럼 값에 함수를 적용한 결과에 대한 인덱스
- **사용 사례**: 대소문자 구분 없는 검색, 날짜 부분 추출 검색 등
- **예시** (Oracle, PostgreSQL):
  ```sql
  -- 대소문자 구분 없는 이메일 검색을 위한 인덱스
  CREATE INDEX idx_email_lower ON users(LOWER(email));
  
  -- 쿼리 예시
  SELECT * FROM users WHERE LOWER(email) = 'user@example.com';
  ```

### 4.3 비트맵 인덱스(Bitmap Index)
- **개념**: 낮은 카디널리티 컬럼에 효과적인 인덱스 유형
- **사용 사례**: 성별, 상태 코드 등 고유값이 적은 컬럼
- **장점**: 공간 효율성, 다중 조건 필터링에 효과적
- **단점**: 높은 DML 비용, 동시성 제한

## 5. 샤딩 환경에서의 인덱스 설계

### 5.1 샤딩 키와 인덱스 관계
- **샤딩 키 선택**: 트래픽 분산을 고려한 샤딩 키 선택
- **로컬 인덱스 vs 글로벌 인덱스**: 각 샤드별 로컬 인덱스와 전체 샤드를 아우르는 글로벌 인덱스의 장단점
- **샤드 간 쿼리 최소화**: 단일 샤드 내에서 해결 가능한 쿼리 설계

### 5.2 분산 환경에서의 인덱스 일관성
- **인덱스 업데이트 전략**: 동기식 vs 비동기식 인덱스 업데이트
- **일관성 수준**: 강한 일관성 vs 최종 일관성 (Strong vs Eventual Consistency)

## 6. 인덱스 관리 및 모니터링

### 6.1 인덱스 사용 모니터링
- **사용되지 않는 인덱스 식별**: 리소스만 차지하는 불필요한 인덱스 제거
- **인덱스 사용 통계 수집**: 인덱스별 사용 빈도 및 효율성 측정
- **모니터링 쿼리 예시** (MySQL):
  ```sql
  -- 사용되지 않는 인덱스 찾기
  SELECT * FROM sys.schema_unused_indexes;
  
  -- 인덱스 사용 통계
  SELECT * FROM sys.schema_index_statistics ORDER BY rows_selected DESC;
  ```

### 6.2 인덱스 재구성 및 유지보수
- **인덱스 단편화(Fragmentation) 모니터링**
- **정기적인 인덱스 재구성(Rebuild) 또는 재구성(Reorganize)**
- **유지보수 작업의 영향 최소화**: 오프 피크 시간대 활용, 온라인 재구성 옵션 사용

## 7. 데이터베이스별 인덱스 최적화 기법

### 7.1 MySQL/InnoDB
- **클러스터드 인덱스 활용**: 기본키가 클러스터드 인덱스로 자동 생성됨
- **커버링 인덱스와 ICP(Index Condition Pushdown)**: 쿼리 성능 향상을 위한 최적화
- **인덱스 힌트 사용**: 필요한 경우 옵티마이저에 인덱스 사용 지시

### 7.2 PostgreSQL
- **부분 인덱스와 표현식 인덱스**: 특정 조건이나 표현식에 대한 인덱스 생성
- **GIN, GiST 인덱스**: 전문 검색, 지리 정보 등 특수 데이터 타입을 위한 인덱스
- **병렬 인덱스 스캔**: 고성능 쿼리를 위한 병렬 처리 활용

### 7.3 Oracle
- **인덱스 구성 옵션**: COMPRESS, NOLOGGING 등 성능 최적화 옵션
- **파티션 인덱스**: 대용량 테이블의 파티션에 맞춘 인덱스 설계
- **인덱스 모니터링 도구**: AWR, ASH 등을 활용한 성능 분석

## 8. 실제 사례 연구

### 8.1 소셜 미디어 플랫폼의 인덱스 설계
- **사용자 타임라인 쿼리 최적화**: 최신 게시물을 빠르게 조회하기 위한 인덱스 설계
- **해시태그 검색 최적화**: 해시태그 검색을 위한 특수 인덱스 구조
- **트래픽 급증 대응**: 인기 콘텐츠 조회 시 성능 유지를 위한 전략

### 8.2 전자상거래 플랫폼의 인덱스 설계
- **상품 검색 최적화**: 다양한 필터링 조건을 지원하는 인덱스 구조
- **주문 이력 조회**: 사용자별 주문 이력을 효율적으로 조회하기 위한 인덱스
- **재고 관리**: 실시간 재고 업데이트와 조회를 위한 인덱스 전략

## 9. 결론
조회 트래픽이 많은 환경에서 효율적인 인덱스 설계는 시스템 성능과 사용자 경험에 직접적인 영향을 미칩니다. 트래픽 패턴을 분석하고, 쿼리 특성에 맞는 인덱스를 설계하며, 지속적인 모니터링과 최적화를 통해 고성능 데이터베이스 시스템을 구축할 수 있습니다. 인덱스는 만능 해결책이 아니므로, 각 상황에 맞는 균형 잡힌 접근이 중요합니다.

## 10. 참고 자료
- [MySQL 인덱스 최적화 가이드](https://dev.mysql.com/doc/refman/8.0/en/optimization-indexes.html)
- [PostgreSQL 인덱스 타입](https://www.postgresql.org/docs/current/indexes-types.html)
- [Oracle 인덱스 설계 및 관리](https://docs.oracle.com/en/database/oracle/oracle-database/19/admin/managing-indexes.html)
- "Database System Concepts" (Silberschatz, Korth, Sudarshan)
- "High Performance MySQL" (Baron Schwartz, Peter Zaitsev, Vadim Tkachenko)