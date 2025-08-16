# 참조 지역성(Locality of Reference)

## 1. 한 줄 요약
- CPU는 "최근에 접근한 곳"(시간 지역성)과 "가까이에 있는 곳"(공간 지역성)을 다시 접근하는 경향이 있다. 이 특성을 이용해 데이터/코드를 배치하고 순회하면 캐시 적중률이 올라가고 성능이 좋아진다.

---

## 2. 개념
- 시간 지역성(Temporal Locality): 최근에 접근한 메모리(데이터/코드)에 곧 다시 접근할 가능성이 높음.
- 공간 지역성(Spatial Locality): 어떤 주소를 접근하면, 그 주변(인접한 주소들)도 곧 접근할 가능성이 높음.

이 원리를 이용하는 대표 구조가 CPU 캐시(L1/L2/L3)이며, 캐시는 "라인(line)" 단위로 인접한 데이터를 미리 끌어와 적중률을 높인다.

---

## 3. 자바 2차원 배열과 주의점
- 자바의 2차원 배열은 "배열의 배열"이다. 즉 `int[][]` 는 `int[]` 객체들의 참조 배열이며, 내부 `int[]` 들은 메모리에서 연속적으로 배치된다는 보장이 없다.
- 따라서 열(column)을 먼저 순회하며 서로 다른 `int[]` 들을 건너뛰게 되면 공간 지역성이 나빠져 캐시 히트율이 떨어질 수 있다.

---

## 4. 예제: 열 우선 순회 → 행 우선 순회로 개선

아래 코드는 열을 먼저 순회해(열-주도) 캐시 지역성이 좋지 않다.

```
public class LocalityTest {
    @Test
    void test() {
        int size = 10240;
        int[][] array = new int[size][size];

        long beforeTime = System.currentTimeMillis();

        for (int j = 0; j < size; j++) {
            for (int i = 0; i < size; i++) {
                array[i][j]++;
            }
        }

        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        System.out.println("수행시간(ms) : " + diffTime); // 예: 577ms
    }
}
```

행을 먼저 순회(행-주도)하면 각 `int[]` 내부를 연속적으로 접근하여 공간 지역성이 좋아지고 보통 훨씬 빠르다.

```
public class LocalityTest {
    @Test
    void test() {
        int size = 10240;
        int[][] array = new int[size][size];

        long beforeTime = System.currentTimeMillis();

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                array[i][j]++;
            }
        }

        long afterTime = System.currentTimeMillis();
        long diffTime = afterTime - beforeTime;
        System.out.println("수행시간(ms) : " + diffTime); // 예: 28ms (환경에 따라 달라짐)
    }
}
```

- 주의: 실제 수치는 JDK/옵션/하드웨어/배열 크기/JIT 워밍업 여부 등에 따라 달라진다.

---

## 5. 더 나은 측정/최적화 팁
- 정밀한 시간 측정: `System.nanoTime()` 사용을 고려.
- JIT 워밍업: 실제 측정 전에 동일 코드를 여러 번 수행하여 JIT 최적화를 유도.
- 1차원 배열로 평탄화: 진짜 연속 메모리 접근을 원한다면 2D 대신 1D를 사용.

```
int n = 10240;
int[] flat = new int[n * n];
for (int i = 0; i < n; i++) {
    int rowBase = i * n;
    for (int j = 0; j < n; j++) {
        flat[rowBase + j]++;
    }
}
```

- 타일링(Blocking): 매우 큰 데이터에서 캐시 친화적으로 접근하려면 블록 단위로 나눠 순회.

```
int n = 10240, B = 64; // B는 캐시/라인 크기에 맞춰 실험적으로 조정
for (int ii = 0; ii < n; ii += B) {
    for (int jj = 0; jj < n; jj += B) {
        for (int i = ii; i < Math.min(ii + B, n); i++) {
            for (int j = jj; j < Math.min(jj + B, n); j++) {
                array[i][j]++;
            }
        }
    }
}
```

---

## 6. 백엔드 개발에서의 시사점
- 캐시 친화적 데이터 레이아웃: 핫 패스에서 데이터 구조를 인접하게 배치하면 GC/CPU 부담을 줄이고 지연을 낮출 수 있다.
- 배치/집계 연산: 대량 반복 처리 시 접근 순서(정렬, 파티셔닝, 배치 크기)를 조정해 캐시 효율을 높인다.
- 프리페치/스트리밍: I/O 계층에서도 순차 접근을 선호해 OS/스토리지 캐시 효율을 높인다.

---

## 7. 핵심 정리
- 참조 지역성은 캐시 효율을 좌우한다.
- 자바 2차원 배열은 "연속 메모리 보장 X" → 행 우선 순회가 유리하다.
- 필요시 1D 평탄화/타일링/정확한 측정으로 추가 개선 가능.
