# 다익스트라 알고리즘 (Dijkstra's Algorithm)

다익스트라 알고리즘은 그래프에서 한 정점에서 다른 모든 정점까지의 최단 경로를 찾는 알고리즘입니다. 이 알고리즘은 에츠허르 다익스트라(Edsger W. Dijkstra)가 1956년에 고안했으며, 음수 가중치가 없는 그래프에서 효율적으로 작동합니다.

## 1. 기본 개념

다익스트라 알고리즘은 다음과 같은 단계로 진행됩니다:
1. 시작 정점을 선택하고 해당 정점까지의 거리를 0으로 초기화합니다.
2. 나머지 모든 정점까지의 거리는 무한대로 초기화합니다.
3. 방문하지 않은 정점 중에서 최단 거리를 가진 정점을 선택합니다.
4. 선택한 정점의 인접 정점들에 대해, 현재까지의 거리와 해당 간선을 통해 이동하는 거리의 합을 계산합니다.
5. 계산한 거리가 기존에 알려진 거리보다 작으면 거리 값을 업데이트합니다.
6. 모든 정점을 방문할 때까지 3-5 단계를 반복합니다.

## 2. Java 구현

### 기본 구현 (배열 사용)

```java
/**
 * 다익스트라 알고리즘의 기본 구현
 * 인접 행렬을 사용하여 그래프를 표현
 */
public class DijkstraBasic {
    /**
     * 다익스트라 알고리즘을 수행하는 메소드
     * @param graph 인접 행렬로 표현된 그래프 (graph[i][j]는 정점 i에서 j로 가는 간선의 가중치)
     * @param start 시작 정점
     * @return 시작 정점에서 각 정점까지의 최단 거리 배열
     */
    public static int[] dijkstra(int[][] graph, int start) {
        int n = graph.length;  // 정점의 개수
        int[] distance = new int[n];  // 최단 거리를 저장할 배열
        boolean[] visited = new boolean[n];  // 방문 여부를 저장할 배열
        
        // 거리 배열 초기화
        for (int i = 0; i < n; i++) {
            distance[i] = Integer.MAX_VALUE;  // 모든 거리를 무한대로 초기화
        }
        distance[start] = 0;  // 시작 정점까지의 거리는 0
        
        // 모든 정점에 대해 반복
        for (int count = 0; count < n - 1; count++) {
            // 방문하지 않은 정점 중 최단 거리를 가진 정점 찾기
            int minDistance = Integer.MAX_VALUE;
            int minIndex = -1;
            
            for (int v = 0; v < n; v++) {
                if (!visited[v] && distance[v] < minDistance) {
                    minDistance = distance[v];
                    minIndex = v;
                }
            }
            
            // 최단 거리 정점을 방문 처리
            visited[minIndex] = true;
            
            // 선택한 정점의 인접 정점들의 거리 업데이트
            for (int v = 0; v < n; v++) {
                // 방문하지 않았고, 간선이 존재하며, 시작 정점에서 minIndex를 거쳐 v로 가는 경로가 더 짧은 경우
                if (!visited[v] && 
                    graph[minIndex][v] != 0 && 
                    distance[minIndex] != Integer.MAX_VALUE && 
                    distance[minIndex] + graph[minIndex][v] < distance[v]) {
                    
                    distance[v] = distance[minIndex] + graph[minIndex][v];
                }
            }
        }
        
        return distance;
    }
    
    /**
     * 다익스트라 알고리즘 사용 예시
     */
    public static void main(String[] args) {
        // 예제 그래프 (인접 행렬)
        int[][] graph = {
            {0, 4, 0, 0, 0, 0, 0, 8, 0},
            {4, 0, 8, 0, 0, 0, 0, 11, 0},
            {0, 8, 0, 7, 0, 4, 0, 0, 2},
            {0, 0, 7, 0, 9, 14, 0, 0, 0},
            {0, 0, 0, 9, 0, 10, 0, 0, 0},
            {0, 0, 4, 14, 10, 0, 2, 0, 0},
            {0, 0, 0, 0, 0, 2, 0, 1, 6},
            {8, 11, 0, 0, 0, 0, 1, 0, 7},
            {0, 0, 2, 0, 0, 0, 6, 7, 0}
        };
        
        int[] distances = dijkstra(graph, 0);  // 정점 0에서 시작
        
        // 결과 출력
        System.out.println("정점 0에서 각 정점까지의 최단 거리:");
        for (int i = 0; i < distances.length; i++) {
            System.out.println("정점 " + i + ": " + distances[i]);
        }
    }
}
```

### 최적화된 구현 (우선순위 큐 사용)

