# 시간복잡도와 공간복잡도의 차이점

## 1. 한 줄 정의
- **시간복잡도(Time Complexity)**: 알고리즘이 문제를 해결하는데 걸리는 시간을 입력 크기의 함수로 표현한 것
- **공간복잡도(Space Complexity)**: 알고리즘이 실행되는 동안 사용하는 메모리 공간을 입력 크기의 함수로 표현한 것

---

## 2. 시간복잡도 (Time Complexity)

### 2-1. 개념
알고리즘의 실행 시간이 입력 크기 n에 따라 어떻게 증가하는지를 나타내는 지표입니다.

### 2-2. 주요 표기법: Big-O 표기법
- **O(1)**: 상수 시간 - 입력 크기와 무관하게 일정한 시간
- **O(log n)**: 로그 시간 - 이진 탐색
- **O(n)**: 선형 시간 - 배열 순회
- **O(n log n)**: 선형 로그 시간 - 효율적인 정렬 (병합 정렬, 퀵 정렬)
- **O(n²)**: 이차 시간 - 중첩 반복문 (버블 정렬, 선택 정렬)
- **O(2ⁿ)**: 지수 시간 - 피보나치 재귀
- **O(n!)**: 팩토리얼 시간 - 순열 생성

### 2-3. 예시 코드

```java
// O(1) - 상수 시간
public int getFirstElement(int[] arr) {
    return arr[0];  // 항상 한 번의 연산
}

// O(n) - 선형 시간
public int findMax(int[] arr) {
    int max = arr[0];
    for (int i = 1; i < arr.length; i++) {  // n번 반복
        if (arr[i] > max) {
            max = arr[i];
        }
    }
    return max;
}

// O(n²) - 이차 시간
public void bubbleSort(int[] arr) {
    for (int i = 0; i < arr.length; i++) {        // n번
        for (int j = 0; j < arr.length - 1; j++) {  // n번
            if (arr[j] > arr[j + 1]) {
                // swap
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}

// O(log n) - 로그 시간
public int binarySearch(int[] arr, int target) {
    int left = 0, right = arr.length - 1;
    while (left <= right) {
        int mid = left + (right - left) / 2;
        if (arr[mid] == target) return mid;
        if (arr[mid] < target) left = mid + 1;
        else right = mid - 1;
    }
    return -1;
}
```

---

## 3. 공간복잡도 (Space Complexity)

### 3-1. 개념
알고리즘이 실행되는 동안 필요로 하는 메모리 공간의 양을 입력 크기의 함수로 나타낸 것입니다.

### 3-2. 공간복잡도의 구성 요소
1. **고정 공간(Fixed Space)**: 알고리즘과 무관한 공간
   - 코드 자체를 저장하는 공간
   - 단순 변수, 상수

2. **가변 공간(Variable Space)**: 알고리즘 실행에 따라 달라지는 공간
   - 동적으로 할당되는 메모리
   - 재귀 호출 스택
   - 임시 배열이나 자료구조

### 3-3. 예시 코드

```java
// O(1) 공간복잡도 - 상수 공간
public int sum(int[] arr) {
    int total = 0;  // 고정된 변수 하나만 사용
    for (int num : arr) {
        total += num;
    }
    return total;
}

// O(n) 공간복잡도 - 선형 공간
public int[] copyArray(int[] arr) {
    int[] copy = new int[arr.length];  // n크기의 배열 생성
    for (int i = 0; i < arr.length; i++) {
        copy[i] = arr[i];
    }
    return copy;
}

// O(n) 공간복잡도 - 재귀 호출 스택
public int factorial(int n) {
    if (n <= 1) return 1;
    return n * factorial(n - 1);  // 재귀 깊이 n만큼 스택 사용
}

// O(n²) 공간복잡도 - 2차원 배열
public int[][] createMatrix(int n) {
    int[][] matrix = new int[n][n];  // n×n 크기의 2차원 배열
    return matrix;
}
```

---

## 4. 시간복잡도 vs 공간복잡도: 핵심 차이점

| 구분 | 시간복잡도 | 공간복잡도 |
|------|-----------|-----------|
| **측정 대상** | 연산 횟수 (실행 시간) | 메모리 사용량 |
| **주요 관심사** | 얼마나 빠른가? | 얼마나 적은 메모리를 사용하는가? |
| **최적화 방향** | 불필요한 연산 제거, 효율적인 알고리즘 선택 | 메모리 재사용, 불필요한 변수 제거 |
| **리소스** | CPU 시간 | RAM (메모리) |
| **트레이드오프** | 메모리를 더 사용해서 시간 단축 가능 | 시간을 더 써서 메모리 절약 가능 |

