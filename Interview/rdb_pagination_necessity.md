# RDB에서 페이징 쿼리의 필요성

## 1. 한 줄 정의
페이징은 대용량 데이터를 일정 크기의 청크(chunk)로 나누어 단계적으로 조회하는 쿼리 기법으로, 메모리 효율성과 성능을 확보하기 위한 필수 요소다.

---

## 2. 페이징이 필요한 이유

### 2-1. 메모리 효율성(Memory Efficiency)
- **문제**: 전체 데이터를 한 번에 로드하면 메모리 부족
- **해결**: 필요한 데이터만 분할 조회

```sql
-- 나쁜 예: 모든 데이터를 메모리에 로드
SELECT * FROM users;  -- 1,000,000건이라면?

-- 좋은 예: 페이징으로 분할 조회
SELECT * FROM users LIMIT 20 OFFSET 0;
SELECT * FROM users LIMIT 20 OFFSET 20;
```

### 2-2. 응답 시간(Response Time)
- **문제**: 대용량 쿼리는 응답 시간이 매우 길어짐
- **해결**: 페이징으로 빠른 첫 응답 시간 확보

```python
# 사용자가 원하는 것: 빠른 첫 페이지 응답
# 1,000,000건 조회: 10초 (불가)
# 페이지 1 (20건): 0.1초 (가능)
```

### 2-3. 네트워크 효율성(Network Efficiency)
- **문제**: 대용량 데이터 전송은 네트워크 대역폭 낭비
- **해결**: 필요한 데이터만 전송

```
1,000,000건 데이터: 500MB 이상 (비효율)
페이지당 20건: 5KB (효율)
```

### 2-4. 사용자 경험(User Experience)
- **문제**: 무한 대기로 사용자 이탈
- **해결**: 즉시 결과를 보여주고 필요시 다음 페이지 로드

```
점진적 로딩(Progressive Loading) 경험 제공
┌─────────────────┐
│ Page 1 (빠르게) │ → 사용자가 이미 보기 시작
├─────────────────┤
│ Page 2 (로드중) │ → 백그라운드에서 다음 페이지 준비
└─────────────────┘
```

### 2-5. 데이터베이스 부하 감소(Database Load)
- **문제**: 대용량 쿼리는 DB 리소스 과다 사용
- **해결**: 소량의 데이터만 조회하여 DB 부하 감소

```
대용량 조회: CPU 100%, Memory 80% 사용
페이징 조회: CPU 10%, Memory 5% 사용
```

---

## 3. 데이터베이스별 페이징 구현

### 3-1. MySQL/MariaDB
```sql
-- LIMIT OFFSET 방식
SELECT * FROM orders
WHERE status = 'completed'
ORDER BY created_at DESC
LIMIT 20 OFFSET 40;  -- 41번째~60번째 레코드
```

### 3-2. PostgreSQL
```sql
-- LIMIT OFFSET 방식 (MySQL과 동일)
SELECT * FROM products
ORDER BY id
LIMIT 10 OFFSET 20;

-- FETCH 방식 (SQL 표준)
SELECT * FROM products
ORDER BY id
OFFSET 20 ROWS
FETCH NEXT 10 ROWS ONLY;
```

### 3-3. Oracle
```sql
-- ROWNUM 방식 (레거시)
SELECT * FROM (
    SELECT * FROM employees
    WHERE ROWNUM <= 30
    ORDER BY salary DESC
)
WHERE ROWNUM > 20;

-- ROW_NUMBER() 방식 (현대)
SELECT * FROM (
    SELECT ROW_NUMBER() OVER (ORDER BY salary DESC) as rn,
           * FROM employees
)
WHERE rn BETWEEN 21 AND 30;
```

### 3-4. MSSQL
```sql
-- OFFSET FETCH 방식
SELECT * FROM sales
ORDER BY sale_date DESC
OFFSET 50 ROWS
FETCH NEXT 20 ROWS ONLY;
```

---

## 4. 페이징 기법의 종류

### 4-1. Offset 기반 페이징
```sql
-- Page 1: OFFSET 0
-- Page 2: OFFSET 20
-- Page 3: OFFSET 40
SELECT * FROM users LIMIT 20 OFFSET 40;
```

**장점**:
- 구현이 간단
- 원하는 페이지로 직접 이동 가능

**단점**:
- 데이터 증가로 뒤로 갈수록 느려짐 (O(n))
- 데이터 삽입/삭제 중복/누락 가능

### 4-2. Cursor 기반 페이징 (Keyset)
```sql
-- 마지막 조회한 ID를 기준으로 다음 데이터 조회
SELECT * FROM posts
WHERE id > 1500  -- 마지막 cursor 값
ORDER BY id
LIMIT 20;
```

**장점**:
- 대용량 데이터에서 빠름 (O(1))
- 데이터 변경에도 안전

**단점**:
- 임의 페이지 이동 불가능
- 구현이 복잡

### 4-3. 검색 기반 페이징
```sql
-- 시간 범위로 페이징
SELECT * FROM events
WHERE created_at >= '2025-01-01'
  AND created_at < '2025-01-08'
ORDER BY created_at DESC;
```

**장점**:
- 사용자가 이해하기 쉬운 범위 제공

**단점**:
- 범위 설정이 복잡할 수 있음

---

