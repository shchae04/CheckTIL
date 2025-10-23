# 플로이드 와샬 알고리즘(Floyd-Warshall Algorithm)이란?

## 1. 한 줄 정의
플로이드 와샬 알고리즘은 그래프의 모든 정점 쌍(pair)에 대한 최단 경로를 구하는 동적계획법(DP) 기반 알고리즘으로, 음수 간선이 포함되어 있어도 작동하며 시간복잡도는 O(V³)이다.

---

## 2. 핵심 개념 3단계 이해

### 2-1. 1단계: 문제 정의
- **최단 경로 문제**: 그래프에서 한 정점에서 다른 모든 정점으로의 최단 거리 구하기
- **다익스트라와의 차이점**:
  - 다익스트라: 한 정점에서 출발하는 최단 경로 (O(E log V) 또는 O(V²))
  - 플로이드 와샬: **모든 정점 쌍의 최단 경로** (O(V³))
  - 음수 간선 처리: 플로이드 와샬 가능, 다익스트라 불가능

### 2-2. 2단계: 동적계획법 아이디어
- **부분 문제**: "정점 k를 경유하는 경로가 직접 경로보다 짧은가?"
- **상태 정의**: `dp[i][j][k]` = 정점 i에서 j로 가는 경로에서, 0부터 k까지의 정점만 경유할 수 있을 때의 최단 거리
- **점화식**: `dp[i][j][k] = min(dp[i][j][k-1], dp[i][k][k-1] + dp[k][j][k-1])`

### 2-3. 3단계: 공간 최적화
- **2D DP 활용**: `dp[i][j]`만으로 업데이트 가능 (k를 순차적으로 처리)
- **업데이트 규칙**: 각 k에 대해 모든 (i, j) 쌍을 갱신
- **초기화**: 인접 행렬로 초기화, 자기 자신은 0, 간선 없으면 무한대

---

## 3. 알고리즘 상세 설명

### 3-1. 의사코드(Pseudocode)

```python
def floyd_warshall(graph):
    """
    graph: n×n 인접 행렬
    dist[i][j]: i에서 j로의 최단 거리
    """
    n = len(graph)
    dist = [row[:] for row in graph]  # 깊은 복사

    # k: 경유할 수 있는 정점 (0 ~ k)
    for k in range(n):
        # i: 출발점
        for i in range(n):
            # j: 도착점
            for j in range(n):
                # i → k → j 경로가 i → j 직접 경로보다 짧은가?
                dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])

    return dist
```

### 3-2. 파이썬 구현

```python
def floyd_warshall_detailed(n, edges):
    """
    n: 정점 개수
    edges: (출발, 도착, 가중치) 리스트
    반환: 최단 거리 행렬, 경로 행렬
    """
    INF = float('inf')

    # 1. 인접 행렬 초기화
    dist = [[INF] * n for _ in range(n)]
    next_vertex = [[None] * n for _ in range(n)]

    # 자기 자신은 0
    for i in range(n):
        dist[i][i] = 0

    # 간선 정보 추가
    for u, v, w in edges:
        dist[u][v] = min(dist[u][v], w)  # 중복 간선 처리
        next_vertex[u][v] = v

    # 2. 플로이드 와샬 알고리즘 실행
    for k in range(n):
        for i in range(n):
            for j in range(n):
                if dist[i][k] != INF and dist[k][j] != INF:
                    if dist[i][k] + dist[k][j] < dist[i][j]:
                        dist[i][j] = dist[i][k] + dist[k][j]
                        next_vertex[i][j] = next_vertex[i][k]

    # 3. 음수 사이클 감지
    for i in range(n):
        if dist[i][i] < 0:
            return None  # 음수 사이클 존재

    return dist, next_vertex

# 4. 경로 복원
def reconstruct_path(next_vertex, start, end):
    """경로 행렬을 이용해 실제 경로 복원"""
    if next_vertex[start][end] is None:
        return []

    path = [start]
    current = start
    while current != end:
        current = next_vertex[current][end]
        path.append(current)

    return path
```

### 3-3. 실제 예제

