# 세그먼트 트리 (Segment Tree)

## 1. 한 줄 정의
세그먼트 트리는 배열의 특정 범위(구간)에 대한 연산(합, 최댓값, 최솟값 등)을 효율적으로 처리하기 위한 이진 트리 자료구조이다. 구간 쿼리와 업데이트 연산을 O(log n) 시간에 수행할 수 있다.

---

## 2. 기본 개념

### 2-1. 세그먼트 트리란?
- **목적**: 배열의 구간에 대한 집계 연산을 빠르게 수행
- **구조**: 완전 이진 트리 형태로 구성
- **특징**: 각 노드는 특정 구간의 정보를 저장

### 2-2. 왜 필요한가?

```
배열에서 특정 범위의 합을 구하는 경우:

[1] 단순 순회: O(n)
    for (int i = left; i <= right; i++) sum += arr[i];

[2] 누적합(Prefix Sum): O(1) 쿼리, O(n) 업데이트
    - 값이 변경되면 모든 누적합을 다시 계산해야 함

[3] 세그먼트 트리: O(log n) 쿼리, O(log n) 업데이트 ← 최고!
    - 구간 쿼리와 업데이트 모두 효율적
```

### 2-3. 시간 복잡도 비교

| 연산 | 배열 순회 | 누적합 | 세그먼트 트리 |
|------|---------|-------|------------|
| 구간 합 쿼리 | O(n) | O(1) | O(log n) |
| 단일 업데이트 | O(1) | O(n) | O(log n) |
| 구간 업데이트 | O(n) | O(n) | O(log n) |
| 공간 | O(n) | O(n) | O(n) |

---

## 3. 세그먼트 트리의 구조

### 3-1. 트리 구조 예시 (배열 [1, 2, 3, 4, 5])

```
                    [1,15]
                   /      \
              [1,6]          [7,15]
             /      \        /      \
        [1,3]      [4,6]  [7,11]   [12,15]
       /    \      /  \    /  \     /   \
    [1,1]  [2,3] [4,4][5,6][7,9][10,11][12,13][14,15]
     1      5    4    9    12   21    25    9
```

- 리프 노드: 원래 배열의 값
- 중간 노드: 자식 노드 값의 합 (또는 다른 연산)
- 루트 노드: 전체 배열의 합

### 3-2. 배열로 표현

```java
// 크기 n인 배열을 세그먼트 트리로 표현하려면
// 트리 배열의 크기는 4n이 필요
// (최악의 경우 높이가 log n이고, 각 레벨에서 최대 2배씩 증가)

int[] tree = new int[4 * n];

// 트리 인덱싱:
// 루트: 1
// 노드 i의 왼쪽 자식: 2*i
// 노드 i의 오른쪽 자식: 2*i+1
// 노드 i의 부모: i/2
```

---

## 4. 세그먼트 트리 구현 (Java)

### 4-1. 기본 구현 (구간 합)

```java
public class SegmentTree {
    private int[] tree;
    private int[] arr;
    private int n;

    public SegmentTree(int[] arr) {
        this.arr = arr;
        this.n = arr.length;
        this.tree = new int[4 * n];
        build(1, 0, n - 1);
    }

    /**
     * 세그먼트 트리 구축
     * @param node 현재 노드 인덱스
     * @param start 현재 노드가 담당하는 구간의 시작
     * @param end 현재 노드가 담당하는 구간의 끝
     */
    private void build(int node, int start, int end) {
        // 리프 노드인 경우
        if (start == end) {
            tree[node] = arr[start];
            return;
        }

        // 중간 노드인 경우
        int mid = start + (end - start) / 2;
        build(2 * node, start, mid);      // 왼쪽 자식 구축
        build(2 * node + 1, mid + 1, end); // 오른쪽 자식 구축

        // 자식 노드의 합을 현재 노드에 저장
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    /**
     * 구간 [queryStart, queryEnd]의 합 쿼리
     */
    public int query(int queryStart, int queryEnd) {
        return query(1, 0, n - 1, queryStart, queryEnd);
    }

    private int query(int node, int start, int end, int queryStart, int queryEnd) {
        // 쿼리 범위가 현재 노드 범위와 겹치지 않음
        if (queryEnd < start || queryStart > end) {
            return 0;
        }

        // 현재 노드 범위가 쿼리 범위에 완전히 포함됨
        if (queryStart <= start && end <= queryEnd) {
            return tree[node];
        }

        // 부분적으로 겹침 → 왼쪽, 오른쪽 자식에서 재귀적으로 쿼리
        int mid = start + (end - start) / 2;
        int leftSum = query(2 * node, start, mid, queryStart, queryEnd);
        int rightSum = query(2 * node + 1, mid + 1, end, queryStart, queryEnd);

        return leftSum + rightSum;
    }

    /**
     * 특정 인덱스의 값을 업데이트
     */
    public void update(int index, int value) {
        update(1, 0, n - 1, index, value);
    }

    private void update(int node, int start, int end, int index, int value) {
        // 리프 노드 도달
        if (start == end) {
            arr[index] = value;
            tree[node] = value;
            return;
        }

        // 중간 노드 → 왼쪽 또는 오른쪽 자식으로 진행
        int mid = start + (end - start) / 2;
        if (index <= mid) {
            update(2 * node, start, mid, index, value);
        } else {
            update(2 * node + 1, mid + 1, end, index, value);
        }

        // 업데이트된 자식 노드의 값으로 현재 노드 업데이트
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }
}
```

