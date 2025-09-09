# Elasticsearch란 무엇인가요?

## 개요

Elasticsearch는 Apache Lucene 기반의 오픈 소스 분산 검색 및 분석 엔진입니다. 2010년 Shay Banon에 의해 처음 개발되었으며, 현재는 Elastic社에서 개발 및 유지보수하고 있습니다. RESTful API를 제공하며, JSON 문서 형태로 데이터를 저장하고 실시간으로 검색할 수 있는 NoSQL 데이터베이스의 특성도 가지고 있습니다.

## 주요 특징

### 1. 분산 아키텍처
- **클러스터링**: 여러 노드로 구성된 클러스터를 통해 데이터를 분산 저장
- **샤딩(Sharding)**: 인덱스를 여러 샤드로 분할하여 성능 향상
- **복제(Replication)**: 데이터의 복사본을 생성하여 고가용성 보장
- **자동 장애 조치**: 노드 장애 시 자동으로 다른 노드로 작업 이관

### 2. 실시간 검색
- **Near Real-Time (NRT)**: 거의 실시간으로 데이터 인덱싱 및 검색 가능
- **빠른 응답 속도**: 역색인(Inverted Index) 구조를 통한 빠른 검색
- **복합 쿼리**: 다양한 조건을 조합한 복잡한 검색 쿼리 지원

### 3. 스키마리스
- **동적 매핑**: 새로운 필드가 추가될 때 자동으로 스키마 생성
- **유연한 데이터 구조**: JSON 형태의 유연한 문서 구조 지원
- **타입 추론**: 데이터 타입 자동 감지 및 매핑

### 4. RESTful API
- **HTTP 기반**: 표준 HTTP 메서드(GET, POST, PUT, DELETE) 사용
- **JSON 통신**: 요청과 응답 모두 JSON 형태로 처리
- **언어 독립적**: 다양한 프로그래밍 언어에서 쉽게 접근 가능

## 핵심 개념

### 인덱스(Index)
관계형 데이터베이스의 데이터베이스와 유사한 개념으로, 관련된 문서들의 논리적 집합입니다.

```json
PUT /products
{
  "settings": {
    "number_of_shards": 3,
    "number_of_replicas": 1
  },
  "mappings": {
    "properties": {
      "name": { "type": "text" },
      "price": { "type": "double" },
      "created_date": { "type": "date" }
    }
  }
}
```

### 문서(Document)
Elasticsearch의 기본 정보 단위로, JSON 형태로 저장되며 관계형 DB의 행(row)과 유사합니다.

```json
{
  "_index": "products",
  "_type": "_doc",
  "_id": "1",
  "_source": {
    "name": "MacBook Pro",
    "price": 2500000,
    "category": "laptop",
    "created_date": "2024-01-15"
  }
}
```

### 타입(Type)
Elasticsearch 7.0부터 deprecated되어 현재는 `_doc` 타입으로 통일되었습니다.

### 샤드(Shard)
인덱스를 여러 조각으로 나눈 것으로, 데이터를 분산 저장하여 성능을 향상시킵니다.

## 검색 기능

### 전문 검색(Full-text Search)
```json
GET /products/_search
{
  "query": {
    "match": {
      "name": "MacBook Pro"
    }
  }
}
```

### 구조화된 검색
```json
GET /products/_search
{
  "query": {
    "bool": {
      "must": [
        { "range": { "price": { "gte": 1000000, "lte": 3000000 } } },
        { "term": { "category": "laptop" } }
      ]
    }
  }
}
```

### 집계(Aggregations)
```json
GET /products/_search
{
  "size": 0,
  "aggs": {
    "avg_price": {
      "avg": {
        "field": "price"
      }
    },
    "categories": {
      "terms": {
        "field": "category"
      }
    }
  }
}
```

## ELK 스택과의 통합

### ELK 스택 구성
1. **Elasticsearch**: 데이터 저장 및 검색 엔진
2. **Logstash**: 데이터 수집, 변환, 전송 파이프라인
3. **Kibana**: 데이터 시각화 및 관리 도구

### 로그 분석 파이프라인
```
[애플리케이션 로그] → [Logstash] → [Elasticsearch] → [Kibana]
```

**Logstash 설정 예시**:
```ruby
input {
  file {
    path => "/var/log/application.log"
    start_position => "beginning"
  }
}

filter {
  grok {
    match => { "message" => "%{TIMESTAMP_ISO8601:timestamp} %{LOGLEVEL:level} %{GREEDYDATA:message}" }
  }
}

output {
  elasticsearch {
    hosts => ["localhost:9200"]
    index => "application-logs-%{+YYYY.MM.dd}"
  }
}
```

## 사용 사례

