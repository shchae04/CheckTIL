# RDS를 시계열 DB로 전환하는 방법과 이점

## 1) 한줄 정의 (TL;DR)
- RDS(관계형 데이터베이스)에서 시계열 전용 데이터베이스로 마이그레이션하여 시간 기반 데이터의 저장, 조회, 분석 성능을 대폭 향상시키고 운영 비용을 절감하는 전환 전략이다.

---

## 2) 왜 RDS에서 시계열 DB로 전환해야 하는가

### RDS의 한계점
- **쿼리 성능**: 시간 범위 조회 시 B-Tree 인덱스의 비효율성
- **저장 공간**: 정규화된 스키마로 인한 메타데이터 오버헤드
- **압축률**: 시계열 데이터 특성을 활용한 압축 불가
- **스케일링**: 수직 확장에 의존, 수평 확장 복잡성
- **보존 정책**: 자동화된 데이터 라이프사이클 관리 부재

### 시계열 DB의 장점
- **고성능**: 시간 기반 인덱싱과 청크 분할로 빠른 조회
- **압축률**: 델타 압축, RLE 등으로 90% 이상 공간 절약
- **자동 관리**: 다운샘플링, TTL 기반 자동 삭제
- **수평 확장**: 샤딩과 클러스터링 지원
- **분석 기능**: 내장된 시계열 분석 함수

---

## 3) 주요 시계열 DB 옵션

### TimescaleDB (PostgreSQL 확장)
```sql
-- 하이퍼테이블 생성
CREATE TABLE metrics (
  time    TIMESTAMPTZ NOT NULL,
  device_id INTEGER,
  cpu     DOUBLE PRECISION,
  memory  DOUBLE PRECISION
);
SELECT create_hypertable('metrics', 'time');

-- 압축 정책
ALTER TABLE metrics SET (
  timescaledb.compress,
  timescaledb.compress_segmentby = 'device_id'
);
SELECT add_compress_policy('metrics', INTERVAL '7 days');
```

### InfluxDB
```sql
-- 데이터 입력
INSERT cpu,host=server01,region=us-west value=0.64 1465839830100400200

-- 쿼리
SELECT mean("value") FROM "cpu" 
WHERE time >= now() - 1h 
GROUP BY time(5m), "host"
```

### Amazon Timestream
```sql
-- 테이블 생성
CREATE TABLE "myDatabase"."myTable"

-- 데이터 조회
SELECT BIN(time, 5m) as time_bin, 
       AVG(measure_value::double) as avg_cpu
FROM "myDatabase"."myTable"
WHERE measure_name = 'cpu_utilization'
  AND time between ago(1h) and now()
GROUP BY BIN(time, 5m)
ORDER BY time_bin
```

---

## 4) 마이그레이션 전략

### 4.1) 평가 및 계획 단계
```bash
# 현재 데이터 분석
SELECT 
  DATE_TRUNC('hour', created_at) as hour,
  COUNT(*) as records_per_hour,
  pg_size_pretty(pg_total_relation_size('metrics_table')) as table_size
FROM metrics_table
WHERE created_at >= NOW() - INTERVAL '30 days'
GROUP BY hour
ORDER BY hour;
```

### 4.2) 점진적 마이그레이션 (Dual Write)
```python
# 듀얼 라이트 패턴
class MetricsWriter:
    def __init__(self, postgres_conn, influxdb_client):
        self.postgres = postgres_conn
        self.influxdb = influxdb_client
        
    def write_metric(self, timestamp, device_id, cpu, memory):
        # 기존 PostgreSQL 저장
        self.postgres.execute(
            "INSERT INTO metrics VALUES (%s, %s, %s, %s)",
            (timestamp, device_id, cpu, memory)
        )
        
        # 새로운 InfluxDB 저장
        point = Point("metrics")\
            .tag("device_id", device_id)\
            .field("cpu", cpu)\
            .field("memory", memory)\
            .time(timestamp)
        self.influxdb.write_points([point])
```

