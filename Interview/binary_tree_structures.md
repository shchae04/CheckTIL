# Binary Search Tree, Splay Tree, AVL Tree, Red-Black Tree의 차이점

## 1. 한 줄 정의
Binary Search Tree는 기본 이진 탐색 트리이며, AVL과 Red-Black Tree는 자동 균형을 맞춰 성능을 보장하고, Splay Tree는 최근에 접근한 노드를 루트로 끌어올려 캐시 효율을 최적화하는 자료구조들이다.

---

## 2. 자료구조별 특성 비교

### 2-1. 기본 구조(Structure)
- **Binary Search Tree (BST)**: 왼쪽 < 부모 < 오른쪽 규칙만 만족
- **AVL Tree**: 모든 노드의 높이 차이를 1 이하로 유지 (엄격한 균형)
- **Red-Black Tree**: 색상(빨강/검정)과 규칙으로 대략적 균형 유지
- **Splay Tree**: 특정 규칙 없이 접근된 노드를 루트로 회전

```
BST:        50              AVL:       40          RB Tree:     40(B)
           /  \                      /  \                       /    \
          30  70                   30(R) 50(R)              30(R)  50(B)
         /                        /   \
        10                      20(B) 35(B)

Splay Tree: 접근한 노드가 항상 루트
(동적으로 변화)
```

### 2-2. 삽입/삭제 시간 복잡도
- **BST**: 최악 O(n), 평균 O(log n) (불균형 가능)
- **AVL Tree**: O(log n) 보장 (높이 차이 <= 1 유지)
- **Red-Black Tree**: O(log n) 보장 (색상 규칙으로 균형)
- **Splay Tree**: 상각 O(log n) (최악 O(n)이지만 평균적으로 O(log n))

```
작업 횟수: 1, 10, 100, 1000
BST:        1, 5,  10,  50   (불균형 가능)
AVL:        1, 4,  7,   10   (항상 균형)
RB Tree:    1, 4,  7,   10   (대략 균형)
Splay:      1, 3,  6,   9    (접근 패턴에 따라 변함)
```

### 2-3. 회전(Rotation) 비용
- **BST**: 회전 없음
- **AVL Tree**: 높은 회전 빈도 (모든 삽입/삭제 시 확인)
- **Red-Black Tree**: 낮은 회전 빈도 (색상 변경으로 처리)
- **Splay Tree**: 모든 접근 시 회전

```
삽입 작업 시 회전:
AVL:      높음 (매번 height 확인 필요)
RB Tree:  낮음 (색상만 변경 대부분)
Splay:    매우 높음 (zig-zig, zig-zag 회전)
```

### 2-4. 메모리 오버헤드
- **BST**: 최소 (포인터만 필요)
- **AVL Tree**: 높이 정보 저장 필요
- **Red-Black Tree**: 색상 1비트 + 높이 정보
- **Splay Tree**: 추가 오버헤드 없음 (포인터만)

```
노드 구조:
BST:        [value] → [left] [right]
AVL:        [value] [height] → [left] [right]
RB Tree:    [value] [color] → [left] [right]
Splay:      [value] → [left] [right]
```

### 2-5. 최악의 시나리오 처리
- **BST**: 정렬된 데이터 입력 → O(n) 성능 저하
- **AVL Tree**: 모든 경우 O(log n) 보장
- **Red-Black Tree**: 모든 경우 O(log n) 보장
- **Splay Tree**: 악의적 접근 패턴 → O(n)이지만 드문 경우

```
정렬된 1,2,3,4,5 삽입:
BST:        1                AVL:    3
             \                      /  \
              2                    2    4
               \                  / \    \
                3    vs.         1   2   5

결과: BST는 선형, AVL은 균형
```

### 2-6. 접근 패턴 최적화
- **BST**: 접근 패턴 무시
- **AVL Tree**: 접근 패턴 무시
- **Red-Black Tree**: 접근 패턴 무시
- **Splay Tree**: 최근 접근 노드 자주 접근 시 O(1)에 가까운 성능

```
반복된 접근 (ex. 같은 노드 10번 접근):
BST/AVL/RB: 매번 O(log n)
Splay:      첫 번째 O(log n), 이후 O(1)에 가까움
```

---

## 3. 구현 특성

### 3-1. Binary Search Tree (BST)
- **구현 복잡도**: 낮음 (기본 자료구조)
- **특징**: 삽입/삭제는 간단하지만 균형 유지 안 함
- **사용 언어**: 모든 언어에서 구현 가능

