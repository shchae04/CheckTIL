# 좋아요 수 기반 페이지네이션은 어떻게 구현할까?

## 0. 한눈에 보기(초간단)
- 정렬 기준: like_count DESC, id DESC와 같이 “강한 타이브레이커”를 둔 안정 정렬이 필수.
- 방식: OFFSET/LIMIT 대신 커서(Keyset) 페이지네이션을 사용. 커서는 (last_like_count, last_id).
- SQL 패턴:
  - 첫 페이지: ORDER BY like_count DESC, id DESC LIMIT :size
  - 다음 페이지: WHERE (like_count, id) < (:lastLike, :lastId) ORDER BY like_count DESC, id DESC LIMIT :size (Postgres)
  - MySQL: WHERE like_count < :lastLike OR (like_count = :lastLike AND id < :lastId)
- 인덱스: (like_count, id) 복합 인덱스(가능하면 DESC 포함). Postgres는 DESC 인덱스 지원, MySQL 8도 DESC 인덱스 지원.
- 대규모 피드: Redis ZSET을 활용해 score=like_count(또는 결합 스코어)로 커서링.
- 일관성: like_count는 비정규화(카운터 컬럼) + 원자적 증가/감소. 페이지 간 변경에 따른 중복/누락은 허용 가능한 최종 일관성으로 설계.

---

## 1. 왜 OFFSET/LIMIT는 곤란한가?
- 비용: 큰 OFFSET은 스캔 비용이 커지고 페이지가 뒤로 갈수록 느려짐.
- 순위 변동: 좋아요 수는 실시간으로 바뀜 → OFFSET 기준이 흔들려 중복/누락이 발생.
- 해결: Keyset(커서) 페이지네이션은 정렬 키를 기준으로 다음 범위를 직접 조회해 성능과 정합성이 안정적.

## 2. 정렬 설계: 안정 정렬과 타이브레이커
- 같은 like_count가 많을 수 있으므로 반드시 2차 키를 둬야 함.
- 권장: ORDER BY like_count DESC, id DESC
  - id는 단조 증가하는 PK라고 가정.
  - 이렇게 하면 정렬이 안정되고 커서 비교가 명확해짐.

## 3. SQL 예시와 인덱스
### 3.1 PostgreSQL
- 인덱스
```sql
CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_posts_like_id_desc
ON posts (like_count DESC, id DESC);
```
- 첫 페이지
```sql
SELECT id, title, like_count
FROM posts
ORDER BY like_count DESC, id DESC
LIMIT :size;
```
- 다음 페이지(커서: lastLike, lastId)
```sql
SELECT id, title, like_count
FROM posts
WHERE (like_count, id) < (:lastLike, :lastId)
ORDER BY like_count DESC, id DESC
LIMIT :size;
```
- 이전 페이지(옵션)
  - 첫 아이템 커서를 이용해 역방향 범위 조회 후 앱에서 역순 정렬
```sql
SELECT id, title, like_count
FROM posts
WHERE (like_count, id) > (:firstLike, :firstId)
ORDER BY like_count ASC, id ASC
LIMIT :size; -- 가져온 뒤 클라이언트에서 역순으로 반환
```

### 3.2 MySQL 8+
- 인덱스(가능하면 DESC, 없으면 ASC도 대부분 역방향 스캔으로 활용 가능)
```sql
CREATE INDEX idx_posts_like_id_desc ON posts (like_count DESC, id DESC);
-- 또는 하위 호환
CREATE INDEX idx_posts_like_id ON posts (like_count, id);
```
- 첫 페이지
```sql
SELECT id, title, like_count
FROM posts
ORDER BY like_count DESC, id DESC
LIMIT :size;
```
- 다음 페이지
```sql
SELECT id, title, like_count
FROM posts
WHERE like_count < :lastLike
   OR (like_count = :lastLike AND id < :lastId)
ORDER BY like_count DESC, id DESC
LIMIT :size;
```

## 4. API 커서 설계
- 요청
  - GET /posts?sort=likes&size=20
  - GET /posts?sort=likes&cursor=base64("like=12345&id=9988")&size=20
- 응답 예시
```
{
  "items": [{"id": 9987, "title": "Hello", "likeCount": 12345}],
  "nextCursor": "bGlrZT0xMjM0NSZpZD05OTgy"
}
```
- 커서 내용: lastLike, lastId를 URL-safe Base64로 인코딩.
- 역방향 페이지(옵션): firstLike, firstId를 함께 내려줘 이전 페이지 커서도 제공 가능.