### 4-2. 사용 예시

```java
public class Main {
    public static void main(String[] args) {
        int[] arr = {1, 2, 3, 4, 5};
        SegmentTree st = new SegmentTree(arr);

        // 구간 [0, 2]의 합: 1 + 2 + 3 = 6
        System.out.println(st.query(0, 2)); // 출력: 6

        // 구간 [1, 4]의 합: 2 + 3 + 4 + 5 = 14
        System.out.println(st.query(1, 4)); // 출력: 14

        // 인덱스 2의 값을 10으로 변경
        st.update(2, 10);

        // 변경 후 구간 [0, 2]의 합: 1 + 2 + 10 = 13
        System.out.println(st.query(0, 2)); // 출력: 13
    }
}
```

---

## 5. 고급 기능

### 5-1. 구간 최댓값 쿼리

```java
public class SegmentTreeMax {
    private int[] tree;
    private int[] arr;
    private int n;

    public SegmentTreeMax(int[] arr) {
        this.arr = arr;
        this.n = arr.length;
        this.tree = new int[4 * n];
        build(1, 0, n - 1);
    }

    private void build(int node, int start, int end) {
        if (start == end) {
            tree[node] = arr[start];
            return;
        }

        int mid = start + (end - start) / 2;
        build(2 * node, start, mid);
        build(2 * node + 1, mid + 1, end);

        // 변경: 합 대신 최댓값 저장
        tree[node] = Math.max(tree[2 * node], tree[2 * node + 1]);
    }

    public int queryMax(int queryStart, int queryEnd) {
        return queryMax(1, 0, n - 1, queryStart, queryEnd);
    }

    private int queryMax(int node, int start, int end, int queryStart, int queryEnd) {
        if (queryEnd < start || queryStart > end) {
            return Integer.MIN_VALUE;  // 무의미한 값 반환
        }

        if (queryStart <= start && end <= queryEnd) {
            return tree[node];
        }

        int mid = start + (end - start) / 2;
        int leftMax = queryMax(2 * node, start, mid, queryStart, queryEnd);
        int rightMax = queryMax(2 * node + 1, mid + 1, end, queryStart, queryEnd);

        return Math.max(leftMax, rightMax);
    }

    public void update(int index, int value) {
        update(1, 0, n - 1, index, value);
    }

    private void update(int node, int start, int end, int index, int value) {
        if (start == end) {
            arr[index] = value;
            tree[node] = value;
            return;
        }

        int mid = start + (end - start) / 2;
        if (index <= mid) {
            update(2 * node, start, mid, index, value);
        } else {
            update(2 * node + 1, mid + 1, end, index, value);
        }

        tree[node] = Math.max(tree[2 * node], tree[2 * node + 1]);
    }
}
```

### 5-2. 구간 업데이트 (Lazy Propagation)

```java
public class SegmentTreeLazy {
    private int[] tree;
    private int[] lazy;
    private int[] arr;
    private int n;

    public SegmentTreeLazy(int[] arr) {
        this.arr = arr;
        this.n = arr.length;
        this.tree = new int[4 * n];
        this.lazy = new int[4 * n];
        build(1, 0, n - 1);
    }

    private void build(int node, int start, int end) {
        if (start == end) {
            tree[node] = arr[start];
            return;
        }

        int mid = start + (end - start) / 2;
        build(2 * node, start, mid);
        build(2 * node + 1, mid + 1, end);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    /**
     * 구간 [l, r]의 모든 값에 value를 더함
     */
    public void rangeUpdate(int l, int r, int value) {
        rangeUpdate(1, 0, n - 1, l, r, value);
    }

    private void rangeUpdate(int node, int start, int end, int l, int r, int value) {
        // 이전 lazy 값 적용
        if (lazy[node] != 0) {
            tree[node] += (end - start + 1) * lazy[node];
            if (start != end) {
                lazy[2 * node] += lazy[node];
                lazy[2 * node + 1] += lazy[node];
            }
            lazy[node] = 0;
        }

        // 업데이트 범위가 현재 범위와 겹치지 않음
        if (l > end || r < start) {
            return;
        }

        // 현재 범위가 업데이트 범위에 완전히 포함
        if (l <= start && end <= r) {
            tree[node] += (end - start + 1) * value;
            if (start != end) {
                lazy[2 * node] += value;
                lazy[2 * node + 1] += value;
            }
            return;
        }

        // 부분적으로 겹침
        int mid = start + (end - start) / 2;
        rangeUpdate(2 * node, start, mid, l, r, value);
        rangeUpdate(2 * node + 1, mid + 1, end, l, r, value);
        tree[node] = tree[2 * node] + tree[2 * node + 1];
    }

    public int query(int l, int r) {
        return query(1, 0, n - 1, l, r);
    }

    private int query(int node, int start, int end, int l, int r) {
        // 이전 lazy 값 적용
        if (lazy[node] != 0) {
            tree[node] += (end - start + 1) * lazy[node];
            if (start != end) {
                lazy[2 * node] += lazy[node];
                lazy[2 * node + 1] += lazy[node];
            }
            lazy[node] = 0;
        }

        // 쿼리 범위와 겹치지 않음
        if (l > end || r < start) {
            return 0;
        }

        // 현재 범위가 쿼리 범위에 완전히 포함
        if (l <= start && end <= r) {
            return tree[node];
        }

        // 부분적으로 겹침
        int mid = start + (end - start) / 2;
        int leftSum = query(2 * node, start, mid, l, r);
        int rightSum = query(2 * node + 1, mid + 1, end, l, r);
        return leftSum + rightSum;
    }
}
```