```java
import java.util.*;

/**
 * 다익스트라 알고리즘의 최적화된 구현
 * 인접 리스트와 우선순위 큐를 사용
 */
public class DijkstraOptimized {
    // 간선을 표현하는 클래스
    static class Edge {
        int destination;  // 도착 정점
        int weight;       // 가중치
        
        public Edge(int destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }
    }
    
    // 우선순위 큐에서 사용할 노드 클래스
    static class Node implements Comparable<Node> {
        int vertex;    // 정점 번호
        int distance;  // 시작 정점으로부터의 거리
        
        public Node(int vertex, int distance) {
            this.vertex = vertex;
            this.distance = distance;
        }
        
        // 거리를 기준으로 노드 비교 (우선순위 큐에서 사용)
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.distance, other.distance);
        }
    }
    
    /**
     * 다익스트라 알고리즘을 수행하는 메소드 (우선순위 큐 사용)
     * @param graph 인접 리스트로 표현된 그래프
     * @param start 시작 정점
     * @param n 정점의 개수
     * @return 시작 정점에서 각 정점까지의 최단 거리 배열
     */
    public static int[] dijkstra(List<List<Edge>> graph, int start, int n) {
        int[] distance = new int[n];  // 최단 거리를 저장할 배열
        Arrays.fill(distance, Integer.MAX_VALUE);  // 모든 거리를 무한대로 초기화
        distance[start] = 0;  // 시작 정점까지의 거리는 0
        
        // 우선순위 큐 생성 (거리가 짧은 노드가 먼저 나옴)
        PriorityQueue<Node> pq = new PriorityQueue<>();
        pq.add(new Node(start, 0));
        
        while (!pq.isEmpty()) {
            Node current = pq.poll();  // 현재 최단 거리를 가진 노드 추출
            int u = current.vertex;
            int dist = current.distance;
            
            // 이미 처리된 노드라면 무시
            if (dist > distance[u]) {
                continue;
            }
            
            // 현재 노드의 인접 노드들을 확인
            for (Edge edge : graph.get(u)) {
                int v = edge.destination;
                int weight = edge.weight;
                
                // 더 짧은 경로를 발견한 경우 거리 업데이트
                if (distance[u] + weight < distance[v]) {
                    distance[v] = distance[u] + weight;
                    pq.add(new Node(v, distance[v]));
                }
            }
        }
        
        return distance;
    }
    
    /**
     * 다익스트라 알고리즘 사용 예시 (우선순위 큐 사용)
     */
    public static void main(String[] args) {
        int n = 9;  // 정점의 개수
        
        // 인접 리스트로 그래프 표현
        List<List<Edge>> graph = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            graph.add(new ArrayList<>());
        }
        
        // 간선 추가 (양방향)
        addEdge(graph, 0, 1, 4);
        addEdge(graph, 0, 7, 8);
        addEdge(graph, 1, 2, 8);
        addEdge(graph, 1, 7, 11);
        addEdge(graph, 2, 3, 7);
        addEdge(graph, 2, 5, 4);
        addEdge(graph, 2, 8, 2);
        addEdge(graph, 3, 4, 9);
        addEdge(graph, 3, 5, 14);
        addEdge(graph, 4, 5, 10);
        addEdge(graph, 5, 6, 2);
        addEdge(graph, 6, 7, 1);
        addEdge(graph, 6, 8, 6);
        addEdge(graph, 7, 8, 7);
        
        int[] distances = dijkstra(graph, 0, n);  // 정점 0에서 시작
        
        // 결과 출력
        System.out.println("정점 0에서 각 정점까지의 최단 거리 (우선순위 큐 사용):");
        for (int i = 0; i < distances.length; i++) {
            System.out.println("정점 " + i + ": " + distances[i]);
        }
    }
    
    /**
     * 그래프에 간선을 추가하는 헬퍼 메소드
     */
    private static void addEdge(List<List<Edge>> graph, int source, int destination, int weight) {
        graph.get(source).add(new Edge(destination, weight));
        graph.get(destination).add(new Edge(source, weight));  // 양방향 그래프의 경우
    }
}
```

## 3. 특징

- **시간 복잡도**:
  - 기본 구현: O(V²), 여기서 V는 정점의 개수입니다.
  - 우선순위 큐 구현: O((V+E) log V), 여기서 E는 간선의 개수입니다.

- **공간 복잡도**:
  - 기본 구현: O(V²) - 인접 행렬 사용 시
  - 우선순위 큐 구현: O(V+E) - 인접 리스트 사용 시

- **장점**:
  - 음수 가중치가 없는 그래프에서 효율적으로 최단 경로를 찾습니다.
  - 우선순위 큐를 사용하면 성능이 크게 향상됩니다.
  - 실제 응용 프로그램에서 널리 사용됩니다.

- **단점**:
  - 음수 가중치가 있는 그래프에서는 작동하지 않습니다.
  - 모든 정점 쌍 간의 최단 경로를 찾는 데는 플로이드-워셜 알고리즘이 더 효율적일 수 있습니다.

## 4. 응용 분야

다익스트라 알고리즘은 다양한 분야에서 활용됩니다:

1. **네트워크 라우팅**: 데이터 패킷이 네트워크를 통해 가장 효율적으로 이동하는 경로를 찾는 데 사용됩니다.
2. **지도 및 내비게이션 시스템**: 두 지점 간의 최단 경로를 찾는 데 사용됩니다.
3. **로봇 경로 계획**: 로봇이 장애물을 피해 목적지까지 최적의 경로를 찾는 데 활용됩니다.
4. **전화 네트워크**: 통화 라우팅 최적화에 사용됩니다.
5. **사회 네트워크 분석**: 사용자 간의 최단 연결 경로를 찾는 데 활용됩니다.

## 5. 변형 알고리즘

### 5.1 양방향 다익스트라 (Bidirectional Dijkstra)

시작점과 도착점에서 동시에 탐색을 시작하여 중간에서 만나는 지점을 찾는 방식입니다. 이 방법은 단일 출발점-단일 도착점 문제에서 효율적입니다.

```java
// 양방향 다익스트라 알고리즘의 개념적 구현
public static int bidirectionalDijkstra(List<List<Edge>> graph, List<List<Edge>> reverseGraph, int start, int end, int n) {
    // 시작점에서의 거리
    int[] distanceFromStart = new int[n];
    Arrays.fill(distanceFromStart, Integer.MAX_VALUE);
    distanceFromStart[start] = 0;
    
    // 도착점에서의 거리 (역방향 그래프 사용)
    int[] distanceFromEnd = new int[n];
    Arrays.fill(distanceFromEnd, Integer.MAX_VALUE);
    distanceFromEnd[end] = 0;
    
    // 방문 여부
    boolean[] visitedFromStart = new boolean[n];
    boolean[] visitedFromEnd = new boolean[n];
    
    // 우선순위 큐
    PriorityQueue<Node> queueFromStart = new PriorityQueue<>();
    PriorityQueue<Node> queueFromEnd = new PriorityQueue<>();
    
    queueFromStart.add(new Node(start, 0));
    queueFromEnd.add(new Node(end, 0));
    
    int shortestPath = Integer.MAX_VALUE;
    
    while (!queueFromStart.isEmpty() && !queueFromEnd.isEmpty()) {
        // 시작점에서 한 단계 진행
        processStep(graph, queueFromStart, distanceFromStart, visitedFromStart);
        
        // 도착점에서 한 단계 진행
        processStep(reverseGraph, queueFromEnd, distanceFromEnd, visitedFromEnd);
        
        // 두 탐색이 만나는 지점 확인
        for (int i = 0; i < n; i++) {
            if (distanceFromStart[i] != Integer.MAX_VALUE && distanceFromEnd[i] != Integer.MAX_VALUE) {
                shortestPath = Math.min(shortestPath, distanceFromStart[i] + distanceFromEnd[i]);
            }
        }
    }
    
    return shortestPath;
}

// 다익스트라 알고리즘의 한 단계를 처리하는 헬퍼 메소드
private static void processStep(List<List<Edge>> graph, PriorityQueue<Node> queue, int[] distance, boolean[] visited) {
    Node current = queue.poll();
    int u = current.vertex;
    
    if (visited[u]) return;
    visited[u] = true;
    
    for (Edge edge : graph.get(u)) {
        int v = edge.destination;
        int weight = edge.weight;
        
        if (!visited[v] && distance[u] + weight < distance[v]) {
            distance[v] = distance[u] + weight;
            queue.add(new Node(v, distance[v]));
        }
    }
}
```

### 5.2 A* 알고리즘

다익스트라 알고리즘의 확장으로, 휴리스틱 함수를 사용하여 목표 정점에 더 빨리 도달할 수 있는 경로를 우선적으로 탐색합니다.

## 6. 주의사항

1. **음수 가중치**: 다익스트라 알고리즘은 음수 가중치가 있는 그래프에서는 정확한 결과를 보장하지 않습니다. 이런 경우에는 벨만-포드 알고리즘을 사용해야 합니다.
2. **무한대 표현**: `Integer.MAX_VALUE`를 사용하여 무한대를 표현할 때, 오버플로우에 주의해야 합니다.
3. **메모리 사용**: 큰 그래프에서는 인접 행렬보다 인접 리스트를 사용하는 것이 메모리 효율성 측면에서 유리합니다.
4. **최적화**: 목적지가 정해진 경우, 해당 정점에 도달하면 알고리즘을 조기 종료할 수 있습니다.

## 7. 결론

다익스트라 알고리즘은 그래프에서 최단 경로를 찾는 가장 기본적이고 중요한 알고리즘 중 하나입니다. 우선순위 큐를 사용한 최적화된 구현은 많은 실제 응용 프로그램에서 효율적으로 사용됩니다. 그러나 음수 가중치가 있는 그래프에서는 벨만-포드 알고리즘이나 플로이드-워셜 알고리즘과 같은 대안을 고려해야 합니다.