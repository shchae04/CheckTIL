# 이진 탐색 (Binary Search)

이진 탐색은 정렬된 배열에서 특정 값을 찾는 효율적인 알고리즘입니다. 이 알고리즘은 분할 정복(Divide and Conquer) 방법을 사용하여 탐색 범위를 절반씩 줄여나가며 원하는 값을 찾습니다.

## 1. 기본 개념

이진 탐색은 다음과 같은 단계로 진행됩니다:
1. 배열의 중간 요소를 선택합니다.
2. 중간 요소와 찾고자 하는 값을 비교합니다.
3. 중간 요소가 찾는 값보다 크면, 왼쪽 절반에서 탐색을 계속합니다.
4. 중간 요소가 찾는 값보다 작으면, 오른쪽 절반에서 탐색을 계속합니다.
5. 중간 요소가 찾는 값과 같으면, 탐색을 종료하고 해당 인덱스를 반환합니다.
6. 찾는 값이 없으면 적절한 값(보통 -1)을 반환합니다.

## 2. Java 구현

### 반복적 구현 (Iterative Implementation)

```java
/**
 * 이진 탐색 알고리즘의 반복적 구현
 * 정렬된 배열에서 특정 값을 찾는 방법
 */
public class BinarySearch {
    /**
     * 이진 탐색을 수행하는 메소드 (반복적 방법)
     * @param arr 정렬된 정수 배열
     * @param x 찾고자 하는 값
     * @return 찾은 경우 해당 인덱스, 찾지 못한 경우 -1
     */
    public static int binarySearch(int[] arr, int x) {
        int left = 0;                  // 탐색 범위의 시작 인덱스
        int right = arr.length - 1;    // 탐색 범위의 끝 인덱스
        
        while (left <= right) {
            // 중간 인덱스 계산 (오버플로우 방지를 위한 방식)
            int mid = left + (right - left) / 2;
            
            // 중간 요소가 찾는 값인 경우
            if (arr[mid] == x) {
                return mid;  // 찾은 인덱스 반환
            }
            
            // 중간 요소가 찾는 값보다 큰 경우, 왼쪽 부분 배열에서 탐색
            if (arr[mid] > x) {
                right = mid - 1;
            }
            // 중간 요소가 찾는 값보다 작은 경우, 오른쪽 부분 배열에서 탐색
            else {
                left = mid + 1;
            }
        }
        
        // 값을 찾지 못한 경우
        return -1;
    }
}
```

### 재귀적 구현 (Recursive Implementation)

```java
/**
 * 이진 탐색 알고리즘의 재귀적 구현
 */
public class RecursiveBinarySearch {
    /**
     * 이진 탐색을 수행하는 메소드 (재귀적 방법)
     * @param arr 정렬된 정수 배열
     * @param x 찾고자 하는 값
     * @param left 탐색 범위의 시작 인덱스
     * @param right 탐색 범위의 끝 인덱스
     * @return 찾은 경우 해당 인덱스, 찾지 못한 경우 -1
     */
    public static int binarySearchRecursive(int[] arr, int x, int left, int right) {
        // 기저 조건: 탐색 범위가 유효하지 않은 경우
        if (left > right) {
            return -1;  // 값을 찾지 못함
        }
        
        // 중간 인덱스 계산
        int mid = left + (right - left) / 2;
        
        // 중간 요소가 찾는 값인 경우
        if (arr[mid] == x) {
            return mid;
        }
        
        // 중간 요소가 찾는 값보다 큰 경우, 왼쪽 부분 배열에서 재귀적으로 탐색
        if (arr[mid] > x) {
            return binarySearchRecursive(arr, x, left, mid - 1);
        }
        
        // 중간 요소가 찾는 값보다 작은 경우, 오른쪽 부분 배열에서 재귀적으로 탐색
        return binarySearchRecursive(arr, x, mid + 1, right);
    }
    
    /**
     * 이진 탐색의 재귀적 구현을 위한 래퍼 메소드
     * @param arr 정렬된 정수 배열
     * @param x 찾고자 하는 값
     * @return 찾은 경우 해당 인덱스, 찾지 못한 경우 -1
     */
    public static int binarySearch(int[] arr, int x) {
        return binarySearchRecursive(arr, x, 0, arr.length - 1);
    }
}
```

## 3. 특징

- **시간 복잡도**: O(log n)
  - 매 단계마다 탐색 범위가 절반으로 줄어들기 때문에 로그 시간 복잡도를 가집니다.
  - 선형 탐색(O(n))보다 훨씬 효율적입니다.

- **공간 복잡도**:
  - 반복적 구현: O(1) - 추가 공간이 거의 필요 없습니다.
  - 재귀적 구현: O(log n) - 재귀 호출 스택에 필요한 공간입니다.

- **장점**:
  - 대규모 정렬된 데이터에서 매우 효율적입니다.
  - 구현이 비교적 간단합니다.

- **단점**:
  - 정렬된 배열에서만 사용할 수 있습니다.
  - 동적 크기 조정이 필요한 데이터 구조에는 적합하지 않을 수 있습니다.

## 4. 응용 분야

이진 탐색은 다양한 분야에서 활용됩니다:

1. **데이터베이스 인덱싱**: B-트리와 같은 데이터베이스 인덱스 구조의 기본 원리입니다.
2. **컴퓨터 그래픽**: 충돌 감지 및 레이 캐스팅에 사용됩니다.
3. **네트워크 라우팅**: 라우팅 테이블에서 최적 경로를 찾는 데 활용됩니다.
4. **머신 러닝**: 결정 트리와 같은 알고리즘에서 사용됩니다.

## 5. 변형 알고리즘

### 5.1 Lower Bound (하한)

배열에서 특정 값 이상이 처음 나타나는 위치를 찾습니다.

```java
public static int lowerBound(int[] arr, int x) {
    int left = 0;
    int right = arr.length;
    
    while (left < right) {
        int mid = left + (right - left) / 2;
        
        if (arr[mid] < x) {
            left = mid + 1;
        } else {
            right = mid;
        }
    }
    
    return left;
}
```

### 5.2 Upper Bound (상한)

배열에서 특정 값보다 큰 첫 번째 요소의 위치를 찾습니다.

```java
public static int upperBound(int[] arr, int x) {
    int left = 0;
    int right = arr.length;
    
    while (left < right) {
        int mid = left + (right - left) / 2;
        
        if (arr[mid] <= x) {
            left = mid + 1;
        } else {
            right = mid;
        }
    }
    
    return left;
}
```

## 6. 주의사항

1. **정렬된 배열**: 이진 탐색은 반드시 정렬된 배열에서만 작동합니다.
2. **중간 인덱스 계산**: `(left + right) / 2` 대신 `left + (right - left) / 2`를 사용하여 정수 오버플로우를 방지합니다.
3. **경계 조건**: 탐색 범위의 시작과 끝을 올바르게 조정해야 합니다.
4. **무한 루프**: 반복적 구현에서 탐색 범위가 적절히 줄어들지 않으면 무한 루프에 빠질 수 있습니다.

## 7. 결론

이진 탐색은 정렬된 데이터에서 효율적으로 값을 찾는 강력한 알고리즘입니다. O(log n)의 시간 복잡도로 인해 대규모 데이터셋에서도 빠른 검색이 가능합니다. 하지만 데이터가 정렬되어 있어야 한다는 전제 조건이 있으므로, 데이터의 특성에 따라 적절히 사용해야 합니다.