### 1. 로그 관리 및 분석
- **중앙집중식 로그 수집**: 분산된 시스템의 로그를 한 곳에서 관리
- **실시간 로그 모니터링**: 실시간으로 오류 및 이상 징후 탐지
- **로그 분석**: 사용자 행동 패턴, 시스템 성능 분석

### 2. 전문 검색 시스템
- **검색 엔진**: 웹사이트 내부 검색 기능 구현
- **문서 관리 시스템**: 대량의 문서에서 빠른 검색 제공
- **제품 카탈로그**: 전자상거래 사이트의 제품 검색

### 3. 실시간 데이터 분석
- **비즈니스 인텔리전스**: 실시간 대시보드 및 리포트 생성
- **성능 모니터링**: 애플리케이션 및 인프라 성능 추적
- **보안 분석**: 보안 이벤트 모니터링 및 분석

### 4. 시계열 데이터 분석
- **메트릭 모니터링**: 시스템 메트릭 수집 및 분석
- **IoT 데이터 처리**: 센서 데이터 실시간 분석
- **금융 데이터 분석**: 주식, 환율 등 시계열 데이터 분석

## 성능 최적화

### 인덱싱 최적화
```json
PUT /_cluster/settings
{
  "persistent": {
    "indices.store.throttle.max_bytes_per_sec": "200mb"
  }
}
```

### 검색 성능 향상
- **필터 사용**: `term`, `range` 등 필터를 활용하여 캐싱 효과 활용
- **필드 데이터 비활성화**: 불필요한 필드의 `doc_values` 비활성화
- **라우팅**: 관련 문서를 같은 샤드에 저장하여 검색 성능 향상

### 메모리 관리
- **힙 메모리 설정**: JVM 힙 크기를 시스템 메모리의 50% 이하로 설정
- **필드 데이터 캐시**: 자주 사용되는 필드 데이터 캐싱

## 보안

### 인증 및 권한 관리
```yaml
xpack.security.enabled: true
xpack.security.transport.ssl.enabled: true
xpack.security.http.ssl.enabled: true
```

### 역할 기반 접근 제어(RBAC)
```json
PUT /_security/role/log_reader
{
  "cluster": ["monitor"],
  "indices": [
    {
      "names": ["logs-*"],
      "privileges": ["read", "view_index_metadata"]
    }
  ]
}
```

## 모니터링

### 클러스터 상태 확인
```bash
GET /_cluster/health
GET /_cat/nodes?v
GET /_cat/indices?v
```

### 성능 메트릭
- **인덱싱 속도**: 초당 처리 가능한 문서 수
- **검색 지연시간**: 쿼리 응답 시간
- **클러스터 상태**: 녹색(정상), 노란색(경고), 빨간색(오류)

## 실제 구축 예시

### Docker Compose를 이용한 ELK 스택 구성
```yaml
version: '3.8'
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    
  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    volumes:
      - ./logstash.conf:/usr/share/logstash/pipeline/logstash.conf
    depends_on:
      - elasticsearch
    
  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    depends_on:
      - elasticsearch
```

## 최신 발전 사항

### Elasticsearch 8.x 새로운 기능
- **벡터 검색**: 의미 검색을 위한 dense_vector 필드 지원 강화
- **Runtime Fields**: 쿼리 시점에 계산되는 동적 필드
- **자동 스케일링**: 클러스터 크기 자동 조정
- **보안 강화**: 기본적으로 보안 기능 활성화

### 머신러닝 통합
- **이상 탐지**: 시계열 데이터에서 이상값 자동 탐지
- **자연어 처리**: BERT 등 NLP 모델 통합
- **추천 시스템**: 사용자 행동 기반 추천 알고리즘

## 대안 기술과의 비교

| 특징 | Elasticsearch | Apache Solr | MongoDB |
|------|---------------|-------------|---------|
| 검색 성능 | 매우 빠름 | 빠름 | 보통 |
| 분산 처리 | 우수 | 우수 | 우수 |
| 학습 곡선 | 보통 | 높음 | 낮음 |
| 커뮤니티 | 활발함 | 활발함 | 활발함 |
| 라이선스 | Elastic License | Apache 2.0 | SSPL |

## 결론

Elasticsearch는 현대적인 검색 및 분석 요구사항을 만족시키는 강력한 도구입니다. 특히 대용량 데이터의 실시간 검색, 로그 분석, 복합적인 쿼리 처리에 뛰어난 성능을 보입니다. ELK 스택의 핵심 구성 요소로서 많은 기업에서 로그 관리, 모니터링, 비즈니스 인텔리전스 용도로 활용되고 있습니다.

하지만 운영의 복잡성, 메모리 사용량, 라이선스 정책 등을 고려하여 프로젝트의 요구사항에 맞는지 신중히 검토한 후 도입하는 것이 중요합니다.