---

## 6. 세그먼트 트리의 응용

### 6-1. 적용 가능한 연산

| 연산 | 설명 | 예시 |
|------|------|------|
| 덧셈 | 구간 합 | 가장 일반적인 경우 |
| 최댓값 | 구간 최댓값 | 특정 범위의 최대값 찾기 |
| 최솟값 | 구간 최솟값 | 특정 범위의 최소값 찾기 |
| XOR | 구간 XOR | 패턴 매칭에 유용 |
| GCD | 최대공약수 | 정수론 문제 |

### 6-2. 실제 응용 사례

```
1. 온라인 판매 시스템
   - 특정 기간의 총 판매량 조회: O(log n)
   - 특정 제품 판매량 업데이트: O(log n)

2. 센서 네트워크
   - 특정 구간의 평균 온도: O(log n)
   - 특정 센서의 값 업데이트: O(log n)

3. 금융 데이터 분석
   - 특정 기간의 주가 변동: O(log n)
   - 주가 업데이트: O(log n)

4. 온라인 게임
   - 플레이어 랭킹 구간 조회: O(log n)
   - 플레이어 점수 업데이트: O(log n)
```

---

## 7. 세그먼트 트리 vs 다른 자료구조

| 자료구조 | 구간 쿼리 | 단일 업데이트 | 구간 업데이트 | 구현 복잡도 |
|---------|---------|------------|------------|----------|
| 배열 | O(n) | O(1) | O(n) | 낮음 |
| 누적합 | O(1) | O(n) | O(n) | 낮음 |
| 펜윅 트리 | O(log n) | O(log n) | O(log n) | 중간 |
| 세그먼트 트리 | O(log n) | O(log n) | O(log n) | 높음 |

**선택 기준:**
- 단순 쿼리만 필요: 누적합
- 자주 업데이트: 펜윅 트리 또는 세그먼트 트리
- 복잡한 연산 필요: 세그먼트 트리

---

## 8. 세그먼트 트리의 장단점

### 8-1. 장점
- **빠른 구간 쿼리**: O(log n)
- **빠른 업데이트**: O(log n)
- **유연한 연산**: 다양한 연산 지원 (합, 최댓값, 최솟값 등)
- **구간 업데이트 지원**: Lazy Propagation으로 O(log n) 구현 가능

### 8-2. 단점
- **높은 구현 복잡도**: 코드 작성이 복잡함
- **추가 메모리**: O(4n) 메모리 필요
- **학습 난도**: 이해하기 어려움
- **디버깅 어려움**: 버그 찾기가 힘들 수 있음

---

## 9. 면접 질문

### 9-1. 개념 질문
1. 세그먼트 트리가 무엇이고 어떨 때 사용하나요?
2. 세그먼트 트리의 시간 복잡도는?
3. 세그먼트 트리와 펜윅 트리의 차이점은?

### 9-2. 구현 질문
1. 세그먼트 트리를 구축하는 과정을 설명해주세요.
2. 구간 쿼리는 어떻게 동작하나요?
3. Lazy Propagation이 무엇인가요?

### 9-3. 최적화 질문
1. 메모리를 더 효율적으로 사용할 수 있을까요?
2. 재귀 대신 반복문으로 구현할 수 있나요?
3. 다양한 연산을 어떻게 지원할까요?

---

## 10. 핵심 요약

### 10-1. 세그먼트 트리의 역할
- **구간 쿼리와 업데이트를 O(log n)에 처리**
- 누적합보다 유연함
- 복잡한 연산 지원

### 10-2. 구현의 핵심
1. 트리 구축: O(n)
2. 구간 쿼리: O(log n)
3. 단일 업데이트: O(log n)
4. 구간 업데이트(Lazy): O(log n)

### 10-3. 실무 활용
- 대규모 데이터에서 구간 연산이 자주 발생하는 경우
- 실시간 업데이트가 필요한 시스템
- 다양한 구간 통계가 필요한 데이터 분석
