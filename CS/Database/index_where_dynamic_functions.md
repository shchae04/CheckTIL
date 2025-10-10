# 인덱스 WHERE 절에 NOW() 같은 계속 바뀌는 함수를 쓸 수 없는 이유

TIL: 인덱스를 만들 때 필터 조건(부분/필터드 인덱스의 WHERE 절)이나 인덱스 키의 표현식에 `NOW()`처럼 시간이 지나며 값이 바뀌는 함수(비결정적, 변동 함수)를 사용할 수 없다. 왜 그럴까?

---

## 핵심 요약 (TL;DR)
- 인덱스는 “어떤 행이 인덱스에 포함되는지”를 항상 일관되게 판단할 수 있어야 한다.
- `NOW()`, `RAND()`, `UUID()`처럼 실행 시점마다 결과가 달라지는 함수가 조건/표현식에 들어가면, 시간이 흐르면서 같은 행의 포함 여부가 바뀐다.
- 그러면 인덱스의 정확성과 유지 비용이 감당 불가능해져서, 대부분의 DB는 인덱스 표현식과 부분 인덱스 WHERE 절에 “불변(immutable)/결정적(deterministic)” 함수만 허용한다.
- “최근 7일” 같은 동적 조건을 최적화하고 싶다면, 보통은 컬럼 자체에 일반 인덱스를 걸면 된다. 쿼리 시점의 `NOW()`는 런타임에 평가되며, 플래너는 범위 스캔으로 일반 인덱스를 잘 활용한다.

---

## 왜 안 되나요? (개념)
인덱스는 테이블 데이터가 바뀌거나 시간이 흘러도, 어떤 행이 인덱스에 들어갈지 기준이 “행 자체의 값에 의해” 결정되어야 한다. 이는 다음 두 가지 이유 때문이다.

1) 일관성(consistency)
- 인덱스는 테이블 변경 시점(INSERT/UPDATE/DELETE)에 즉시 갱신된다.
- 만약 인덱스의 포함 조건이 “현재 시각(now)”처럼 시간이 지나면 변하는 값에 의존한다면, 데이터를 건드리지 않아도 시간이 흐르는 것만으로 포함/제외가 계속 뒤바뀐다.
- DB가 매 초마다 인덱스를 재평가/재구성해야 하는 말이 되며, 현실적으로 불가능하다.

2) 결정성(determinism)과 불변성(immutability)
- 대부분의 DB는 인덱스에 쓰이는 함수와 표현식이 “같은 입력 -> 항상 같은 출력”을 보장해야 한다고 규정한다.
- 이는 인덱스가 “행의 값만 보면” 포함 여부와 정렬 키를 안정적으로 판단할 수 있게 하기 위함이다.
- `NOW()`는 입력이 없어 보이지만, 숨은 입력은 “현재 시각”이며 시각은 계속 변한다 → 비결정적/불변 아님 → 금지.

---

## DBMS 별 규칙 요약
- PostgreSQL
  - 인덱스 표현식(functional index)의 함수는 `IMMUTABLE`이어야 한다.
  - 부분 인덱스(partial index)의 WHERE 절 역시 사실상 불변식만 허용된다. `NOW()`, `CURRENT_DATE` 등은 `STABLE`/`VOLATILE`로 간주되어 금지.
  - 예) `ERROR: functions in index predicate must be immutable`

- MySQL 8.0
  - 표현식 인덱스는 “결정적(deterministic)”이어야 하므로 `NOW()`, `RAND()` 등 비결정적 함수 사용 불가.

- SQL Server
  - Filtered Index의 WHERE 절은 결정적 표현식만 허용. `GETDATE()` 같은 비결정적 함수 사용 불가.

- Oracle (Function-Based Index)
  - 함수는 입력이 같으면 같은 값을 내는 “결정적”이어야 한다(필요 시 함수에 DETERMINISTIC 힌트를 주지만, 실제로 시간 의존 함수는 여전히 부적합).

요지는 모두 같다: 시간이 흐르면 결과가 달라지는 함수는 인덱스 정의에 들어갈 수 없다.

---

## 흔한 오해와 올바른 대안

오해: “최근 7일만 자주 조회하니, `WHERE created_at >= NOW() - INTERVAL '7 days'` 로 부분 인덱스를 만들자.”
- 잘못된 이유: `NOW()`가 시점마다 달라져 인덱스 포함 범위가 계속 움직인다 → 부분 인덱스 조건에 사용할 수 없다.