---

## 5. 시간-공간 트레이드오프 (Time-Space Tradeoff)

### 5-1. 개념
시간복잡도와 공간복잡도는 종종 반비례 관계에 있습니다. 하나를 개선하면 다른 하나가 희생되는 경우가 많습니다.

### 5-2. 실제 예시: 피보나치 수열

```java
// 방법 1: 재귀 (시간 O(2ⁿ), 공간 O(n))
public int fibRecursive(int n) {
    if (n <= 1) return n;
    return fibRecursive(n - 1) + fibRecursive(n - 2);
}

// 방법 2: 동적 프로그래밍 - 메모이제이션 (시간 O(n), 공간 O(n))
public int fibMemoization(int n) {
    int[] memo = new int[n + 1];  // 추가 메모리 사용
    return fibHelper(n, memo);
}

private int fibHelper(int n, int[] memo) {
    if (n <= 1) return n;
    if (memo[n] != 0) return memo[n];
    memo[n] = fibHelper(n - 1, memo) + fibHelper(n - 2, memo);
    return memo[n];
}

// 방법 3: 반복문 (시간 O(n), 공간 O(1))
public int fibIterative(int n) {
    if (n <= 1) return n;
    int prev = 0, curr = 1;
    for (int i = 2; i <= n; i++) {
        int next = prev + curr;
        prev = curr;
        curr = next;
    }
    return curr;
}
```

**분석**:
- 재귀: 매우 느리지만 코드가 직관적
- 메모이제이션: 빠르지만 추가 메모리 필요
- 반복문: 빠르고 메모리 효율적, 가장 최적

---

## 6. 백엔드 개발자 관점에서의 고려사항

### 6-1. 실무에서의 선택 기준

1. **웹 API 응답 시간**: 시간복잡도가 더 중요
   - 사용자 경험에 직접 영향
   - 일반적으로 200ms 이내 응답 목표

2. **대용량 데이터 처리**: 공간복잡도가 중요
   - 메모리 부족 시 서버 다운
   - 페이징, 스트리밍 처리 고려

3. **캐싱 전략**: 공간을 희생해 시간 단축
   - Redis, Memcached 활용
   - 자주 사용되는 데이터를 메모리에 저장

### 6-2. 데이터베이스 쿼리 최적화

```sql
-- 시간복잡도 개선: 인덱스 사용
-- O(n) → O(log n)
CREATE INDEX idx_user_email ON users(email);

-- 공간복잡도 고려: 필요한 컬럼만 조회
-- SELECT * 대신 필요한 컬럼만
SELECT id, name, email FROM users WHERE status = 'active';
```

### 6-3. 실제 서비스 시나리오

```java
// 시나리오: 사용자 추천 시스템

// 방법 1: 실시간 계산 (시간 O(n²), 공간 O(1))
public List<User> getRecommendations(User user) {
    // 매번 모든 사용자와 유사도 계산
    // 느리지만 항상 최신 데이터
}

// 방법 2: 사전 계산 + 캐싱 (시간 O(1), 공간 O(n))
public List<User> getRecommendationsFromCache(User user) {
    // 미리 계산된 결과를 캐시에서 조회
    // 빠르지만 메모리 사용량 증가, 데이터 신선도 문제
    return redisCache.get("recommendations:" + user.getId());
}

// 방법 3: 하이브리드 접근
public List<User> getRecommendationsHybrid(User user) {
    // 인기 사용자는 캐싱, 나머지는 실시간 계산
    if (isPopularUser(user)) {
        return getRecommendationsFromCache(user);
    }
    return getRecommendations(user);
}
```

---

## 7. 예상 면접 질문

### 7-1. 기본 개념
1. **Q**: 시간복잡도와 공간복잡도의 차이는 무엇인가요?
   - **A**: 시간복잡도는 알고리즘의 실행 시간을, 공간복잡도는 메모리 사용량을 입력 크기에 대한 함수로 나타낸 것입니다.

2. **Q**: Big-O 표기법에서 O(n)과 O(n²)의 차이를 설명해주세요.
   - **A**: O(n)은 선형 시간으로 입력이 두 배가 되면 실행 시간도 두 배가 되고, O(n²)은 이차 시간으로 입력이 두 배가 되면 실행 시간은 네 배가 됩니다.

