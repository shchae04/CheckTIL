# 자료구조 그래프(Graph)

## 1. 한 줄 정의
그래프는 정점(Vertex)과 간선(Edge)으로 이루어진 비선형 자료구조로, 객체 간의 관계를 표현하는 네트워크 구조이다.

---

## 2. 그래프의 핵심 개념

### 2-1. 기본 구성 요소
- **정점(Vertex, Node)**: 그래프의 기본 단위, 데이터를 저장하는 노드
- **간선(Edge)**: 정점과 정점을 연결하는 선, 관계를 나타냄
- **차수(Degree)**: 한 정점에 연결된 간선의 수

```python
# 그래프 기본 구조 예시
graph = {
    'A': ['B', 'C'],      # A는 B, C와 연결
    'B': ['A', 'D', 'E'],  # B는 A, D, E와 연결
    'C': ['A', 'F'],
    'D': ['B'],
    'E': ['B', 'F'],
    'F': ['C', 'E']
}
```

### 2-2. 그래프의 종류

#### 방향성에 따른 분류
- **무방향 그래프(Undirected Graph)**: 간선에 방향이 없음 (양방향)
- **방향 그래프(Directed Graph, Digraph)**: 간선에 방향이 있음

```python
# 무방향 그래프
undirected = {
    'A': ['B', 'C'],
    'B': ['A'],  # 양방향
    'C': ['A']
}

# 방향 그래프
directed = {
    'A': ['B', 'C'],  # A → B, A → C
    'B': [],          # B에서 나가는 간선 없음
    'C': ['A']        # C → A
}
```

#### 가중치에 따른 분류
- **가중치 그래프(Weighted Graph)**: 간선에 비용/거리 값이 있음
- **비가중치 그래프(Unweighted Graph)**: 간선에 값이 없음

```python
# 가중치 그래프 (거리, 비용 등)
weighted_graph = {
    'A': [('B', 4), ('C', 2)],  # (목적지, 가중치)
    'B': [('A', 4), ('D', 5)],
    'C': [('A', 2), ('D', 1)],
    'D': [('B', 5), ('C', 1)]
}
```

#### 연결성에 따른 분류
- **연결 그래프(Connected Graph)**: 모든 정점이 경로로 연결
- **비연결 그래프(Disconnected Graph)**: 고립된 정점이 존재
- **완전 그래프(Complete Graph)**: 모든 정점이 서로 연결

### 2-3. 그래프의 표현 방법

#### 인접 행렬(Adjacency Matrix)
- **장점**: 두 정점 간 연결 여부를 O(1)에 확인 가능
- **단점**: V² 크기의 메모리 필요, 희소 그래프에 비효율적

```python
# 인접 행렬 표현 (4개 정점)
# 0: A, 1: B, 2: C, 3: D
adjacency_matrix = [
    [0, 1, 1, 0],  # A → B, C
    [1, 0, 0, 1],  # B → A, D
    [1, 0, 0, 1],  # C → A, D
    [0, 1, 1, 0]   # D → B, C
]

# 연결 확인: O(1)
is_connected = adjacency_matrix[0][1] == 1  # A-B 연결?
```

#### 인접 리스트(Adjacency List)
- **장점**: 메모리 효율적 (V + E), 희소 그래프에 적합
- **단점**: 두 정점 간 연결 확인이 O(V) 시간 소요

```python
# 인접 리스트 표현
adjacency_list = {
    'A': ['B', 'C'],
    'B': ['A', 'D'],
    'C': ['A', 'D'],
    'D': ['B', 'C']
}

# 연결 확인: O(degree)
is_connected = 'B' in adjacency_list['A']  # A-B 연결?
```

### 2-4. 그래프 순회(Traversal)

#### 깊이 우선 탐색(DFS, Depth-First Search)
- 한 경로를 끝까지 탐색 후 백트래킹
- 스택(재귀) 사용
- **시간 복잡도**: O(V + E)

```python
def dfs(graph, start, visited=None):
    if visited is None:
        visited = set()

    visited.add(start)
    print(start, end=' ')

    for neighbor in graph[start]:
        if neighbor not in visited:
            dfs(graph, neighbor, visited)

    return visited

# 실행
graph = {
    'A': ['B', 'C'],
    'B': ['A', 'D', 'E'],
    'C': ['A', 'F'],
    'D': ['B'],
    'E': ['B', 'F'],
    'F': ['C', 'E']
}
dfs(graph, 'A')  # 출력: A B D E F C
```

#### 너비 우선 탐색(BFS, Breadth-First Search)
- 인접한 노드를 먼저 탐색
- 큐 사용
- **시간 복잡도**: O(V + E)