옳은 방법 1: 컬럼에 일반 인덱스 사용
- created_at 컬럼에 일반 인덱스(예: B-Tree)만 걸어도, 아래 쿼리는 인덱스 범위 스캔으로 충분히 최적화된다.
- 예 (PostgreSQL/MySQL 공통 아이디어):
  - 인덱스 생성: `CREATE INDEX idx_created_at ON events(created_at);`
  - 실행 쿼리: `SELECT * FROM events WHERE created_at >= NOW() - INTERVAL '7 days';`
  - 쿼리 시점에 `NOW()`가 숫자/시간 상수로 평가되고, 플래너가 `created_at`에 대해 `>=` 범위로 인덱스를 사용한다.

옳은 방법 2: 생성(가상/퍼시스턴트) 컬럼 + 인덱스
- “최근 7일 여부” 같은 동적 조건을 미리 물리 컬럼으로 보유하고 갱신하는 방법.
- 예) `is_recent_7d BOOLEAN` 컬럼을 두고, 트리거 또는 배치로 업데이트한 뒤, `is_recent_7d`에 인덱스를 생성.
- 장점: 쿼리가 단순해지고 인덱스 선택성이 좋아질 수 있음. 단점: 운영 복잡성(동기화 작업)이 늘어난다.

옳은 방법 3: 파티셔닝(partitioning)
- 시간 기준으로 테이블을 파티션(일/월 단위)하고, 쿼리 범위에 맞는 파티션만 스캔하게 한다.
- 오래된 파티션은 drop/detach 하여 관리. 동적 “최근 N일” 조건도 파티션 프루닝으로 비용이 크게 준다.

옳은 방법 4: (정말로 필요하다면) 고정 경계의 부분 인덱스
- 움직이는 경계(NOW 기반) 대신, 고정된 경계를 사용한 부분 인덱스를 쓸 수는 있다.
- 예) `WHERE created_at >= DATE '2025-01-01' AND status = 'ACTIVE'` 처럼 시간 상수를 고정.
- 하지만 시간이 지나면 적합성이 떨어지므로 관리 포인트가 된다.

보너스: 표현식 인덱스는 가능하지만, 표현식도 불변이어야 함
- 예) `CREATE INDEX idx_trunc_day ON events (date_trunc('day', created_at));` (PostgreSQL)
- `date_trunc('day', x)`는 입력이 같으면 결과가 같으므로 불변. 쿼리도 같은 표현식을 사용해야 인덱스를 탄다:
  - `WHERE date_trunc('day', created_at) = date_trunc('day', TIMESTAMP '2025-10-10 00:00:00')`

---

## 실무 체크리스트
- 부분/필터드 인덱스 WHERE, 표현식 인덱스의 함수/연산자는 “결정적/불변”인지 확인한다.
- 시간 의존 쿼리는 보통 “일반 컬럼 인덱스 + 범위 조건”으로 충분히 빠르다.
- 더 빠른 응답이 필요하면 파티셔닝, 생성 컬럼(트리거/배치 갱신), 캐시 등을 검토한다.
- DB 벤더별 제약을 문서로 확인하자 (PostgreSQL, MySQL, SQL Server, Oracle 모두 유사 정책).

---

## 오류 예시 모음
- PostgreSQL (부분 인덱스):
  - `CREATE INDEX idx_recent ON events(created_at) WHERE created_at >= NOW() - INTERVAL '7 days';`
  - 결과: `ERROR: functions in index predicate must be immutable`

- MySQL (표현식 인덱스):
  - `CREATE INDEX idx_recent ON events ((created_at >= NOW() - INTERVAL 7 DAY));`
  - 결과: `ERROR 3753 (HY000): A functional index expression is not deterministic`

- SQL Server (Filtered Index):
  - `CREATE INDEX IX_recent ON dbo.Events(created_at) WHERE created_at >= DATEADD(day, -7, GETDATE());`
  - 결과: `Msg 10609 ... filtered index ... non-deterministic functions are not allowed`

---

## 결론
- 인덱스 정의에는 시간이 지나며 변하는 값에 의존하는 함수가 들어갈 수 없다.
- 대부분의 경우, “일반 인덱스 + 런타임 평가된 NOW()를 사용한 범위 조건”이면 성능은 충분히 좋다.
- 필요 시 파티셔닝, 생성 컬럼, 배치/트리거 등으로 “동적 조건”을 정적으로 재구성해 인덱스에 적합한 형태로 바꾸자.
