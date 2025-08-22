# 동적 계획법 (Dynamic Programming, DP)

## 개요
동적 계획법(DP)은 복잡한 문제를 더 작은 하위 문제로 나누어 해결하고, 그 결과를 저장하여 같은 하위 문제를 반복해서 풀지 않도록 하는 알고리즘 기법입니다. 핵심은 중복되는 하위 문제와 최적 부분 구조를 이용해 전체 문제를 효율적으로 해결하는 데 있습니다.

- 중복 하위 문제(Overlapping Subproblems): 동일한 하위 문제가 여러 번 등장합니다.
- 최적 부분 구조(Optimal Substructure): 문제의 최적해가 하위 문제들의 최적해로부터 구성될 수 있습니다.
- 메모이제이션(Memoization, Top-Down)과 타뷸레이션(Tabulation, Bottom-Up) 두 방식이 널리 쓰입니다.

## 언제 DP를 사용할까?
- 재귀 + 중복 계산이 많은 문제
- 모든 경우를 탐색하면 지수 시간이 걸리지만, 하위 문제 결과를 재사용하면 다항 시간으로 줄어드는 문제
- 경로의 개수, 최댓값/최솟값/최대 합/최장 길이 등을 묻는 문제

## 기본 구성 요소
1. 상태 정의(State): dp[i][j] 등으로 “무엇을 최적화/계산”할지를 정확히 정의합니다.
2. 점화식(Transition/Recurrence): 상태 간 전이 규칙을 수식으로 표현합니다.
3. 초기값(Initialization): 경계 조건과 기저 사례를 설정합니다.
4. 계산 순서(Iteration Order): Top-Down(재귀+메모) 또는 Bottom-Up(반복문) 순서를 정합니다.
5. 답 추출: 최종적으로 필요한 상태에서 답을 뽑습니다.

---

## 예제 1: 피보나치 수열
문제: F(0)=0, F(1)=1, F(n)=F(n-1)+F(n-2)

### 1) Top-Down (메모이제이션)
```java
import java.util.*;

public class FibonacciMemo {
    private Map<Integer, Long> memo = new HashMap<>();

    public long fib(int n) {
        if (n <= 1) return n;
        if (memo.containsKey(n)) return memo.get(n);
        long val = fib(n - 1) + fib(n - 2);
        memo.put(n, val);
        return val;
    }
}
```

### 2) Bottom-Up (타뷸레이션)
```java
public class FibonacciTab {
    public long fib(int n) {
        if (n <= 1) return n;
        long prev2 = 0, prev1 = 1;
        for (int i = 2; i <= n; i++) {
            long cur = prev1 + prev2;
            prev2 = prev1;
            prev1 = cur;
        }
        return prev1;
    }
}
```
- 시간 복잡도: O(n)
- 공간 복잡도: O(1) (상수 공간 최적화 적용 시)

---

## 예제 2: 0/1 배낭 문제 (Knapsack)
문제: 무게 한도 W, 각 물건 i의 가치 v[i], 무게 w[i]. 일부만 담을 수 없고(0/1), 가치의 최댓값을 구하라.

### 상태 정의
- dp[i][curW] = i번째 물건까지 고려했을 때, 무게 curW로 담을 수 있는 최대 가치

### 점화식
- 담지 않는 경우: dp[i-1][curW]
- 담는 경우(가능하면): dp[i-1][curW - w[i]] + v[i]
- dp[i][curW] = max(담지 않음, 담음)

### 구현 (1차원 최적화)
```java
public class Knapsack01 {
    public int maxValue(int[] w, int[] v, int W) {
        int n = w.length;
        int[] dp = new int[W + 1];
        for (int i = 0; i < n; i++) {
            for (int curW = W; curW >= w[i]; curW--) {
                dp[curW] = Math.max(dp[curW], dp[curW - w[i]] + v[i]);
            }
        }
        return dp[W];
    }
}
```
- 시간 복잡도: O(nW)
- 공간 복잡도: O(W)

---

## 예제 3: LIS (최장 증가 부분 수열)
문제: 수열에서 증가하는 부분 수열 중 가장 긴 길이.

### O(n log n) 접근(전형 DP+이진 탐색)
핵심 아이디어: 길이 k의 증가 부분 수열이 가질 수 있는 “가장 작은 끝 값”을 관리하는 배열 tails를 유지합니다.

```java
import java.util.*;

public class LIS {
    public int lengthOfLIS(int[] nums) {
        List<Integer> tails = new ArrayList<>();
        for (int x : nums) {
            int i = lowerBound(tails, x);
            if (i == tails.size()) tails.add(x);
            else tails.set(i, x);
        }
        return tails.size();
    }

    private int lowerBound(List<Integer> a, int key) {
        int l = 0, r = a.size();
        while (l < r) {
            int m = (l + r) >>> 1;
            if (a.get(m) < key) l = m + 1; else r = m;
        }
        return l;
    }
}
```
- 전통적 DP(O(n^2))도 가능: dp[i] = i에서 끝나는 LIS 길이 = max(dp[j] + 1 | j < i, nums[j] < nums[i])

---

## 자주 등장하는 DP 패턴
1. 1차원 누적/최적화: 최대 부분합(Kadane), 계단 오르기, 코인 체인지(조합/경우의 수), 칸 채우기
2. 2차원 그리드: 격자 경로 수, 편집 거리(Edit Distance), LCS, 2D 배낭
3. 구간 DP: 행렬 곱셈 순서, 괄호 추가, 팰린드롬 분할
4. 비트마스크 DP: TSP, 부분집합 순회
5. 트리 DP: 자식 서브트리 정보를 합쳐 루트 해 구하기
6. 확률/기댓값 DP: 주사위, 마코프 과정 단순화

## 메모이제이션 vs 타뷸레이션
- 메모이제이션(Top-Down): 재귀로 필요할 때만 계산, 코드가 직관적. 하지만 재귀 한도/스택 사용 주의.
- 타뷸레이션(Bottom-Up): 계산 순서를 직접 설계, 반복문으로 스택 부담 없음. 순서가 중요.

## 구현 팁
- 상태 정의를 명확히: 인덱스의 의미, 차원 수, 무엇을 저장하는지부터 적어보세요.
- 경계 조건 체크: 배열 범위, 0/빈 입력 처리.
- 공간 최적화: 2D를 1D로 압축, 순회 방향(역방향/정방향) 주의.
- 큰 수 모듈러: 경우의 수 문제는 1e9+7 등 모듈러 연산 포함.
- 추적(복원): 경로/선택을 복원하려면 prev 포인터/배열을 추가로 유지.

## 시간/공간 복잡도 관점
- DP는 보통 상태 수 × 전이 당 비용으로 계산합니다.
- 상태 수가 크면(예: n·W) 메모리 한계를 먼저 맞을 수 있음 → 압축 필요.

## 흔한 실수
- 점화식과 상태 정의 불일치
- 초기값 누락 또는 잘못된 설정
- 순회 순서 오류(특히 1차원 압축 시 역순 필요 여부)
- 중복 카운팅(조합/순열 문제에서 순서 고려 실수)

## 연습 문제 아이디어
- 피보나치 변형: 타일링(2×n 보드), 점프 방법 수
- 코인 체인지: 경우의 수/최소 동전 수
- LCS/LIS/LPS(최장 팰린드롬 부분수열)
- 편집 거리(Edit Distance)
- 0/1 및 완전 배낭

## 참고 자료
- CLRS: Introduction to Algorithms – Dynamic Programming
- CP-Algorithms: https://cp-algorithms.com/
- 백준/프로그래머스의 DP 태그 문제