### 4.3) 벌크 데이터 마이그레이션
```python
# 과거 데이터 배치 이전
def migrate_historical_data():
    batch_size = 10000
    start_date = datetime(2024, 1, 1)
    end_date = datetime.now()
    
    current = start_date
    while current < end_date:
        batch_end = current + timedelta(hours=1)
        
        # PostgreSQL에서 데이터 추출
        rows = fetch_postgres_data(current, batch_end)
        
        # InfluxDB로 배치 삽입
        points = convert_to_influx_points(rows)
        influx_client.write_points(points)
        
        current = batch_end
        print(f"Migrated data up to {current}")
```

### 4.4) 검증 및 전환
```python
# 데이터 정합성 검증
def validate_migration():
    # 레코드 수 비교
    pg_count = postgres.execute("SELECT COUNT(*) FROM metrics WHERE date >= %s", [date])
    influx_count = influx.query(f"SELECT COUNT(*) FROM metrics WHERE time >= '{date}'")
    
    # 통계값 비교
    pg_stats = postgres.execute("SELECT AVG(cpu), MAX(memory) FROM metrics WHERE date >= %s", [date])
    influx_stats = influx.query(f"SELECT MEAN(cpu), MAX(memory) FROM metrics WHERE time >= '{date}'")
    
    return pg_count == influx_count and pg_stats == influx_stats
```

---

## 5) 성능 비교 및 측정

### 저장 공간 효율성
```
데이터셋: 1억 개 시계열 포인트 (30일)

PostgreSQL (RDS):
- 원시 데이터: 15GB
- 인덱스: 8GB  
- 총 용량: 23GB

InfluxDB:
- 압축 후: 2.3GB
- 메타데이터: 0.5GB
- 총 용량: 2.8GB

압축률: 87.8% 절약
```

### 쿼리 성능
```sql
-- 시간 범위 집계 쿼리 성능 비교

-- PostgreSQL (RDS)
SELECT DATE_TRUNC('hour', timestamp), AVG(value)
FROM metrics 
WHERE timestamp BETWEEN '2024-01-01' AND '2024-01-31'
GROUP BY DATE_TRUNC('hour', timestamp);
-- 실행시간: 45초

-- InfluxDB  
SELECT MEAN(value) FROM metrics 
WHERE time >= '2024-01-01T00:00:00Z' AND time <= '2024-01-31T23:59:59Z'
GROUP BY time(1h);
-- 실행시간: 1.2초

성능 향상: 37.5배
```

---

## 6) 운영 관리 자동화

### 데이터 보존 정책
```sql
-- TimescaleDB 보존 정책
SELECT add_retention_policy('metrics', INTERVAL '90 days');

-- InfluxDB 보존 정책  
CREATE RETENTION POLICY "30_days" ON "mydb" DURATION 30d REPLICATION 1 DEFAULT;
```

### 다운샘플링 정책
```sql
-- TimescaleDB 연속 집계
CREATE MATERIALIZED VIEW metrics_hourly
WITH (timescaledb.continuous) AS
SELECT time_bucket('1 hour', time) AS bucket,
       device_id,
       AVG(cpu) as avg_cpu,
       MAX(memory) as max_memory
FROM metrics
GROUP BY bucket, device_id;

-- InfluxDB 연속 쿼리
CREATE CONTINUOUS QUERY "cq_mean" ON "mydb"
BEGIN
  SELECT mean("value") INTO "average" FROM "metrics" 
  GROUP BY time(30m), *
END;
```

---

## 7) 모니터링 및 알림

### Grafana 대시보드 연동
```yaml
# datasource.yml
apiVersion: 1
datasources:
  - name: InfluxDB
    type: influxdb
    url: http://localhost:8086
    database: metrics
    user: admin
    password: password
```

### 알림 규칙
```sql
-- 임계치 기반 알림
SELECT mean("cpu") FROM "metrics" 
WHERE time >= now() - 5m 
GROUP BY "host"
HAVING mean("cpu") > 80
```