## 5. 백엔드 개발자 관점의 중요성

### 5-1. API 설계
```python
# REST API 페이징 파라미터
GET /api/users?page=1&size=20
GET /api/products?cursor=1500&limit=20
GET /api/orders?from=2025-01-01&to=2025-01-07
```

### 5-2. 성능 최적화
```python
# 총 개수 조회는 비용이 높으므로 필요시에만 사용
SELECT COUNT(*) FROM large_table;  # 느림
SELECT FOUND_ROWS();               # 빠름 (MySQL)
```

### 5-3. 인덱싱 전략
```sql
-- 페이징에 사용되는 정렬 컬럼에는 인덱스 필수
CREATE INDEX idx_users_created_at ON users(created_at DESC);
CREATE INDEX idx_orders_id ON orders(id);
```

### 5-4. 데이터 일관성
```python
# Offset 기반: 데이터 변경 시 주의
# 페이지 1: id 1~20
# [새로운 데이터 3개 삽입]
# 페이지 2: id 18~37 (id 18-20이 중복!)

# 해결책: Cursor 기반 사용 또는 스냅샷 활용
```

---

## 6. 실전 페이징 예제

### 6-1. Python + SQLAlchemy
```python
from sqlalchemy import create_engine, desc
from sqlalchemy.orm import sessionmaker

# Offset 기반
def get_users_offset(page: int, size: int):
    offset = (page - 1) * size
    return session.query(User)\
        .order_by(User.created_at.desc())\
        .limit(size)\
        .offset(offset)\
        .all()

# Cursor 기반
def get_users_cursor(cursor_id: int, size: int):
    return session.query(User)\
        .filter(User.id > cursor_id)\
        .order_by(User.id)\
        .limit(size)\
        .all()
```

### 6-2. Spring Boot + JPA
```java
// Offset 기반
Page<User> users = userRepository.findAll(
    PageRequest.of(pageNumber, pageSize,
                   Sort.by("createdAt").descending())
);

// Cursor 기반
List<User> users = userRepository.findByIdGreaterThan(
    cursorId,
    PageRequest.of(0, pageSize, Sort.by("id"))
);
```

### 6-3. Node.js + Sequelize
```javascript
// Offset 기반
const users = await User.findAll({
    offset: (page - 1) * 20,
    limit: 20,
    order: [['createdAt', 'DESC']]
});

// Cursor 기반
const users = await User.findAll({
    where: { id: { [Op.gt]: cursorId } },
    limit: 20,
    order: [['id', 'ASC']]
});
```

---

## 7. 핵심 요약

| 측면 | 필요성 | 효과 |
|------|--------|------|
| **메모리** | 대용량 데이터 적재 방지 | 메모리 사용 90% 감소 |
| **응답시간** | 빠른 첫 페이지 응답 | 응답시간 10배 개선 |
| **네트워크** | 데이터 전송량 최소화 | 대역폭 효율적 사용 |
| **UX** | 즉시 결과 표시 | 사용자 만족도 향상 |
| **DB 부하** | 리소스 효율적 사용 | 동시 사용자 수 증가 |

### 7-1. 페이징 기법 선택 기준

| 상황 | 추천 기법 | 이유 |
|------|----------|------|
| 작은 데이터셋 (<10만 건) | Offset | 구현 간단 |
| 큰 데이터셋 (>1백만 건) | Cursor | 성능 우수 |
| 임의 페이지 이동 필요 | Offset | 유연성 |
| 실시간 스트림 데이터 | Cursor | 데이터 일관성 |
| 시간대별 조회 | 검색 기반 | 사용성 우수 |

### 7-2. 실무 팁

1. **항상 페이징을 기본으로 사용하기**
   - API에서 무제한 조회는 금지
   - 기본 페이지 크기 설정: 10~50건

2. **Offset 기반에서 깊은 페이지 접근 제한**
   ```python
   # 예: 최대 1000 페이지까지만 허용
   max_offset = 1000 * page_size  # 50,000 건까지
   ```

3. **인덱스 최적화 필수**
   ```sql
   -- 정렬 컬럼에 인덱스 생성
   CREATE INDEX idx_created_at ON users(created_at DESC);
   ```

4. **총 개수 조회 신중하게 사용**
   - 큰 테이블의 COUNT()는 매우 느림
   - 필요한 경우에만 별도 캐시 사용

5. **데이터 변경 대비**
   - Cursor 기반 또는 타임스탐프 기반 권장
   - 트랜잭션 격리 수준 고려

6. **캐싱 전략**
   ```python
   # 자주 요청되는 페이지는 캐시
   @cache.cached(timeout=3600)
   def get_popular_page():
       return get_users_offset(page=1, size=20)
   ```

---

## 8. 참고: LIMIT/OFFSET의 성능 이슈

```sql
-- 빠름: 앞 페이지
SELECT * FROM users LIMIT 20 OFFSET 0;     -- 0.01초

-- 느림: 뒤 페이지
SELECT * FROM users LIMIT 20 OFFSET 1000000; -- 5초 이상
-- 왜? 1,000,020건을 스캔한 후 1,000,000건을 버림
```

**해결책: Cursor 기반 전환**
```sql
-- 빠름: 커서 기반 (항상 일정한 성능)
SELECT * FROM users WHERE id > 1000000 LIMIT 20;  -- 0.01초
```