## 5. like_count 유지 전략(일관성)
- 비정규화: posts.like_count 컬럼 유지. 좋아요/취소 시 원자적 갱신
```sql
UPDATE posts SET like_count = like_count + 1 WHERE id = :postId; -- 좋아요
UPDATE posts SET like_count = like_count - 1 WHERE id = :postId AND like_count > 0; -- 취소
```
- 동시성: DB 레벨 원자성(단일 UPDATE) + 낙관적 락(@Version) 또는 조건부 업데이트 적용 가능.
- 재계산: 주기적 배치로 진실원장(likes 테이블)과 like_count를 대사(reconcile)해 드리프트 방지.
- 스냅샷 고려: 엄격한 정합성이 필요하면 ‘조회 시점’ 기준 스냅샷(예: MVCC) 또는 랭킹 스냅샷 테이블/뷰를 사용.

## 6. Redis ZSET을 활용한 대규모 피드
- 기본: ZADD posts:likes {score} {postId}, score=like_count.
- 동점 타이브레이커: 결합 스코어를 사용(64-bit 안전 범위 내 권장)
  - combined = (like_count << 32) | (0xFFFFFFFF - post_id)
  - 이렇게 하면 like_count가 높을수록 크고, 같은 like_count에서는 id가 클수록 작은 값이 되어 ZREVRANGE로 내림차순 정렬 유지.
- 페이징
  - 첫 페이지: ZREVRANGE posts:likes 0 :size-1 WITHSCORES
  - 다음 페이지: 마지막 combinedScore를 커서로 보관 → ZREVRANGEBYSCORE posts:likes (lastCombinedScore -inf LIMIT 0 :size
- 주의
  - ZSET은 score만 정렬 → 동점은 멤버의 사전식으로 처리되므로 결합 스코어가 더 단순.
  - Redis는 최종 일관성 캐시로 사용하고, 원장은 DB. 주기적으로 동기화.

## 7. JPA 예시(커서 방식)
```
// 첫 페이지
TypedQuery<Post> q1 = em.createQuery(
    "SELECT p FROM Post p ORDER BY p.likeCount DESC, p.id DESC", Post.class);
q1.setMaxResults(size);
List<Post> first = q1.getResultList();

// 다음 페이지
TypedQuery<Post> q2 = em.createQuery(
    "SELECT p FROM Post p " +
    "WHERE (p.likeCount < :lastLike) OR (p.likeCount = :lastLike AND p.id < :lastId) " +
    "ORDER BY p.likeCount DESC, p.id DESC", Post.class);
q2.setParameter("lastLike", lastLike);
q2.setParameter("lastId", lastId);
q2.setMaxResults(size);
List<Post> next = q2.getResultList();
```

## 8. 엣지 케이스와 운영 팁
- 첫 페이지: cursor가 없으면 WHERE 조건 없이 LIMIT만 사용.
- 빈 페이지: 반환 아이템 수 < size면 더 없음(endOfList=true) 표시.
- 중복/누락: 페이지 사이에 like_count 변동으로 소폭 발생 가능. UX 관점 허용하거나, 짧은 TTL 스냅샷/캐시로 완화.
- 역방향 페이지: 역정렬 조회 후 앱에서 역전시켜 안정성 확보.
- 인덱스 건강: 통계 갱신(ANALYZE), 필요시 partial index(예: 공개 글만)로 크기/효율 최적화.
- 큰 테이블 마이그레이션: like_count 컬럼 추가 시 백필(batch backfill) + 읽기 경로 점진 전환.

## 9. 면접 한 줄 답변
- 좋아요 순 정렬은 OFFSET 대신 커서(Keyset) 페이지네이션으로 (like_count DESC, id DESC) 정렬과 복합 인덱스를 사용합니다. 다음 페이지는 커서의 (last_like, last_id)보다 작은 범위를 조회해 안정성과 성능을 확보합니다. 대규모에선 Redis ZSET으로 결합 스코어를 써서 커서링하고, like_count는 비정규화 카운터로 원자적으로 유지합니다.

## 10. 참고
- Keyset pagination: PostgreSQL docs, use of tuple comparison
- MySQL 8 Descending Indexes
- Redis Sorted Sets