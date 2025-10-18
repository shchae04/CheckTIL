# LLM 기반 Query 시스템 설계 가이드

본 문서는 다음 주제를 실무 중심으로 정리한다.
- 벡터로 저장되는 데이터 형태와 인덱싱
- 데이터 웨어하우스 연동 패턴
- 거대한 데이터셋을 LLM에게 “최적으로” 전달하는 전략
- 데이터 스트림과 LLM 컨텍스트 윈도우 한계 관리
- 메타데이터(예: created_at, FK 등) 기반 최적화 쿼리 팁

---

## 1) 전체 아키텍처 개요
- 주요 컴포넌트
  - Ingestion/ETL: 원천 데이터(문서/테이블/이벤트) 수집 → 정규화/청크 분할 → 임베딩 생성.
  - Vector Store: 임베딩 + 메타데이터 저장, 벡터 인덱스(HNSW/IVF/Scalar Quantization) 활용.
  - Data Warehouse: 정형 데이터 연산(집계/조인/필터), 거버넌스/카탈로그/권한 관리.
  - Retrieval Orchestrator: 질의 해석 → 후보 검색(vector/keyword) → 메타데이터 필터 → 랭킹/재랭킹 → 컨텍스트 압축.
  - LLM Serving: 프롬프트 구성, 컨텍스트 윈도우와 토큰 예산 관리, 스트리밍 응답.
- 권장 설계 원칙
  - RAG 우선: 모델 파라미터 업데이트 없이 최신 데이터 반영 가능.
  - 하이브리드 검색: 벡터 + 키워드/구조화 필터(시간/카테고리/권한).
  - 최소 컨텍스트로 최대 정보량: 청크 전략/요약/재랭킹/표 압축.

---

## 2) 벡터 저장 데이터 형태
- 공통 스키마(예: Postgres + pgvector)
  - id: PK
  - embedding: vector(N) — 임베딩 차원 수(N)
  - text/contents: 원문 청크
  - metadata: JSONB — created_at, source, fk, tags, lang, row_id 등
  - created_at: TIMESTAMPTZ — 파티셔닝/필터
  - fk_*: 참조 키(예: 문서/사용자/조직)

예시 스키마 (PostgreSQL + pgvector):
```
CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS doc_chunk (
  id BIGSERIAL PRIMARY KEY,
  doc_id BIGINT NOT NULL,
  chunk_no INT NOT NULL,
  embedding vector(1536) NOT NULL,
  contents TEXT NOT NULL,
  metadata JSONB NOT NULL DEFAULT '{}',
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- HNSW 인덱스 (pgvector 0.6+)
CREATE INDEX IF NOT EXISTS idx_doc_chunk_embedding
  ON doc_chunk USING hnsw (embedding vector_l2_ops);

-- 시간 + 문서별 조회 최적화를 위한 보조 인덱스
CREATE INDEX IF NOT EXISTS idx_doc_chunk_doc_time
  ON doc_chunk (doc_id, created_at DESC);

-- 메타데이터 키 접근을 자주 하면 표현식 인덱스 고려
-- CREATE INDEX ON doc_chunk ((metadata->>'lang'));
```

쿼리 예시: 시간/도메인 필터 후 근사 최근접
```
SELECT id, doc_id, chunk_no, contents
FROM doc_chunk
WHERE created_at >= NOW() - INTERVAL '90 days'
  AND (metadata->>'lang') = 'ko'
ORDER BY embedding <-> :query_embedding
LIMIT 20;
```

- 기타 VectorDB
  - Milvus/Weaviate: 컬렉션 스키마에 벡터 필드 + 스칼라 필드(태그/시간) 정의, IVF_FLAT/HNSW.
  - Elasticsearch/OpenSearch: dense_vector + BM25 혼합(Reciprocal Rank Fusion) 가능.

---

## 3) 데이터 웨어하우스 연동
- 목적: 대용량 정형 데이터 집계/조인/권한을 DWH에서 수행하고, LLM 컨텍스트에는 요약/결과만 전달.
- 연동 패턴
  1) 오프라인 동기화: DWH 테이블 → ETL → 문서화/표 형태로 청크화 → 벡터화 → Vector Store 적재.
  2) 온디맨드 하이브리드: 
     - 1차 검색(vector/keyword)으로 후보 문서/키를 추출
     - 후보 키 기반으로 DWH에서 세부 집계/조인 실행
     - 결과를 표(summary table) 형태로 압축 후 LLM에 전달.
  3) 피쳐/카탈로그 재사용: 메타데이터 스키마를 DWH 데이터 카탈로그(예: BigQuery Data Catalog, Snowflake Tags)와 일치.

- 예: BigQuery/Snowflake/Redshift와의 연계
  - LLM이 직접 DWH에 자유 질의하지 않도록, 승인된 프롬프트→SQL 변환 레이어에서
    - 화이트리스트된 뷰/UDTF만 접근
    - 제한된 파라미터 바인딩
    - LIMIT/샘플링/시간 필터 강제

---

