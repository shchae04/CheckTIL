# 정렬 알고리즘 (Sorting Algorithms)

## 1. 버블 정렬 (Bubble Sort)

### 설명
버블 정렬은 인접한 두 원소를 비교하여 필요한 경우 위치를 교환하는 방식으로 동작합니다.

### Java 구현
```java
/**
 * 버블 정렬 구현 클래스
 * 인접한 두 원소를 비교하여 정렬하는 방식
 */
public class BubbleSort {
    /**
     * 버블 정렬을 수행하는 메소드
     * @param arr 정렬할 정수 배열
     */
    public static void bubbleSort(int[] arr) {
        int n = arr.length;  // 배열의 길이

        // 전체 원소에 대해 반복
        for (int i = 0; i < n-1; i++) {
            // 각 회전에서 인접한 원소들을 비교
            // i번째 회전 후에는 끝에서 i개의 원소는 이미 정렬되어 있으므로 n-i-1까지만 비교
            for (int j = 0; j < n-i-1; j++) {
                // 현재 원소가 다음 원소보다 크면 교환
                if (arr[j] > arr[j+1]) {
                    // 두 원소의 위치를 교환
                    int temp = arr[j];        // 임시 변수에 현재 원소 저장
                    arr[j] = arr[j+1];        // 다음 원소를 현재 위치로 이동
                    arr[j+1] = temp;          // 임시 저장한 원소를 다음 위치로 이동
                }
            }
        }
    }
}
```

### 특징
- 시간 복잡도: O(n²)
- 공간 복잡도: O(1)
- 장점: 구현이 매우 간단함
- 단점: 대규모 데이터에서는 비효율적

## 2. 선택 정렬 (Selection Sort)

### 설명
배열에서 최소값을 찾아 맨 앞으로 이동시키는 과정을 반복합니다.

### Java 구현
```java
/**
 * 선택 정렬 구현 클래스
 * 배열에서 최솟값을 찾아 맨 앞부터 순서대로 정렬하는 방식
 */
public class SelectionSort {
    /**
     * 선택 정렬을 수행하는 메소드
     * @param arr 정렬할 정수 배열
     */
    public static void selectionSort(int[] arr) {
        int n = arr.length;  // 배열의 길이

        // 배열의 모든 원소에 대해 반복
        for (int i = 0; i < n-1; i++) {
            int minIdx = i;  // 현재 구간에서 최솟값의 인덱스를 저장

            // i+1부터 끝까지 최솟값을 찾음
            for (int j = i+1; j < n; j++) {
                // 현재 최솟값보다 더 작은 값을 찾으면 인덱스 업데이트
                if (arr[j] < arr[minIdx]) {
                    minIdx = j;
                }
            }

            // 찾은 최솟값을 현재 위치(i)로 이동
            int temp = arr[minIdx];     // 최솟값을 임시 변수에 저장
            arr[minIdx] = arr[i];       // 현재 위치의 값을 최솟값 위치로 이동
            arr[i] = temp;              // 최솟값을 현재 위치로 이동
        }
    }
}
```

### 특징
- 시간 복잡도: O(n²)
- 공간 복잡도: O(1)
- 장점: 메모리 사용이 적음
- 단점: 큰 데이터셋에서 비효율적

## 3. 삽입 정렬 (Insertion Sort)

### 설명
배열의 모든 요소를 앞에서부터 차례대로 이미 정렬된 부분과 비교하여 자신의 위치를 찾아 삽입합니다.

### Java 구현
```java
/**
 * 삽입 정렬 구현 클래스
 * 정렬되지 않은 부분에서 원소를 하나씩 가져와 정렬된 부분의 적절한 위치에 삽입하는 방식
 */
public class InsertionSort {
    /**
     * 삽입 정렬을 수행하는 메소드
     * @param arr 정렬할 정수 배열
     */
    public static void insertionSort(int[] arr) {
        int n = arr.length;  // 배열의 길이

        // 두 번째 원소부터 시작하여 끝까지 반복
        for (int i = 1; i < n; i++) {
            int key = arr[i];  // 현재 삽입할 원소를 key로 저장
            int j = i - 1;     // key 이전의 원소부터 비교 시작

            // key보다 큰 원소들을 뒤로 한 칸씩 이동
            while (j >= 0 && arr[j] > key) {
                arr[j + 1] = arr[j];  // 한 칸 뒤로 이동
                j = j - 1;            // 이전 원소로 이동
            }

            arr[j + 1] = key;  // key를 적절한 위치에 삽입
        }
    }
}
```

### 특징
- 시간 복잡도: O(n²)
- 공간 복잡도: O(1)
- 장점: 작은 데이터셋에서 효율적, 안정적인 정렬
- 단점: 큰 데이터셋에서는 비효율적