```python
# 예제: 4개 정점, 5개 간선
n = 4
edges = [
    (0, 1, 1),   # 0 → 1 (가중치 1)
    (0, 3, 4),   # 0 → 3 (가중치 4)
    (1, 2, 2),   # 1 → 2 (가중치 2)
    (2, 3, 1),   # 2 → 3 (가중치 1)
    (3, 2, 3),   # 3 → 2 (가중치 3)
]

dist, next_v = floyd_warshall_detailed(n, edges)

# 결과 출력
print("최단 거리 행렬:")
for row in dist:
    print([f"{d:.0f}" if d != float('inf') else "INF" for d in row])

# 0에서 3으로의 최단 경로
print(f"\n0 → 3 최단 거리: {dist[0][3]}")
print(f"경로: {reconstruct_path(next_v, 0, 3)}")

# 출력 예상:
# 최단 거리 행렬:
# ['0', '1', '3', '4']
# ['INF', '0', '2', '3']
# ['INF', 'INF', '0', '1']
# ['INF', 'INF', '3', '0']
#
# 0 → 3 최단 거리: 4.0
# 경로: [0, 1, 2, 3]
```

---

## 4. 시간 및 공간 복잡도

### 4-1. 시간 복잡도

| 구성 요소 | 복잡도 | 설명 |
|---------|--------|------|
| 초기화 | O(V²) | 인접 행렬 생성 |
| 알고리즘 | O(V³) | 3중 루프 |
| **전체** | **O(V³)** | 지배적 요소는 알고리즘 |

- **V = 정점 개수**
- 각 k에 대해 모든 (i, j) 쌍 검사: V × V × V = V³

### 4-2. 공간 복잡도

| 구조 | 복잡도 | 설명 |
|------|--------|------|
| 거리 행렬 | O(V²) | 모든 정점 쌍 거리 저장 |
| 경로 행렬 | O(V²) | 경로 복원을 위한 다음 정점 정보 |
| **전체** | **O(V²)** | 입력과 출력의 크기 |

---

## 5. 주요 특징 및 활용

### 5-1. 장점

- **음수 간선 처리 가능**: 음의 가중치가 있는 그래프에서도 작동
- **모든 쌍의 최단 경로**: 한 번의 실행으로 모든 정점 쌍의 최단 경로 계산
- **구현 단순**: 3중 루프로 직관적이고 구현이 간단
- **음수 사이클 감지**: 최단 경로 존재 여부 판단 가능

### 5-2. 단점

- **O(V³) 시간복잡도**: 정점이 많으면 매우 느림 (V > 500일 때 비현실적)
- **메모리 요구**: O(V²) 공간 필요 (정점 10,000개이면 100MB 이상)
- **한 출발점 최단경로에 비효율**: 모든 쌍이 필요 없으면 다익스트라가 더 효율적

### 5-3. 실무 활용 사례

| 활용처 | 설명 |
|-------|------|
| **교통/물류** | 모든 도시 간 최단 거리 계산 |
| **네트워크 라우팅** | 라우터 간 최적 경로 선정 |
| **게임 AI** | NPC의 이동 경로 계산 |
| **추천 시스템** | 그래프 기반 유사도 계산 |
| **생물정보학** | 단백질 상호작용 네트워크 분석 |

---

## 6. 다른 알고리즘과의 비교

### 6-1. 시간 복잡도 비교

```python
# 같은 그래프에서 모든 정점 쌍의 최단 경로 구하기

# 1. 다익스트라를 V번 실행
# 시간복잡도: O(V × (E log V)) = O(VE log V)
# 정점 500개, 간선 50,000개라면:
# O(500 × 50,000 × log(500)) ≈ O(1.2억) ← 느림

# 2. 플로이드 와샬
# 시간복잡도: O(V³) = O(500³) ≈ O(1.25억) ← 상대적으로 빠름
# 코드 간결함 + 음수 간선 지원
```

### 6-2. 선택 기준

| 상황 | 추천 알고리즘 |
|------|------------|
| **정점 < 500, 모든 쌍** | 플로이드 와샬 |
| **정점 > 1000, 한 출발점** | 다익스트라 |
| **음수 간선 있음** | 벨만-포드 또는 플로이드 와샬 |
| **희소 그래프(간선 적음)** | 다익스트라 (V번 실행) |
| **밀집 그래프(간선 많음)** | 플로이드 와샬 |