```python
from collections import deque

def bfs(graph, start):
    visited = set()
    queue = deque([start])
    visited.add(start)

    while queue:
        vertex = queue.popleft()
        print(vertex, end=' ')

        for neighbor in graph[vertex]:
            if neighbor not in visited:
                visited.add(neighbor)
                queue.append(neighbor)

# 실행
bfs(graph, 'A')  # 출력: A B C D E F
```

---

## 3. 언어별 구현 특성

### 3-1. Python
```python
# 딕셔너리 기반 인접 리스트
class Graph:
    def __init__(self):
        self.graph = {}

    def add_edge(self, u, v):
        if u not in self.graph:
            self.graph[u] = []
        self.graph[u].append(v)

    def get_neighbors(self, vertex):
        return self.graph.get(vertex, [])

# 사용
g = Graph()
g.add_edge('A', 'B')
g.add_edge('A', 'C')
print(g.get_neighbors('A'))  # ['B', 'C']
```

### 3-2. Java
```java
// ArrayList 기반 인접 리스트
import java.util.*;

class Graph {
    private Map<String, List<String>> adjacencyList;

    public Graph() {
        adjacencyList = new HashMap<>();
    }

    public void addEdge(String u, String v) {
        adjacencyList.putIfAbsent(u, new ArrayList<>());
        adjacencyList.get(u).add(v);
    }

    public List<String> getNeighbors(String vertex) {
        return adjacencyList.getOrDefault(vertex, new ArrayList<>());
    }
}

// 사용
Graph g = new Graph();
g.addEdge("A", "B");
g.addEdge("A", "C");
```

### 3-3. TypeScript
```typescript
// Map 기반 인접 리스트
class Graph<T> {
    private adjacencyList: Map<T, T[]>;

    constructor() {
        this.adjacencyList = new Map();
    }

    addEdge(u: T, v: T): void {
        if (!this.adjacencyList.has(u)) {
            this.adjacencyList.set(u, []);
        }
        this.adjacencyList.get(u)!.push(v);
    }

    getNeighbors(vertex: T): T[] {
        return this.adjacencyList.get(vertex) || [];
    }
}

// 사용
const g = new Graph<string>();
g.addEdge("A", "B");
g.addEdge("A", "C");
```

---

## 4. 사용 사례

### 4-1. 소셜 네트워크
- **친구 관계**: 사용자 간 연결 관계 표현
- **팔로우 시스템**: 방향 그래프로 팔로워/팔로잉 구현

```python
# 소셜 네트워크 예시
social_network = {
    'Alice': ['Bob', 'Charlie'],
    'Bob': ['Alice', 'David'],
    'Charlie': ['Alice', 'David'],
    'David': ['Bob', 'Charlie']
}

# 친구 추천: 공통 친구 찾기
def recommend_friends(network, user):
    friends = set(network[user])
    recommendations = set()

    for friend in friends:
        for friend_of_friend in network[friend]:
            if friend_of_friend != user and friend_of_friend not in friends:
                recommendations.add(friend_of_friend)

    return recommendations
```

### 4-2. 지도/내비게이션
- **최단 경로**: Dijkstra, A* 알고리즘
- **경로 탐색**: BFS/DFS로 도달 가능성 확인

```python
# 도시 간 거리 그래프 (가중치)
city_map = {
    'Seoul': [('Incheon', 40), ('Suwon', 30)],
    'Incheon': [('Seoul', 40)],
    'Suwon': [('Seoul', 30), ('Daejeon', 100)],
    'Daejeon': [('Suwon', 100)]
}
```

### 4-3. 웹 크롤링
- **페이지 링크**: 웹 페이지 간 연결 구조
- **PageRank**: 그래프 알고리즘으로 페이지 순위 결정

### 4-4. 의존성 관리
- **패키지 의존성**: npm, pip 등의 의존성 트리
- **빌드 시스템**: Make, Gradle 등의 작업 의존성
- **위상 정렬**: 의존성 순서 결정

```python
# 패키지 의존성 그래프
dependencies = {
    'app': ['db', 'auth'],
    'db': ['config'],
    'auth': ['config', 'crypto'],
    'config': [],
    'crypto': []
}

# 위상 정렬로 빌드 순서 결정
def topological_sort(graph):
    visited = set()
    stack = []

    def dfs(node):
        visited.add(node)
        for neighbor in graph.get(node, []):
            if neighbor not in visited:
                dfs(neighbor)
        stack.append(node)

    for node in graph:
        if node not in visited:
            dfs(node)

    return stack[::-1]  # 역순

# 결과: ['config', 'crypto', 'db', 'auth', 'app']
```

---

## 5. 백엔드 개발자 관점의 중요성

### 5-1. 데이터베이스 설계
- **관계 모델링**: 엔티티 간 관계를 그래프로 표현
- **조인 최적화**: 그래프 순회로 쿼리 경로 최적화
- **그래프 DB**: Neo4j, Amazon Neptune 등