## 4) AI에 ‘거대한 데이터’를 최적으로 전달하기
- 원칙: “최소 토큰으로 최대 정보 밀도”
  1) 청크 전략: 의미 단위 기반 200~800 토큰, 중복 헤더/푸터 제거, 슬라이딩 윈도우는 겹침 10~20% 내외.
  2) 다단계 검색: 
     - Stage1: 대역폭 큰 근사 검색(Top N=200)
     - Stage2: 메타데이터 필터/비즈니스 규칙
     - Stage3: Cross-Encoder 재랭킹(Top K=20)
     - Stage4: 압축 요약(map-reduce, tree, graph) 후 최종 K′=8~12
  3) 표/숫자 데이터는 “요약/통계/피벗”으로 전처리하고 원시 행 전체는 지양.
  4) 스키마/용어집/단위 정의를 별도 섹션으로 제공해 환각 감소.

- 컨텍스트 구성 템플릿
```
<System>
- 사용자 목적: {goal}
- 제약: 정확성 우선, 출처 표기, 추정 금지
- 컨텍스트: {compressed_context_blocks}
- 최종 요청: {task}
</System>
```

---

## 5) 데이터 스트림과 컨텍스트 사이즈 관리
- 스트리밍 전략
  - 서버에서 토큰 스트리밍은 UX를 개선하지만, 입력 컨텍스트 초과는 방지 필요.
  - Long-context 모델이라도 비용이 선형/초과 증가 → 사전 압축 필수.
- 컨텍스트 예산 관리
  - 예산 분배: 시스템/지침 10~20%, 사용자 질문 5~10%, 증거 컨텍스트 60~75%.
  - 토큰 예측: tiktoken류로 사전 추정, 초과 시: 
    1) 덜 관련된 블록 제거
    2) 표를 통계량으로 축약(상위 N, 분위수, 평균/표준편차)
    3) 코드/로그는 스니펫 + 라인 번호만

- 점진적 컨텍스트 주입(Streaming Retrieval)
  - 답변 생성을 시작하고, 필요 시 추가 evidence를 이벤트 스트림으로 주입 → 모델이 보수적으로 업데이트.

---

## 6) 메타데이터 기반 최적화 쿼리
- 시간 필터(created_at): 최신성 가중치(Recency Bias)를 검색에 반영
```
-- 가중 스코어 예시(간단화)
SELECT id, contents,
       (1.0 / (1 + EXTRACT(EPOCH FROM (NOW() - created_at))/86400)) AS recency,
       (embedding <#> :q) AS distance
FROM doc_chunk
WHERE created_at >= NOW() - INTERVAL '365 days'
ORDER BY distance + (-0.2 * recency)  -- recency를 보너스로 반영
LIMIT 20;
```
- 외래키(FK) 기반 필터/조인
```
-- 예: 특정 고객 조직(org_id) 문서만
SELECT *
FROM doc_chunk
WHERE (metadata->>'org_id') = :org_id
ORDER BY embedding <-> :q
LIMIT 20;
```
- 권한/가시성
  - 권한 토큰을 메타데이터에 태깅(role, org_id, project_id). 검색 시 필수 필터.
  - 행 수준 보안(RLS) 또는 뷰를 통해 강제.

- DWH와의 조합
```
-- 후보 키 도출(벡터)
WITH cand AS (
  SELECT doc_id, id
  FROM doc_chunk
  WHERE created_at >= NOW() - INTERVAL '90 days'
  ORDER BY embedding <-> :q
  LIMIT 200
)
-- 세부 집계(DWH)
SELECT d.doc_id, sum(f.amount) AS amt, count(*) AS cnt
FROM cand c
JOIN dw.fact_sales f ON f.doc_id = c.doc_id
GROUP BY d.doc_id;
```

---

## 7) 품질/성능 체크리스트
- [ ] 임베딩 일관성: 모델 버전/차원/전처리 파이프라인 고정 및 기록.
- [ ] 하이브리드 검색: BM25 + Vector 융합 또는 스코어 스태킹.
- [ ] 메타데이터 인덱싱: 시간/조직/문서별 보조 인덱스, 표현식 인덱스.
- [ ] 파티셔닝: created_at(월/분기) 파티션으로 대량 삭제/보관 전략.
- [ ] ETL 재처리: 원본 hash 저장 → 변경 감지 시 해당 청크만 재임베딩.
- [ ] DWH 보호: 제한된 뷰/샌드박스/쿼터/쿼리 가드(LIMIT/타임아웃).
- [ ] 컨텍스트 관리: 토큰 예산 시뮬레이션, 초과 시 압축 파이프라인 자동화.
- [ ] 관측성: 검색 스코어, 선택된 컨텍스트, 최종 답변 링크를 모두 로그/트레이스.

---

## 8) 구현 스니펫 모음
- 임베딩 생성(Python psuedocode)
```
chunks = chunker.split(doc)
embeds = embedder.embed(chunks)
batch_insert(chunks, embeds, metadata)
```

- 검색 + 재랭킹
```
candidates = vector_search(q_embed, top_n=200, filters={org_id})
filtered = meta_filter(candidates, time_range, role)
reranked = cross_encoder.rerank(q_text, filtered, top_k=20)
context = compressor.summarize(reranked, target_tokens=2000)
answer = llm.generate(prompt_template(context, question))
```

---

## 9) 참고
- pgvector: https://github.com/pgvector/pgvector
- Milvus: https://milvus.io/
- Weaviate: https://weaviate.io/
- OpenSearch k-NN: https://opensearch.org/docs/latest/search-plugins/knn/
- RAG Best Practices: https://platform.openai.com/docs/guides/retrieval
- Vector DB/Store 개요(TIL): ../Database/vector_db_vs_vector_store.md

