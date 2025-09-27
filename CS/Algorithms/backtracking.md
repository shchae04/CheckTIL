# 백트래킹 (Backtracking)

## 개요
백트래킹은 해를 찾는 도중 막다른 길에 다다르면 되돌아가서 다시 해를 찾아가는 기법입니다. 모든 가능한 경우의 수를 체계적으로 탐색하되, 조건에 맞지 않는 경우를 조기에 포기(가지치기, Pruning)하여 탐색 시간을 단축시키는 완전탐색 기법입니다.

- **핵심 개념**: 깊이 우선 탐색(DFS) + 가지치기(Pruning)
- **동작 원리**: 유망하지 않은 노드의 하위 트리를 탐색하지 않고 상위 노드로 되돌아감
- **적용 분야**: N-Queen 문제, 스도쿠, 순열/조합 생성, 미로 탐색 등

## 백트래킹의 핵심 구성 요소

### 1. 상태 공간 트리 (State Space Tree)
- 해를 찾기 위해 탐색할 수 있는 모든 경우를 트리 구조로 나타낸 것
- 각 노드는 문제 해결 과정의 한 상태를 의미
- 루트에서 리프까지의 경로가 하나의 해가 될 수 있음

### 2. 유망성 검사 (Promising Function)
- 현재 노드에서 해를 찾을 가능성이 있는지 판단하는 함수
- 유망하지 않으면 해당 노드의 하위 트리를 탐색하지 않음 (가지치기)

### 3. 되돌아가기 (Backtrack)
- 현재 노드가 유망하지 않으면 부모 노드로 되돌아가는 과정
- 다른 자식 노드를 탐색하거나 더 상위 노드로 올라감

---

## 백트래킹 알고리즘의 일반적인 구조

```java
public class BacktrackingTemplate {
    
    public void backtrack(int level, /* 현재 상태 매개변수 */) {
        // 1. 종료 조건 (해를 찾았거나 더 이상 진행할 수 없는 경우)
        if (level == targetLevel || isComplete()) {
            if (isValidSolution()) {
                // 해를 찾았을 때의 처리
                processSolution();
            }
            return;
        }
        
        // 2. 현재 레벨에서 시도할 수 있는 모든 선택지를 탐색
        for (int choice : getPossibleChoices(level)) {
            // 3. 유망성 검사
            if (isPromising(level, choice)) {
                // 4. 선택 적용
                makeChoice(choice);
                
                // 5. 다음 단계로 재귀 호출
                backtrack(level + 1);
                
                // 6. 선택 취소 (되돌아가기)
                undoChoice(choice);
            }
        }
    }
}
```

---

## 예제 1: N-Queen 문제
N×N 체스판에 N개의 퀸을 서로 공격할 수 없도록 배치하는 문제

### 문제 분석
- 각 행에 퀸을 하나씩 배치
- 같은 열, 대각선에 다른 퀸이 없어야 함
- 유망성 검사: 현재 위치에 퀸을 놓았을 때 기존 퀸들과 충돌하지 않는지 확인

```java
public class NQueens {
    private int n;
    private int[] queens; // queens[i] = j는 i번째 행의 j번째 열에 퀸이 있음을 의미
    private int solutionCount = 0;
    
    public int solveNQueens(int n) {
        this.n = n;
        this.queens = new int[n];
        backtrack(0);
        return solutionCount;
    }
    
    private void backtrack(int row) {
        // 종료 조건: 모든 행에 퀸을 배치했을 때
        if (row == n) {
            solutionCount++;
            printSolution(); // 해를 출력하거나 저장
            return;
        }
        
        // 현재 행의 각 열에 퀸을 배치해보기
        for (int col = 0; col < n; col++) {
            if (isPromising(row, col)) {
                queens[row] = col;  // 퀸 배치
                backtrack(row + 1); // 다음 행으로 진행
                // queens[row] = -1; // 되돌아가기 (여기서는 다음 반복에서 덮어쓰므로 생략 가능)
            }
        }
    }
    
    private boolean isPromising(int row, int col) {
        for (int i = 0; i < row; i++) {
            // 같은 열에 퀸이 있는지 확인
            if (queens[i] == col) {
                return false;
            }
            // 대각선에 퀸이 있는지 확인
            if (Math.abs(queens[i] - col) == Math.abs(i - row)) {
                return false;
            }
        }
        return true;
    }
    
    private void printSolution() {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (queens[i] == j) {
                    System.out.print("Q ");
                } else {
                    System.out.print(". ");
                }
            }
            System.out.println();
        }
        System.out.println();
    }
}
```

- **시간 복잡도**: O(N!) - 최악의 경우 모든 가능한 배치를 탐색
- **공간 복잡도**: O(N) - 재귀 스택과 퀸 위치 저장 배열

---

## 예제 2: 순열 생성 (Permutation)
주어진 배열의 모든 순열을 생성하는 문제

```java
import java.util.*;

public class Permutations {
    private List<List<Integer>> result;
    
    public List<List<Integer>> permute(int[] nums) {
        result = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        boolean[] used = new boolean[nums.length];
        
        backtrack(nums, current, used);
        return result;
    }
    
    private void backtrack(int[] nums, List<Integer> current, boolean[] used) {
        // 종료 조건: 현재 순열이 완성되었을 때
        if (current.size() == nums.length) {
            result.add(new ArrayList<>(current)); // 깊은 복사 필요
            return;
        }
        
        // 각 숫자를 시도해보기
        for (int i = 0; i < nums.length; i++) {
            if (!used[i]) { // 유망성 검사: 이미 사용한 숫자가 아닌 경우
                current.add(nums[i]);  // 선택
                used[i] = true;
                
                backtrack(nums, current, used); // 재귀 호출
                
                current.remove(current.size() - 1); // 되돌아가기
                used[i] = false;
            }
        }
    }
}
```

