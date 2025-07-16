# Java I/O 성능 비교

이 문서에서는 Java에서 자주 사용되는 입출력(I/O) 메소드들의 성능을 비교합니다. 특히 다음 항목들에 초점을 맞춥니다:
- 입력 속도: Scanner vs BufferedReader
- 문자열 파싱: StringTokenizer vs String.split()
- 출력 속도: System.out.println vs BufferedWriter.write

## 1. 입력 속도 비교: Scanner vs BufferedReader

### 1.1 개요

Java에서 텍스트 입력을 처리하는 두 가지 주요 클래스는 `Scanner`와 `BufferedReader`입니다. 이 두 클래스는 사용 방법과 성능 측면에서 중요한 차이점이 있습니다.

### 1.2 Scanner

`Scanner` 클래스는 Java 5부터 도입되었으며, 다양한 타입의 데이터를 쉽게 파싱할 수 있는 편리한 메소드를 제공합니다.

**특징:**
- 정규 표현식을 사용하여 입력을 토큰화
- 다양한 데이터 타입(int, double, String 등)을 직접 읽을 수 있는 메소드 제공
- 사용이 간편함
- 내부적으로 버퍼링을 사용하지만 크기가 작음

**예제 코드:**
```java
Scanner scanner = new Scanner(System.in);
int number = scanner.nextInt();
String text = scanner.next();
String line = scanner.nextLine();
scanner.close();
```

### 1.3 BufferedReader

`BufferedReader`는 문자 입력 스트림에서 텍스트를 효율적으로 읽기 위한 버퍼링된 문자 입력 스트림을 제공합니다.

**특징:**
- 대용량 버퍼를 사용하여 I/O 작업 최소화
- 한 번에 한 줄씩 읽는 `readLine()` 메소드 제공
- 기본 데이터 타입을 직접 읽을 수 없어 추가 변환 필요
- 동기화되어 있어 스레드 안전

**예제 코드:**
```java
BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
String line = reader.readLine();
int number = Integer.parseInt(reader.readLine());
reader.close();
```

### 1.4 성능 비교

다음은 `Scanner`와 `BufferedReader`의 성능을 비교한 결과입니다:

| 작업 | Scanner | BufferedReader | 성능 차이 |
|------|---------|----------------|-----------|
| 100만 줄 읽기 | ~1200ms | ~300ms | BufferedReader가 약 4배 빠름 |
| 100만 개 정수 읽기 | ~1500ms | ~450ms | BufferedReader가 약 3배 빠름 |

**성능 차이의 원인:**
1. `BufferedReader`는 8KB(8192자) 크기의 버퍼를 사용하여 I/O 작업을 최소화
2. `Scanner`는 정규 표현식 기반 파싱으로 인한 오버헤드 발생
3. `Scanner`는 작은 버퍼(1KB)를 사용하여 더 자주 I/O 작업 수행

### 1.5 사용 권장 사항

- **대용량 데이터 처리 시**: `BufferedReader` 사용 권장
- **간단한 입력이나 다양한 타입 파싱이 필요한 경우**: `Scanner` 사용 권장
- **성능이 중요한 애플리케이션(예: 알고리즘 문제 해결)**: `BufferedReader` 사용 권장

## 2. 문자열 파싱 비교: StringTokenizer vs String.split()

### 2.1 개요

문자열을 토큰으로 분리하는 작업은 입력 처리에서 자주 사용됩니다. Java에서는 `StringTokenizer`와 `String.split()` 두 가지 주요 방법을 제공합니다.

### 2.2 StringTokenizer

`StringTokenizer`는 Java 초기부터 존재하는 클래스로, 문자열을 지정된 구분자를 기준으로 토큰화합니다.

**특징:**
- 단순한 문자 기반 토큰화
- 정규 표현식을 사용하지 않음
- 토큰을 하나씩 순차적으로 처리
- 빈 토큰을 반환하지 않음

**예제 코드:**
```java
String input = "apple,banana,cherry";
StringTokenizer tokenizer = new StringTokenizer(input, ",");
while (tokenizer.hasMoreTokens()) {
    String token = tokenizer.nextToken();
    System.out.println(token);
}
```

### 2.3 String.split()

`String.split()`은 Java 1.4부터 도입된 메소드로, 정규 표현식을 사용하여 문자열을 분할합니다.

**특징:**
- 정규 표현식 기반 분할
- 모든 토큰을 한 번에 배열로 반환
- 더 유연한 패턴 매칭 가능
- 빈 토큰도 반환 가능

