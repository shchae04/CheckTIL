# 참조 지역성의 원리란 무엇인가요?

## 1. 한 줄 정의
- CPU와 프로그램은 "최근에 접근한 데이터"와 "가까이에 있는 데이터"를 반복해서 사용할 확률이 높다. 이 패턴을 이용하면 캐시 적중률이 올라가고 전체 실행 시간이 줄어든다.

---

## 2. 시간/공간 지역성 핵심 정리
- **시간 지역성(Temporal Locality)**: 방금 읽은 변수나 함수가 곧 다시 호출되는 경향. 반복문에서 동일한 조건 변수를 계속 참조하는 상황이 대표적이다.
- **공간 지역성(Spatial Locality)**: 어떤 주소를 접근하면 그 주변 주소도 함께 사용되는 경향. 배열을 앞에서 뒤로 순차 순회하는 케이스가 전형적이다.
- **설계 포인트**: CPU 캐시는 캐시 라인 단위로 인접 데이터를 미리 가져오므로, 지역성이 높을수록 캐시 히트율이 올라간다.

---

## 3. 자바 2차원 배열 순회 예시
### 3-1. AS-IS: 열 우선 순회 (캐시 미스 ↑)
- 각 반복에서 서로 다른 내부 배열(`int[]`)로 점프하며 공간 지역성이 무너진다.
- 동일한 연산이라도 캐시 미스로 인해 수십 배 이상 느려질 수 있다.

```java
int size = 10_000;
int[][] array = new int[size][size];

for (int col = 0; col < size; col++) {
    for (int row = 0; row < size; row++) {
        array[row][col]++; // 매번 다른 int[] 로 이동
    }
}
```

### 3-2. To-Be: 행 우선 순회 (캐시 적중률 ↑)
- 각 행을 연속적으로 접근해 동일 캐시 라인에서 데이터를 가져올 확률이 높다.
- 실제 측정 시 수십 ms vs. 수백 ms 수준으로 차이가 발생하는 경우가 많다.

```java
int size = 10_000;
int[][] array = new int[size][size];

for (int row = 0; row < size; row++) {
    for (int col = 0; col < size; col++) {
        array[row][col]++; // 같은 int[] 내부를 순차 접근
    }
}
```

### 3-3. 간단한 성능 측정 코드
- 동일한 크기의 배열을 두 번 순회하며 `System.nanoTime()` 으로 시간을 측정한다.
- JIT 영향을 줄이기 위해 워밍업 루프를 먼저 실행하고, G1 GC 사용 환경에서 약 200MB 정도의 힙을 가정한다.

```java
public class LocalityBenchmark {
    private static final int SIZE = 8_192; // 환경에 따라 조정
    private static final int WARMUP = 3;

    public static void main(String[] args) {
        int[][] grid = new int[SIZE][SIZE];

        // JIT 워밍업
        for (int i = 0; i < WARMUP; i++) {
            runRowMajor(grid);
            runColumnMajor(grid);
        }

        long rowNs = time(LocalityBenchmark::runRowMajor, grid);
        long colNs = time(LocalityBenchmark::runColumnMajor, grid);

        System.out.printf("Row-major: %.2f ms%n", rowNs / 1_000_000.0);
        System.out.printf("Column-major: %.2f ms%n", colNs / 1_000_000.0);
    }

    private static long time(java.util.function.Consumer<int[][]> task, int[][] grid) {
        long start = System.nanoTime();
        task.accept(grid);
        return System.nanoTime() - start;
    }

    private static void runRowMajor(int[][] grid) {
        for (int row = 0; row < SIZE; row++) {
            int[] current = grid[row];
            for (int col = 0; col < SIZE; col++) {
                current[col]++;
            }
        }
    }

    private static void runColumnMajor(int[][] grid) {
        for (int col = 0; col < SIZE; col++) {
            for (int row = 0; row < SIZE; row++) {
                grid[row][col]++;
            }
        }
    }
}
```

---

## 4. 백엔드 개발에서의 적용 팁
- **배치 처리**: 대량 데이터 연산 시 정렬, 파티셔닝, 버퍼 크기를 조정해 순차 접근을 유도한다.
- **데이터 구조 평탄화**: 이중 배열이나 링크드 리스트보다, 캐시 친화적인 배열/스트럭트 배열(Struct of Arrays)을 검토한다.
- **쿼리 튜닝**: DB에서도 인덱스 범위 스캔처럼 연속 블록을 읽도록 설계하면 디스크/버퍼 캐시 효율이 높아진다.

---

## 5. 예상 면접 팔로업 질문
1. CPU 캐시 계층(L1/L2/L3)과 지역성의 관계를 설명해보세요.
2. 자바 컬렉션을 순회할 때 지역성을 해치지 않는 방법은 무엇인가요?
3. 지역성을 개선하기 위한 JIT 최적화나 메모리 서브시스템 튜닝 경험이 있나요?

---

## 6. 핵심 요약
- 참조 지역성은 CPU/메모리 계층 구조에서 성능을 끌어올리는 기본 전제다.
- 접근 패턴을 순차적·집중적으로 만들면 캐시 미스를 줄이고 처리량을 높일 수 있다.
- 백엔드 코드를 짤 때도 데이터 레이아웃과 순회 순서를 항상 점검하자.