---

## 7. 심화: 최적화 및 변형

### 7-1. 비트마스킹을 이용한 최적화

```python
def floyd_warshall_bitmask(n, edges, k):
    """
    상태 압축 DP 버전
    정점을 비트마스킹으로 표현하여 메모리 절약
    (작은 그래프에만 사용 가능, V <= 20)
    """
    INF = float('inf')

    # dist[mask][i][j] =
    # mask에 표현된 정점들만 경유 가능할 때 i→j 최단 거리
    dp = [[[INF] * n for _ in range(n)] for _ in range(1 << n)]

    # 초기화
    for i in range(n):
        dp[0][i][i] = 0

    for u, v, w in edges:
        dp[0][u][v] = min(dp[0][u][v], w)

    # DP
    for mask in range(1 << n):
        for i in range(n):
            for j in range(n):
                if dp[mask][i][j] == INF:
                    continue
                for k in range(n):
                    if mask & (1 << k):
                        continue
                    new_mask = mask | (1 << k)
                    new_dist = dp[mask][i][j] + dp[mask][j][k]
                    dp[new_mask][i][j] = min(dp[new_mask][i][j], new_dist)

    return dp[(1 << n) - 1]
```

### 7-2. 전이 폐포(Transitive Closure)

```python
def transitive_closure(n, edges):
    """
    어떤 정점에서 다른 정점으로 갈 수 있는가?
    (가중치 아니라 도달 가능 여부만 필요할 때)
    """
    # 부울 플로이드 와샬
    reach = [[False] * n for _ in range(n)]

    # 초기화
    for i in range(n):
        reach[i][i] = True

    for u, v, _ in edges:
        reach[u][v] = True

    # OR 연산으로 업데이트
    for k in range(n):
        for i in range(n):
            for j in range(n):
                reach[i][j] = reach[i][j] or (reach[i][k] and reach[k][j])

    return reach
```

---

## 8. 예상 면접 질문

### 8-1. 기술적 질문

1. **플로이드 와샬의 점화식을 설명해주세요.**
   - 답: `dist[i][j] = min(dist[i][j], dist[i][k] + dist[k][j])`
   - k번째 정점을 경유하는 경로와 직접 경로 중 최솟값

2. **다익스트라와 플로이드 와샬의 차이점은?**
   - 답: 다익스트라는 한 출발점 최단경로(O(ElogV)), 플로이드는 모든 쌍 최단경로(O(V³))
   - 플로이드는 음수 간선 지원

3. **음수 사이클 감지 원리는?**
   - 답: 알고리즘 종료 후 `dist[i][i] < 0`인 정점 존재 시 음수 사이클 존재

### 8-2. 최적화 질문

1. **메모리를 줄일 수 있는 방법은?**
   - 답: 1D DP 불가능 (k 의존성), 하지만 필요한 쌍만 계산 가능

2. **병렬 처리가 가능한가?**
   - 답: k 루프는 순차적이어야 하지만, i, j 루프는 병렬 가능

3. **정점이 10,000개라면?**
   - 답: O(V³) = 10¹²번 연산 → 비현실적
   - 대안: 희소 그래프면 다익스트라 V번, 필요한 쌍만 계산

---

## 9. 핵심 요약

### 9-1. 언제 사용하는가?

- ✅ **정점 수 적음** (< 500)
- ✅ **모든 정점 쌍의 최단경로 필요**
- ✅ **음수 간선 있음**
- ✅ **구현 단순성 우선**

### 9-2. 백엔드 개발자 관점

- **알고리즘 선택의 핵심**: 입력 규모와 필요한 정보의 범위
- **메모리와 시간의 트레이드오프**: O(V²) 공간으로 O(V³) 계산 최적화
- **실전**: 캐싱된 거리 행렬로 반복 조회 최적화 가능

### 9-3. 실무 포인트

```python
# 좋은 예: 도시 간 거리 정보 서비스
- 도시 100개 → 플로이드 와샬 1회 계산
- 어떤 도시 쌍도 O(1)에 조회 가능
- 비용 효율적

# 나쁜 예: 도시 100,000개 네트워크
- O(V³) = 10¹⁵번 연산 → 불가능
- 대안: 다익스트라 또는 그래프 분할
```