**예제 코드:**
```java
String input = "apple,banana,cherry";
String[] tokens = input.split(",");
for (String token : tokens) {
    System.out.println(token);
}
```

### 2.4 성능 비교

다음은 `StringTokenizer`와 `String.split()`의 성능을 비교한 결과입니다:

| 작업 | StringTokenizer | String.split() | 성능 차이 |
|------|----------------|----------------|-----------|
| 100만 개 문자열 토큰화 (단순 구분자) | ~150ms | ~450ms | StringTokenizer가 약 3배 빠름 |
| 100만 개 문자열 토큰화 (복잡한 패턴) | 해당 없음 | ~600ms | split()만 가능 |

**성능 차이의 원인:**
1. `StringTokenizer`는 단순 문자 비교만 수행하여 오버헤드가 적음
2. `String.split()`은 정규 표현식 엔진을 사용하여 추가 오버헤드 발생
3. `StringTokenizer`는 토큰을 필요할 때만 생성하는 반면, `split()`은 모든 토큰을 미리 생성

### 2.5 사용 권장 사항

- **단순한 구분자로 토큰화하는 경우**: `StringTokenizer` 사용 권장
- **정규 표현식 패턴이 필요한 경우**: `String.split()` 사용
- **성능이 중요한 경우**: `StringTokenizer` 사용 권장
- **코드 가독성과 유지보수성이 중요한 경우**: `String.split()` 사용 권장

## 3. 출력 속도 비교: System.out.println vs BufferedWriter.write

### 3.1 개요

Java에서 텍스트 출력을 처리하는 두 가지 주요 방법은 `System.out.println()`과 `BufferedWriter.write()`입니다. 이 두 방법은 사용 편의성과 성능 측면에서 차이가 있습니다.

### 3.2 System.out.println

`System.out.println()`은 Java에서 가장 널리 사용되는 출력 메소드입니다.

**특징:**
- 사용이 매우 간편함
- 자동으로 줄바꿈 추가
- 다양한 데이터 타입을 직접 출력 가능
- 내부적으로 동기화되어 있어 스레드 안전
- 출력마다 플러시(flush) 발생

**예제 코드:**
```java
System.out.println("Hello, World!");
System.out.println(42);
System.out.println(3.14);
```

### 3.3 BufferedWriter.write

`BufferedWriter`는 문자 출력 스트림에 버퍼링을 제공하여 효율적인 출력을 가능하게 합니다.

**특징:**
- 대용량 버퍼를 사용하여 I/O 작업 최소화
- 명시적으로 줄바꿈과 플러시를 관리해야 함
- 문자열만 직접 출력 가능 (다른 타입은 변환 필요)
- 버퍼가 가득 차거나 명시적으로 플러시할 때만 실제 I/O 발생

**예제 코드:**
```java
BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(System.out));
writer.write("Hello, World!");
writer.newLine();  // 줄바꿈 추가
writer.write(String.valueOf(42));
writer.newLine();
writer.flush();  // 버퍼 내용을 출력
writer.close();  // 사용 후 닫기
```

### 3.4 성능 비교

다음은 `System.out.println()`과 `BufferedWriter.write()`의 성능을 비교한 결과입니다:

| 작업 | System.out.println | BufferedWriter.write | 성능 차이 |
|------|-------------------|----------------------|-----------|
| 100만 줄 출력 | ~2500ms | ~400ms | BufferedWriter가 약 6배 빠름 |
| 대용량 텍스트 출력 | ~3000ms | ~500ms | BufferedWriter가 약 6배 빠름 |

**성능 차이의 원인:**
1. `BufferedWriter`는 8KB 크기의 버퍼를 사용하여 I/O 작업을 최소화
2. `System.out.println()`은 매 호출마다 플러시 작업 수행
3. `System.out`은 동기화되어 있어 멀티스레드 환경에서 추가 오버헤드 발생

### 3.5 사용 권장 사항

- **대용량 데이터 출력 시**: `BufferedWriter` 사용 권장
- **간단한 디버깅이나 콘솔 출력**: `System.out.println()` 사용 권장
- **성능이 중요한 애플리케이션**: `BufferedWriter` 사용 권장

## 4. 성능 테스트 코드 예제

다음은 위에서 설명한 각 방법의 성능을 테스트하는 코드 예제입니다.

### 4.1 입력 속도 테스트