```python
class Node:
    def __init__(self, value):
        self.value = value
        self.left = None
        self.right = None

def insert(node, value):
    if node is None:
        return Node(value)
    if value < node.value:
        node.left = insert(node.left, value)
    else:
        node.right = insert(node.right, value)
    return node
```

### 3-2. AVL Tree
- **구현 복잡도**: 높음 (균형 유지 로직 복잡)
- **특징**: 모든 노드에서 height 확인 및 회전
- **사용 예**: 엄격한 성능이 필요한 경우

```python
class AVLNode:
    def __init__(self, value):
        self.value = value
        self.left = None
        self.right = None
        self.height = 1  # 높이 정보 저장

def get_height(node):
    return node.height if node else 0

def get_balance(node):
    return get_height(node.left) - get_height(node.right) if node else 0

# 불균형 시 회전 필요 (LL, RR, LR, RL case)
```

### 3-3. Red-Black Tree
- **구현 복잡도**: 중간 (색상 규칙은 간단)
- **특징**: 색상 + 5가지 규칙으로 대략적 균형
- **사용 예**: Java TreeMap, C++ std::map

```python
class RBNode:
    def __init__(self, value):
        self.value = value
        self.color = 'RED'  # 삽입 시 항상 RED
        self.left = None
        self.right = None

# Red-Black Tree 규칙:
# 1. 모든 노드는 RED 또는 BLACK
# 2. 루트는 BLACK
# 3. 모든 잎은 BLACK (NIL)
# 4. RED 노드의 자식은 모두 BLACK
# 5. 루트에서 각 잎까지 BLACK 노드 개수는 동일
```

### 3-4. Splay Tree
- **구현 복잡도**: 중간 (회전 로직)
- **특징**: 접근 시 해당 노드를 루트로 끌어올림
- **사용 예**: 캐시, 메모리 계층 최적화

```python
class SplayNode:
    def __init__(self, value):
        self.value = value
        self.left = None
        self.right = None

def splay(node, value):
    if node is None:
        return None

    if value < node.value:
        # 왼쪽 자식에서 찾고, 회전으로 루트로 끌어올림
        node.left = splay(node.left, value)
        node = rotate_right(node)
    elif value > node.value:
        # 오른쪽 자식에서 찾고, 회전으로 루트로 끌어올림
        node.right = splay(node.right, value)
        node = rotate_left(node)

    return node
```

---

## 4. 사용 사례

### 4-1. Binary Search Tree를 사용하는 경우
- **학습용**: 자료구조 기본 개념 학습
- **작은 데이터셋**: 균형을 신경 쓸 필요 없는 경우
- **이미 정렬된 데이터 아님**: 임의의 삽입 순서

```python
# 단순 이진 탐색 트리
bst = BST()
bst.insert(50)
bst.insert(30)
bst.insert(70)
# 구조: 50 ← 30, 70 (균형잡힘)

# 문제 상황
bst = BST()
for i in range(1, 6):  # 1,2,3,4,5 순서 삽입
    bst.insert(i)
# 결과: 1 → 2 → 3 → 4 → 5 (선형 구조, 성능 저하)
```

### 4-2. AVL Tree를 사용하는 경우
- **데이터베이스 인덱스**: 엄격한 성능 보장 필요
- **실시간 시스템**: 최악 경우도 O(log n) 필요
- **금융 시스템**: 성능 예측이 중요한 경우

```python
# AVL Tree
avl = AVLTree()
for i in range(1, 6):  # 1,2,3,4,5
    avl.insert(i)
# 자동으로 균형 유지
#       3
#      / \
#     2   4
#    /     \
#   1       5
# 높이: log(5) ≈ 2.3
```

### 4-3. Red-Black Tree를 사용하는 경우
- **Java TreeMap, TreeSet**
- **C++ std::map, std::set**
- **Linux 커널 (메모리 관리)**
- **MongoDB 인덱스**

```python
# TreeMap (Java)
# 내부적으로 Red-Black Tree 사용
TreeMap<Integer, String> map = new TreeMap<>();
map.put(50, "A");
map.put(30, "B");
map.put(70, "C");
# O(log n) 보장 + 적은 회전 비용
```

### 4-4. Splay Tree를 사용하는 경우
- **캐시 구현**: 최근 접근 항목 빠른 재접근
- **메모리 계층**: 접근 지역성(locality) 활용
- **자체 조직 탐색(Self-organizing search)**
- **네트워크 라우팅**: 최근 접근한 경로 최적화

