# Vector DB, Vector Store가 왜 필요한가?

## TL;DR
- 임베딩(벡터) 공간에서의 "유사성"으로 데이터를 찾기 위해 필요하다.
- 전통 DB의 인덱스(B-Tree, Hash)는 "정확 일치/범위"엔 강하지만, "의미 기반 근접 검색(semantic similarity)"엔 부적합하다.
- Vector Store는 벡터 + 메타데이터를 저장/검색하는 계층(라이브러리/서비스)이고, Vector DB는 이를 DB 수준에서 확장성/내구성/운영 기능과 함께 제공한다.
- RAG, 시맨틱 검색, 추천/중복 제거, 코드 검색 등에서 필수적이다.

---

## 1) 왜 필요한가
- LLM/임베딩 모델은 텍스트/이미지/코드 등을 고정 길이의 벡터로 변환한다.
- 사용자는 “단어가 같진 않지만 같은 의미”의 결과를 원한다. 예: "휴가 정책" ≈ "연차 제도".
- 이런 의미 기반 검색은 코사인/유클리드 거리로 측정되며, 대규모 데이터셋에서는 근사최근접탐색(ANN) 인덱스(HNSW/IVF/PQ 등)가 필수다.
- 일반 RDB의 인덱스는 이러한 고차원 거리 계산/탐색에 최적화되어 있지 않다.

---

## 2) Vector Store vs Vector DB
- Vector Store
  - 정의: 애플리케이션 레벨에서 임베딩 저장/검색을 담당하는 추상화(라이브러리, 경량 서비스).
  - 예: FAISS, ScaNN, Annoy, LanceDB(Local), Chroma, Elasticsearch/OpenSearch 플러그인 레벨 사용.
  - 장점: 가볍고 빠르게 시작, 개발 워크플로우에 쉽게 통합.
  - 한계: 운영 기능(복제/백업/권한/멀티테넌시/트랜잭션)이 제한적일 수 있음.
- Vector DB
  - 정의: 벡터 필드/인덱스를 1급 시민으로 가진 데이터베이스.
  - 예: Milvus, Weaviate, Pinecone(서비스형), PostgreSQL + pgvector, Elasticsearch/OpenSearch(dense_vector).
  - 장점: 내구성, 확장성, 파티셔닝/복제, 메타데이터 필터와 결합된 검색, 운영툴/권한 모델 제공.
  - 고려사항: 비용/운영 복잡도, 스키마 설계 필요.

선택 기준
- 데이터량이 수백만 이상, 멀티테넌시/권한/운영 요구 → Vector DB 권장.
- 프로토타입, 로컬/단일 노드, 간단한 검색 → Vector Store(+ 로컬 ANN)로 시작.

---

## 3) 기존 DB가 부족한 지점
- 고차원 벡터에 대한 k-NN/ANN 탐색 최적화 부재 혹은 미흡.
- 코사인/내적/유클리드 등 거리 연산 기반 정렬에 특화된 인덱스 부재.
- 대규모에서의 리콜/레이턴시 균형(HNSW, IVF, PQ 파라미터 튜닝)이 어려움.
- 벡터와 스칼라 메타데이터(시간/태그/권한) 동시 필터/정렬 결합 최적화가 필요.

---

## 4) 핵심 기능 체크리스트
- ANN 인덱스: HNSW/IVF/IVF_PQ/Flat, 정규화 옵션(cosine).
- 메타데이터 필터: created_at, tags, tenant_id, lang 등과 결합 검색.
- Upsert/삭제: 문서 업데이트/삭제 전파, tombstone, 재인덱싱 전략.
- Consistency/내구성: 트랜잭션 경계, 스냅샷/백업, 복제.
- 확장성: 샤딩/파티셔닝, 피크 트래픽 대응.
- 관측성: 검색 로그, 리콜/정확도/레이턴시 지표, 히트 분석.

---

## 5) 대표 사용 사례
- RAG(Retrieval-Augmented Generation): 프롬프트에 관련 컨텍스트를 주입하기 위한 Top-K 후보 검색.
- 시맨틱 검색: 고객지원 문서, 위키, Q&A에서 의미 기반 검색.
- 중복/유사 문서 탐지: 클러스터링/근접 탐색으로 유사도 그룹핑.
- 추천/탐색: 사용자/아이템 임베딩으로 유사 아이템 추천, 하이브리드(벡터+BM25) 랭킹.
- 코드 검색/스니펫 재사용: 함수/클래스 임베딩 기반 유사 코드 찾기.

---

## 6) 언제 굳이 쓰지 않아도 되는가
- 데이터가 작고 단어 매칭이 정확한 도메인: 키워드 검색(BM25)만으로 충분.
- 강한 정형 질의/정확 일치가 중요한 OLTP: 전통 인덱스/조인으로 해결.
- 모델 품질이 낮아 임베딩이 구별력을 못 줄 때: 먼저 임베딩 모델/전처리를 개선.

대안
- 로컬 ANN 라이브러리(FAISS/ScaNN) + RDB(메타데이터) 혼합.
- Elasticsearch/OpenSearch의 하이브리드 쿼리(BM25 + dense_vector)로 시작.

---

## 7) 베스트 프랙티스/주의사항
- 임베딩 차원과 모델 버전 고정, 버전 필드 저장(metadata.model_version).
- 정규화(cosine) 여부 일관성 유지, 저장 전 L2 정규화 고려.
- 청크 전략: 200~800 토큰, 겹침 10~20%, 중복 헤더 제거.
- 인덱스 파라미터: HNSW(M=16~64, efConstruction), efSearch 조절로 리콜/레이턴시 트레이드오프.
- 필터 우선/검색 후 재랭킹: 하이브리드 + Cross-Encoder 재랭킹으로 품질 향상.
- 평가: Recall@K, nDCG, MRR, 토큰 절약량 등 지표 정의.

---

## 8) 간단 예시(개념적)
```pseudo
# upsert
store.upsert(
  id="doc:123#5",
  vector=embed(chunk_text),
  payload={
    "doc_id": 123,
    "chunk_no": 5,
    "lang": "ko",
    "created_at": "2025-09-03T23:00:00Z",
    "model_version": "text-embedding-3-large"
  }
)

# query with filter
results = store.query(
  vector=embed("연차 제도는?"),
  top_k=20,
  filter={"lang": "ko", "created_at": {"gte": now()-90d}}
)

# hybrid
results = hybrid_search(
  query_text="휴가 정책",
  bm25_weight=0.3,
  vector_weight=0.7,
  filters={"tenant_id": 42}
)
```

---

## 9) 관련 문서
- SystemArchitecture: ../SystemArchitecture/llm_query_system.md
- Database: good_database_design.md