```java
import java.io.*;
import java.util.*;

public class InputPerformanceTest {
    public static void main(String[] args) throws IOException {
        // 테스트 데이터 생성
        generateTestData("input_test.txt", 1000000);
        
        // Scanner 테스트
        long startTime = System.currentTimeMillis();
        try (Scanner scanner = new Scanner(new File("input_test.txt"))) {
            while (scanner.hasNextLine()) {
                scanner.nextLine();
            }
        }
        long scannerTime = System.currentTimeMillis() - startTime;
        System.out.println("Scanner 시간: " + scannerTime + "ms");
        
        // BufferedReader 테스트
        startTime = System.currentTimeMillis();
        try (BufferedReader reader = new BufferedReader(new FileReader("input_test.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // 아무 작업 없음
            }
        }
        long bufferedReaderTime = System.currentTimeMillis() - startTime;
        System.out.println("BufferedReader 시간: " + bufferedReaderTime + "ms");
        System.out.println("성능 차이: " + (double)scannerTime / bufferedReaderTime + "배");
    }
    
    private static void generateTestData(String filename, int lines) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (int i = 0; i < lines; i++) {
                writer.println("Line " + i + " of test data");
            }
        }
    }
}
```

### 4.2 문자열 파싱 테스트

```java
import java.io.*;
import java.util.*;

public class TokenizingPerformanceTest {
    public static void main(String[] args) {
        // 테스트 데이터 생성
        String[] testData = generateTestData(1000000);
        
        // StringTokenizer 테스트
        long startTime = System.currentTimeMillis();
        for (String line : testData) {
            StringTokenizer tokenizer = new StringTokenizer(line, ",");
            while (tokenizer.hasMoreTokens()) {
                tokenizer.nextToken();
            }
        }
        long tokenizerTime = System.currentTimeMillis() - startTime;
        System.out.println("StringTokenizer 시간: " + tokenizerTime + "ms");
        
        // String.split() 테스트
        startTime = System.currentTimeMillis();
        for (String line : testData) {
            String[] tokens = line.split(",");
            // 아무 작업 없음
        }
        long splitTime = System.currentTimeMillis() - startTime;
        System.out.println("String.split() 시간: " + splitTime + "ms");
        System.out.println("성능 차이: " + (double)splitTime / tokenizerTime + "배");
    }
    
    private static String[] generateTestData(int lines) {
        String[] data = new String[lines];
        for (int i = 0; i < lines; i++) {
            data[i] = "value1,value2,value3,value4,value5";
        }
        return data;
    }
}
```

### 4.3 출력 속도 테스트

```java
import java.io.*;

public class OutputPerformanceTest {
    public static void main(String[] args) throws IOException {
        // System.out.println 테스트
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            System.out.println("Line " + i + " of test data");
        }
        long printlnTime = System.currentTimeMillis() - startTime;
        System.out.println("System.out.println 시간: " + printlnTime + "ms");
        
        // BufferedWriter 테스트
        startTime = System.currentTimeMillis();
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output_test.txt")))) {
            for (int i = 0; i < 1000000; i++) {
                writer.write("Line " + i + " of test data");
                writer.newLine();
            }
        }
        long bufferedWriterTime = System.currentTimeMillis() - startTime;
        System.out.println("BufferedWriter 시간: " + bufferedWriterTime + "ms");
        System.out.println("성능 차이: " + (double)printlnTime / bufferedWriterTime + "배");
    }
}
```

## 5. 결론

Java I/O 성능 비교 결과를 요약하면 다음과 같습니다:

1. **입력 처리**:
   - `BufferedReader`가 `Scanner`보다 3-4배 빠름
   - 대용량 데이터 처리나 성능이 중요한 경우 `BufferedReader` 사용 권장
   - 편의성이 중요한 경우 `Scanner` 사용 가능

2. **문자열 파싱**:
   - 단순 구분자 토큰화에서는 `StringTokenizer`가 `String.split()`보다 약 3배 빠름
   - 정규 표현식이 필요한 경우에는 `String.split()` 사용
   - 성능이 중요한 경우 `StringTokenizer` 사용 권장

3. **출력 처리**:
   - `BufferedWriter`가 `System.out.println()`보다 약 6배 빠름
   - 대용량 출력이나 성능이 중요한 경우 `BufferedWriter` 사용 권장
   - 간단한 디버깅이나 콘솔 출력에는 `System.out.println()` 사용 가능

성능이 중요한 애플리케이션, 특히 대용량 데이터를 처리하는 경우에는 버퍼링된 I/O 클래스(`BufferedReader`, `BufferedWriter`)를 사용하는 것이 좋습니다. 그러나 코드의 가독성과 유지보수성도 중요한 요소이므로, 상황에 맞게 적절한 방법을 선택하는 것이 중요합니다.