```python
# 웹 캐시
cache = SplayTree()

# 같은 페이지 반복 접근
cache.search("/index.html")     # O(log n)
cache.search("/index.html")     # O(1) ← 루트에 있음
cache.search("/index.html")     # O(1)
cache.search("/about.html")     # O(log n)
cache.search("/index.html")     # O(log n) (다시 올려야 함)
```

---

## 5. 백엔드 개발자 관점의 중요성

### 5-1. 데이터베이스 선택
- **B-Tree vs Red-Black Tree**: 데이터베이스는 디스크 I/O 최소화를 위해 B-Tree 사용 (다진 트리)
- **인메모리 DB**: Red-Black Tree로 빠른 접근
- **시계열 DB**: Splay Tree 같은 최적화 활용

### 5-2. 성능 예측
- **최악의 경우 보장이 필요**: AVL Tree 또는 Red-Black Tree
- **평균적으로 빠르면 됨**: BST도 충분 (하지만 위험)
- **접근 패턴이 반복됨**: Splay Tree

```
예시: 온라인 쇼핑
- 인기 상품 반복 검색 → Splay Tree 유리
- 모든 상품 동등하게 검색 → Red-Black Tree 안정적
```

### 5-3. 메모리 vs 성능 트레이드오프
- **AVL**: 높은 메모리 오버헤드, 엄격한 성능 (회전 많음)
- **Red-Black**: 낮은 메모리 오버헤드, 안정적 성능 (회전 적음)
- **Splay**: 메모리 효율, 접근 패턴 의존적

```
노드 10만 개 저장 시:
AVL:      10만 × (pointer 크기 + height) = 약 500KB
RB Tree:  10만 × (pointer 크기 + color bit) = 약 400KB
Splay:    10만 × (pointer 크기) = 약 400KB
```

### 5-4. 동시성 고려
- **BST**: 동시 접근 시 복잡한 락 필요
- **AVL/RB Tree**: 회전이 한정적이라 동시성 제어 용이
- **Splay**: 동시 접근 시 splaying 경합(contention) 가능

---

## 6. 핵심 요약

| 특성 | BST | AVL | Red-Black | Splay |
|------|-----|-----|-----------|-------|
| **조회 시간** | O(log n)~ O(n) | O(log n) | O(log n) | O(log n) 상각 |
| **삽입 시간** | O(log n)~ O(n) | O(log n) | O(log n) | O(log n) 상각 |
| **삭제 시간** | O(log n)~ O(n) | O(log n) | O(log n) | O(log n) 상각 |
| **최악의 경우** | O(n) | O(log n) | O(log n) | O(n) |
| **구현 복잡도** | 낮음 | 높음 | 중간 | 중간 |
| **회전 빈도** | 없음 | 높음 | 낮음 | 매우 높음 |
| **메모리 오버헤드** | 최소 | 높음 | 낮음 | 최소 |
| **접근 패턴 최적화** | X | X | X | O |
| **성능 보장** | X | O | O | X (평균적) |
| **사용 예** | 학습 | 데이터베이스 | Java/C++/Linux | 캐시, 라우팅 |

### 6-1. 선택 기준

| 상황 | 선택 | 이유 |
|------|------|------|
| **성능 예측이 중요** | AVL 또는 RB Tree | 최악의 경우도 O(log n) |
| **구현 단순함이 중요** | BST | 가장 간단하지만 위험 |
| **메모리 효율성 중요** | Splay 또는 RB Tree | 오버헤드 최소 |
| **반복되는 접근 패턴** | Splay Tree | 최근 접근 노드 빠름 |
| **표준 라이브러리 사용** | RB Tree | Java/C++ 표준 |
| **엄격한 균형 필요** | AVL Tree | 높이 차이 <= 1 |

### 6-2. 실무 팁
- **대부분의 경우 Red-Black Tree 사용**: Java TreeMap, C++ std::map이 이를 사용하므로 신뢰도 높음
- **성능 급증이 필요한 경우**: AVL Tree (회전 많지만 더 균형잡힘)
- **특수한 접근 패턴**: Splay Tree 검토 (캐시, 반복 접근)
- **BST는 최후의 수단**: 데이터 정렬 순서 보장 안 되면 위험
- **현대 언어의 기본 Map**: 대부분 Red-Black Tree → 따라가기