3. **Q**: 재귀 함수의 공간복잡도는 어떻게 계산하나요?
   - **A**: 재귀 호출의 최대 깊이를 고려해야 합니다. 각 재귀 호출마다 스택 프레임이 생성되므로, 재귀 깊이가 n이면 공간복잡도는 최소 O(n)입니다.

### 7-2. 실무 시나리오
1. **Q**: 대용량 파일을 처리할 때 메모리 부족 문제를 어떻게 해결하나요?
   - **A**: 전체 파일을 메모리에 로드하지 않고 스트리밍 방식으로 처리합니다. 청크 단위로 읽고 처리한 후 버리는 방식으로 공간복잡도를 O(1)로 유지할 수 있습니다.

2. **Q**: 시간복잡도와 공간복잡도 중 어느 것을 우선시해야 하나요?
   - **A**: 상황에 따라 다릅니다. 실시간 응답이 중요한 웹 서비스는 시간복잡도를, 메모리가 제한적인 임베디드 시스템은 공간복잡도를 우선시합니다. 일반적으로 메모리는 확장 가능하지만 사용자 대기 시간은 개선이 어려우므로 시간복잡도를 먼저 고려하는 경향이 있습니다.

3. **Q**: 데이터베이스 인덱스가 시간복잡도와 공간복잡도에 어떤 영향을 미치나요?
   - **A**: 인덱스는 조회 시간을 O(n)에서 O(log n)으로 개선하지만, 추가 디스크 공간을 사용하고 INSERT/UPDATE 시 인덱스 갱신으로 쓰기 성능이 저하될 수 있습니다. 이는 전형적인 시간-공간 트레이드오프 사례입니다.

### 7-3. 코딩 테스트
1. **Q**: 주어진 배열에서 중복을 제거하는 두 가지 방법의 시간/공간 복잡도를 비교하세요.

```java
// 방법 1: Set 사용 (시간 O(n), 공간 O(n))
public int[] removeDuplicates1(int[] arr) {
    Set<Integer> set = new HashSet<>();
    for (int num : arr) {
        set.add(num);
    }
    return set.stream().mapToInt(Integer::intValue).toArray();
}

// 방법 2: 정렬 후 제거 (시간 O(n log n), 공간 O(1))
public int removeDuplicates2(int[] arr) {
    if (arr.length == 0) return 0;
    Arrays.sort(arr);  // 원본 배열 수정
    int j = 0;
    for (int i = 1; i < arr.length; i++) {
        if (arr[i] != arr[j]) {
            arr[++j] = arr[i];
        }
    }
    return j + 1;
}
```

---

## 8. 핵심 요약

### 8-1. 시간복잡도
- **목적**: 알고리즘의 실행 속도 측정
- **최적화**: 불필요한 반복문 제거, 효율적인 자료구조 선택
- **주요 고려**: 사용자 응답 시간, API 처리 속도

### 8-2. 공간복잡도
- **목적**: 알고리즘의 메모리 사용량 측정
- **최적화**: 변수 재사용, 스트리밍 처리, 가비지 컬렉션 고려
- **주요 고려**: 서버 메모리 용량, 동시 처리 가능 요청 수

### 8-3. 실무 적용 원칙
1. **측정 우선**: 추측하지 말고 프로파일링으로 병목 지점 파악
2. **상황별 최적화**: 서비스 특성에 맞는 트레이드오프 선택
3. **점진적 개선**: 가독성 있는 코드 작성 후 필요시 최적화
4. **캐싱 활용**: 계산 결과를 저장하여 시간 단축
5. **비동기 처리**: 블로킹 작업은 비동기로 처리하여 응답 시간 개선

---

## 9. 참고: 복잡도 비교 치트시트

```
시간복잡도 성능 순서 (좋음 → 나쁨):
O(1) < O(log n) < O(n) < O(n log n) < O(n²) < O(2ⁿ) < O(n!)

입력 크기별 허용 가능한 복잡도 (1초 기준):
- n ≤ 10: O(n!)
- n ≤ 20: O(2ⁿ)
- n ≤ 500: O(n³)
- n ≤ 5,000: O(n²)
- n ≤ 1,000,000: O(n log n)
- n ≤ 100,000,000: O(n)
- n이 매우 큰 경우: O(log n), O(1)
```