---

## 8) 실무 팁 & 베스트 프랙티스

### 마이그레이션 팁
- **점진적 전환**: 듀얼 라이트로 리스크 최소화
- **검증 자동화**: 데이터 정합성 체크 스크립트 구축  
- **롤백 계획**: 문제 발생 시 즉시 되돌릴 수 있는 절차 마련
- **성능 테스트**: 실제 워크로드로 충분한 부하 테스트

### 운영 팁  
- **카디널리티 관리**: 태그 조합 수를 모니터링하여 메모리 사용량 제어
- **백업 전략**: 시계열 DB의 백업/복원 방식 이해 및 자동화
- **모니터링**: 시계열 DB 자체의 성능 메트릭 수집

### 안티패턴
- **빅뱅 마이그레이션**: 한 번에 모든 데이터를 이전하는 위험한 접근
- **스키마 미최적화**: RDB 스키마를 그대로 시계열 DB에 적용
- **보존정책 부재**: 무제한 데이터 축적으로 인한 비용 증가

---

## 9) 비용 분석

### TCO(Total Cost of Ownership) 비교
```
1년 기준 (중간 규모 워크로드)

RDS PostgreSQL:
- 인스턴스: $2,400/년 (db.r5.xlarge)
- 스토리지: $1,800/년 (15TB)
- I/O 비용: $600/년
- 총 비용: $4,800/년

InfluxDB Cloud:
- 데이터 저장: $480/년 (2TB 압축 후)
- 쿼리 비용: $360/년
- 총 비용: $840/년

절약액: $3,960/년 (82.5% 절약)
```

---

## 10) 마이그레이션 체크리스트

### 사전 준비
- [ ] 현재 데이터 볼륨 및 증가율 분석
- [ ] 쿼리 패턴 및 성능 요구사항 파악  
- [ ] 시계열 DB 옵션 검토 및 선택
- [ ] 마이그레이션 일정 및 리소스 계획

### 구현 단계
- [ ] 개발/테스트 환경 구축
- [ ] 듀얼 라이트 구현 및 테스트
- [ ] 벌크 마이그레이션 스크립트 개발
- [ ] 데이터 검증 자동화 구축

### 운영 전환
- [ ] 성능 테스트 및 튜닝
- [ ] 모니터링 및 알림 설정
- [ ] 백업/복원 절차 확립
- [ ] 팀 교육 및 문서화

---

## 11) 관련 도구 및 리소스

### 마이그레이션 도구
- **Telegraf**: 다양한 소스에서 시계열 DB로 데이터 수집
- **Apache Kafka**: 실시간 데이터 스트리밍 플랫폼  
- **TimescaleDB Parallel Copy**: PostgreSQL → TimescaleDB 마이그레이션
- **InfluxDB Line Protocol**: 고성능 데이터 입력 포맷

### 모니터링 스택
- **Grafana**: 시계열 데이터 시각화
- **Prometheus**: 메트릭 수집 및 알림
- **Chronograf**: InfluxDB 전용 관리 인터페이스

---

## 12) 함께 보면 좋은 문서
- CS/Database/time_series_data.md (시계열 데이터 기초 개념)
- CS/Database/database_replication.md (데이터베이스 복제 전략)
- CS/Database/nosql_databases.md (NoSQL 데이터베이스 개념)
- Interview/logs_and_metrics.md (로그 및 메트릭 모니터링)

---

## 13) 요약
- RDS에서 시계열 전용 데이터베이스로의 전환은 저장 공간 87% 절약, 쿼리 성능 37배 향상, 운영 비용 82% 절감의 효과를 가져올 수 있다. 점진적 마이그레이션 전략을 통해 리스크를 최소화하고, 자동화된 보존 정책과 다운샘플링으로 운영 부담을 크게 줄일 수 있다. 성공적인 전환을 위해서는 철저한 사전 분석, 듀얼 라이트 패턴 적용, 그리고 지속적인 검증이 핵심이다.