```sql
-- 소셜 그래프 쿼리 (Neo4j Cypher)
MATCH (user:User {name: 'Alice'})-[:FRIEND]->(friend)-[:FRIEND]->(fof)
WHERE NOT (user)-[:FRIEND]->(fof) AND user <> fof
RETURN DISTINCT fof.name AS recommendation
```

### 5-2. API 설계
- **GraphQL**: 그래프 구조의 쿼리 언어
- **REST 관계**: 리소스 간 연결 관계 표현

### 5-3. 시스템 아키텍처
- **서비스 의존성**: 마이크로서비스 간 의존 관계
- **분산 시스템**: 노드 간 통신 네트워크
- **로드 밸런싱**: 서버 간 연결 구조

```python
# 마이크로서비스 의존성 그래프
service_dependencies = {
    'api-gateway': ['auth-service', 'user-service'],
    'auth-service': ['database'],
    'user-service': ['database', 'cache'],
    'database': [],
    'cache': []
}
```

### 5-4. 네트워크 라우팅
- **네트워크 토폴로지**: 서버/라우터 간 연결
- **최단 경로**: 패킷 라우팅 최적화

---

## 6. 핵심 알고리즘

### 6-1. 최단 경로 알고리즘

#### Dijkstra 알고리즘
- 가중치 그래프에서 최단 경로 찾기
- **시간 복잡도**: O((V + E) log V) (우선순위 큐 사용 시)

```python
import heapq

def dijkstra(graph, start):
    distances = {node: float('inf') for node in graph}
    distances[start] = 0
    pq = [(0, start)]  # (거리, 노드)

    while pq:
        current_dist, current = heapq.heappop(pq)

        if current_dist > distances[current]:
            continue

        for neighbor, weight in graph[current]:
            distance = current_dist + weight

            if distance < distances[neighbor]:
                distances[neighbor] = distance
                heapq.heappush(pq, (distance, neighbor))

    return distances

# 사용
graph = {
    'A': [('B', 4), ('C', 2)],
    'B': [('C', 1), ('D', 5)],
    'C': [('D', 8)],
    'D': []
}
print(dijkstra(graph, 'A'))  # {'A': 0, 'B': 4, 'C': 2, 'D': 9}
```

### 6-2. 사이클 탐지
```python
def has_cycle(graph):
    visited = set()
    rec_stack = set()

    def dfs(node):
        visited.add(node)
        rec_stack.add(node)

        for neighbor in graph.get(node, []):
            if neighbor not in visited:
                if dfs(neighbor):
                    return True
            elif neighbor in rec_stack:
                return True

        rec_stack.remove(node)
        return False

    for node in graph:
        if node not in visited:
            if dfs(node):
                return True

    return False
```

---

## 7. 핵심 요약

| 특성 | 그래프 | 트리 | 리스트 |
|------|--------|------|--------|
| **구조** | 비선형, 네트워크 | 비선형, 계층 | 선형 |
| **사이클** | 가능 | 불가능 | 불가능 |
| **루트** | 없음 | 1개 | 없음 |
| **간선 수** | 자유 | V-1개 | V-1개 |
| **관계** | 다대다 | 일대다 | 순차 |
| **표현** | 인접 행렬/리스트 | 노드와 포인터 | 배열/연결 리스트 |
| **탐색** | BFS, DFS | 전위/중위/후위 | 순차 탐색 |

### 7-1. 선택 기준
- **네트워크 관계를 표현해야 하면** → 그래프
- **계층 구조를 표현해야 하면** → 트리
- **순서가 중요한 일렬 데이터면** → 리스트
- **최단 경로를 찾아야 하면** → 가중치 그래프 + Dijkstra
- **의존성 순서를 결정해야 하면** → 방향 그래프 + 위상 정렬

### 7-2. 실무 팁
- **희소 그래프**는 인접 리스트로 표현 (메모리 효율)
- **밀집 그래프**는 인접 행렬로 표현 (빠른 조회)
- **소셜 네트워크**, **추천 시스템**에는 그래프 DB 고려
- **경로 탐색**이 빈번하면 BFS가 최단 경로 보장
- **재귀 깊이 제한** 고려해 DFS는 스택 방식으로 구현
- **순환 참조 방지**를 위해 visited 집합 필수
- **대규모 그래프**는 분산 처리 (Apache Giraph, GraphX)

### 7-3. 시간 복잡도 정리

| 연산 | 인접 행렬 | 인접 리스트 |
|------|-----------|-------------|
| **간선 추가** | O(1) | O(1) |
| **간선 제거** | O(1) | O(V) |
| **간선 확인** | O(1) | O(V) |
| **전체 순회** | O(V²) | O(V + E) |
| **공간 복잡도** | O(V²) | O(V + E) |