- **시간 복잡도**: O(N × N!) - N!개의 순열 × 각 순열을 복사하는 시간 N
- **공간 복잡도**: O(N) - 재귀 스택 깊이

---

## 예제 3: 부분집합 생성 (Subset Generation)
주어진 집합의 모든 부분집합을 생성하는 문제

```java
import java.util.*;

public class Subsets {
    private List<List<Integer>> result;
    
    public List<List<Integer>> subsets(int[] nums) {
        result = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        
        backtrack(nums, 0, current);
        return result;
    }
    
    private void backtrack(int[] nums, int start, List<Integer> current) {
        // 현재 부분집합을 결과에 추가 (모든 단계에서 유효한 해)
        result.add(new ArrayList<>(current));
        
        // start부터 끝까지 각 원소를 포함시켜보기
        for (int i = start; i < nums.length; i++) {
            current.add(nums[i]);           // 선택
            backtrack(nums, i + 1, current); // 다음 인덱스부터 탐색
            current.remove(current.size() - 1); // 되돌아가기
        }
    }
}
```

- **시간 복잡도**: O(N × 2^N) - 2^N개의 부분집합 × 각 부분집합을 복사하는 시간 N
- **공간 복잡도**: O(N) - 재귀 스택 깊이

---

## 백트래킹 vs 브루트포스 vs 동적계획법

### 백트래킹 vs 브루트포스
- **브루트포스**: 모든 경우의 수를 다 확인
- **백트래킹**: 조건에 맞지 않는 경우를 조기에 포기하여 탐색 공간을 줄임
- 백트래킹이 더 효율적이지만, 최악의 경우 브루트포스와 같은 시간 복잡도를 가질 수 있음

### 백트래킹 vs 동적계획법
- **백트래킹**: 해의 존재 여부나 해의 개수를 구할 때 주로 사용
- **동적계획법**: 최적해를 구할 때 주로 사용, 중복 부분 문제가 있을 때 효과적
- 백트래킹은 일반적으로 지수 시간, 동적계획법은 다항 시간 복잡도를 가짐

---

## 백트래킹 최적화 기법

### 1. 가지치기 강화 (Advanced Pruning)
- 더 강력한 조건을 사용하여 유망하지 않은 노드를 조기에 발견
- 문제 특성을 활용한 도메인별 가지치기 조건 추가

### 2. 순서 최적화 (Ordering Heuristics)
- 실패 가능성이 높은 선택지를 먼저 시도하여 빠른 가지치기 유도
- 또는 성공 가능성이 높은 선택지를 먼저 시도하여 빠른 해 발견

### 3. 제약 전파 (Constraint Propagation)
- 하나의 선택이 다른 변수들에 미치는 영향을 미리 계산
- 불가능한 상태를 조기에 감지

---

## 백트래킹이 적용되는 문제 유형

### 1. 배치 문제 (Placement Problems)
- N-Queen, 스도쿠, 그래프 컬러링
- 조건을 만족하는 배치를 찾는 문제

### 2. 조합 최적화 (Combinatorial Optimization)
- 여행판매원 문제(TSP), 배낭 문제
- 모든 가능한 조합 중 최적해를 찾는 문제

### 3. 퍼즐 및 게임 (Puzzles and Games)
- 미로 탐색, 스도쿠, 크로스워드
- 규칙을 만족하는 해를 찾는 문제

### 4. 생성 문제 (Generation Problems)
- 순열, 조합, 부분집합 생성
- 조건을 만족하는 모든 경우를 생성하는 문제

---

## 구현 시 주의사항

### 1. 상태 복원의 중요성
- `makeChoice()`와 `undoChoice()`가 정확히 대응되어야 함
- 전역 상태를 사용할 때는 백트래킹 시 반드시 원상복구

### 2. 깊은 복사 vs 얕은 복사
- 해를 저장할 때 참조가 아닌 새로운 객체를 생성해야 함
- Java에서는 `new ArrayList<>(current)` 사용

### 3. 재귀 깊이 제한
- 입력 크기가 클 때 스택 오버플로우 주의
- 필요시 반복문으로 변환하거나 스택 크기 조정

### 4. 메모리 사용량
- 모든 해를 저장하면 메모리 부족 가능
- 해의 개수만 필요한 경우 저장하지 말고 카운트만 증가

---

## 시간 복잡도 분석
백트래킹의 시간 복잡도는 문제의 특성과 가지치기 효과에 따라 크게 달라집니다:

- **최악의 경우**: 가지치기가 전혀 일어나지 않으면 브루트포스와 동일
- **평균적인 경우**: 효과적인 가지치기로 상당한 시간 단축 가능
- **최선의 경우**: 초기 선택이 좋아 빠르게 해를 발견

일반적으로 지수 시간 복잡도를 가지지만, 실제로는 가지치기 덕분에 훨씬 빠르게 동작합니다.

---

## 연습 문제 추천

### 초급
- 순열/조합/부분집합 생성
- 괄호 생성
- 전화번호 문자 조합

### 중급
- N-Queen 문제
- 스도쿠 해결
- 단어 검색 (Word Search)

### 고급
- 팰린드롬 분할
- 정규식 매칭
- 24게임 (Make 24)

---

## 참고 자료
- Algorithm Design Manual - Steven Skiena
- Introduction to Algorithms - CLRS
- 백준 온라인 저지 백트래킹 문제집
- LeetCode Backtracking Problems