## 4. 퀵 정렬 (Quick Sort)

### 설명
분할 정복 알고리즘으로, 피벗을 기준으로 배열을 분할하고 재귀적으로 정렬합니다.

### Java 구현
```java
/**
 * 퀵 정렬 구현 클래스
 * 피벗을 기준으로 작은 값과 큰 값을 분할하여 정렬하는 방식
 */
public class QuickSort {
    /**
     * 퀵 정렬을 수행하는 메소드
     * @param arr 정렬할 정수 배열
     * @param low 정렬할 범위의 시작 인덱스
     * @param high 정렬할 범위의 끝 인덱스
     */
    public static void quickSort(int[] arr, int low, int high) {
        if (low < high) {  // 정렬할 원소가 2개 이상인 경우에만 수행
            // 피벗을 기준으로 배열을 분할하고 피벗의 최종 위치를 받음
            int pi = partition(arr, low, high);

            // 피벗을 기준으로 왼쪽 부분 배열 정렬
            quickSort(arr, low, pi - 1);
            // 피벗을 기준으로 오른쪽 부분 배열 정렬
            quickSort(arr, pi + 1, high);
        }
    }

    /**
     * 배열을 피벗을 기준으로 분할하는 메소드
     * @param arr 분할할 배열
     * @param low 분할할 범위의 시작 인덱스
     * @param high 분할할 범위의 끝 인덱스
     * @return 피벗의 최종 위치
     */
    private static int partition(int[] arr, int low, int high) {
        int pivot = arr[high];  // 마지막 원소를 피벗으로 선택
        int i = (low - 1);      // 피벗보다 작은 원소들의 경계를 나타내는 인덱스

        // low부터 high-1까지 순회하며 피벗과 비교
        for (int j = low; j < high; j++) {
            // 현재 원소가 피벗보다 작으면
            if (arr[j] < pivot) {
                i++;  // 경계를 오른쪽으로 이동
                // 현재 원소를 경계 왼쪽으로 이동
                int temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }

        // 피벗을 올바른 위치로 이동
        int temp = arr[i + 1];
        arr[i + 1] = arr[high];
        arr[high] = temp;

        return i + 1;  // 피벗의 최종 위치 반환
    }
}
```

### 특징
- 시간 복잡도: 평균 O(n log n), 최악 O(n²)
- 공간 복잡도: O(log n)
- 장점: 평균적으로 매우 빠름
- 단점: 최악의 경우 O(n²), 불안정 정렬

## 5. 병합 정렬 (Merge Sort)

### 설명
분할 정복 방법을 사용하여 배열을 절반으로 나누고, 정렬하여 병합합니다.

### Java 구현
```java
/**
 * 병합 정렬 구현 클래스
 * 배열을 반으로 나누고 정렬하여 다시 병합하는 방식
 */
public class MergeSort {
    /**
     * 병합 정렬을 수행하는 메소드
     * @param arr 정렬할 정수 배열
     * @param l 정렬할 범위의 시작 인덱스
     * @param r 정렬할 범위의 끝 인덱스
     */
    public static void mergeSort(int[] arr, int l, int r) {
        if (l < r) {  // 정렬할 원소가 2개 이상인 경우에만 수행
            int m = (l + r) / 2;  // 중간 지점 계산

            // 배열의 왼쪽 부분 정렬
            mergeSort(arr, l, m);
            // 배열의 오른쪽 부분 정렬
            mergeSort(arr, m + 1, r);
            // 정렬된 두 부분을 병합
            merge(arr, l, m, r);
        }
    }

    /**
     * 두 정렬된 부분 배열을 병합하는 메소드
     * @param arr 병합할 배열
     * @param l 왼쪽 부분의 시작 인덱스
     * @param m 왼쪽 부분의 끝 인덱스
     * @param r 오른쪽 부분의 끝 인덱스
     */
    private static void merge(int[] arr, int l, int m, int r) {
        // 왼쪽과 오른쪽 부분 배열의 크기 계산
        int n1 = m - l + 1;  // 왼쪽 부분의 크기
        int n2 = r - m;      // 오른쪽 부분의 크기

        // 임시 배열 생성
        int[] L = new int[n1];  // 왼쪽 부분을 저장할 임시 배열
        int[] R = new int[n2];  // 오른쪽 부분을 저장할 임시 배열

        // 데이터를 임시 배열로 복사
        for (int i = 0; i < n1; ++i)
            L[i] = arr[l + i];  // 왼쪽 부분 복사
        for (int j = 0; j < n2; ++j)
            R[j] = arr[m + 1 + j];  // 오른쪽 부분 복사

        // 두 임시 배열을 병합하여 원래 배열에 저장
        int i = 0;  // 왼쪽 배열의 인덱스
        int j = 0;  // 오른쪽 배열의 인덱스
        int k = l;  // 병합된 배열의 인덱스

        // 두 배열의 원소를 비교하여 작은 값부터 병합
        while (i < n1 && j < n2) {
            if (L[i] <= R[j]) {
                arr[k] = L[i];  // 왼쪽 배열의 원소가 더 작으면 추가
                i++;
            } else {
                arr[k] = R[j];  // 오른쪽 배열의 원소가 더 작으면 추가
                j++;
            }
            k++;
        }

        // 왼쪽 배열에 남은 원소들을 복사
        while (i < n1) {
            arr[k] = L[i];
            i++;
            k++;
        }

        // 오른쪽 배열에 남은 원소들을 복사
        while (j < n2) {
            arr[k] = R[j];
            j++;
            k++;
        }
    }
}
```

### 특징
- 시간 복잡도: O(n log n)
- 공간 복잡도: O(n)
- 장점: 안정적인 정렬, 일정한 성능
- 단점: 추가 메모리 공간 필요

## 6. 힙 정렬 (Heap Sort)

### 설명
최대 힙 트리나 최소 힙 트리를 구성해 정렬을 하는 방법입니다.

### Java 구현
```java
/**
 * 힙 정렬 구현 클래스
 * 최대 힙을 구성하여 정렬하는 방식
 */
public class HeapSort {
    /**
     * 힙 정렬을 수행하는 메소드
     * @param arr 정렬할 정수 배열
     */
    public static void heapSort(int[] arr) {
        int n = arr.length;  // 배열의 길이

        // 초기 최대 힙 구성
        // 마지막 비단말 노드부터 시작하여 루트까지 힙 속성을 만족하도록 조정
        for (int i = n / 2 - 1; i >= 0; i--)
            heapify(arr, n, i);

        // 힙에서 원소를 하나씩 꺼내어 배열의 뒤쪽부터 저장
        for (int i = n-1; i > 0; i--) {
            // 루트(최댓값)를 배열의 마지막 원소와 교환
            int temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;

            // 루트 노드에 대해 힙 속성을 다시 만족하도록 조정
            heapify(arr, i, 0);
        }
    }

    /**
     * 특정 노드를 루트로 하는 서브트리를 최대 힙으로 만드는 메소드
     * @param arr 힙을 구성할 배열
     * @param n 힙의 크기
     * @param i 현재 노드의 인덱스
     */
    private static void heapify(int[] arr, int n, int i) {
        int largest = i;      // 현재 노드를 최댓값으로 가정
        int l = 2*i + 1;     // 왼쪽 자식 노드의 인덱스
        int r = 2*i + 2;     // 오른쪽 자식 노드의 인덱스

        // 왼쪽 자식이 현재 최댓값보다 크면 largest 업데이트
        if (l < n && arr[l] > arr[largest])
            largest = l;

        // 오른쪽 자식이 현재 최댓값보다 크면 largest 업데이트
        if (r < n && arr[r] > arr[largest])
            largest = r;

        // largest가 변경되었다면 (= 자식 노드 중 더 큰 값이 있다면)
        if (largest != i) {
            // 현재 노드와 최댓값 노드를 교환
            int swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;

            // 교환된 자식 노드에 대해 재귀적으로 heapify 수행
            heapify(arr, n, largest);
        }
    }
}
```

### 특징
- 시간 복잡도: O(n log n)
- 공간 복잡도: O(1)
- 장점: 추가 메모리가 거의 필요 없음
- 단점: 불안정 정렬

## 정렬 알고리즘 비교

| 알고리즘 | 평균 시간 복잡도 | 최악 시간 복잡도 | 공간 복잡도 | 안정성 |
|---------|--------------|--------------|-----------|--------|
| 버블 정렬 | O(n²) | O(n²) | O(1) | 안정 |
| 선택 정렬 | O(n²) | O(n²) | O(1) | 불안정 |
| 삽입 정렬 | O(n²) | O(n²) | O(1) | 안정 |
| 퀵 정렬 | O(n log n) | O(n²) | O(log n) | 불안정 |
| 병합 정렬 | O(n log n) | O(n log n) | O(n) | 안정 |
| 힙 정렬 | O(n log n) | O(n log n) | O(1) | 불안정 |

## 결론
각각의 정렬 알고리즘은 고유한 장단점을 가지고 있습니다. 실제 사용 시에는 데이터의 크기, 정렬 안정성 요구사항, 메모리 제약 등을 고려하여 적절한 알고리즘을 선택해야 합니다.
