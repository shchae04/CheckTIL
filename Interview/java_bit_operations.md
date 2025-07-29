# Java 비트 연산 (Bit Operations)

## 개념

비트 연산(Bit Operations)은 개별 비트 레벨에서 데이터를 조작하는 연산입니다. Java에서는 정수형 데이터 타입(byte, short, int, long)에 대해 비트 연산을 수행할 수 있으며, 이는 매우 빠른 연산 속도를 제공합니다.

## 비트 연산의 종류

### 1. 비트 AND 연산 (&)
두 비트가 모두 1일 때만 결과가 1이 되는 연산입니다.

```java
public class BitAndExample {
    public static void main(String[] args) {
        int a = 12;  // 1100 (이진수)
        int b = 10;  // 1010 (이진수)
        int result = a & b;  // 1000 = 8 (십진수)
        
        System.out.println("12 & 10 = " + result); // 출력: 8
        System.out.println("이진수: " + Integer.toBinaryString(result)); // 출력: 1000
    }
}
```

### 2. 비트 OR 연산 (|)
두 비트 중 하나라도 1이면 결과가 1이 되는 연산입니다.

```java
public class BitOrExample {
    public static void main(String[] args) {
        int a = 12;  // 1100 (이진수)
        int b = 10;  // 1010 (이진수)
        int result = a | b;  // 1110 = 14 (십진수)
        
        System.out.println("12 | 10 = " + result); // 출력: 14
        System.out.println("이진수: " + Integer.toBinaryString(result)); // 출력: 1110
    }
}
```

### 3. 비트 XOR 연산 (^)
두 비트가 다를 때만 결과가 1이 되는 연산입니다.

```java
public class BitXorExample {
    public static void main(String[] args) {
        int a = 12;  // 1100 (이진수)
        int b = 10;  // 1010 (이진수)
        int result = a ^ b;  // 0110 = 6 (십진수)
        
        System.out.println("12 ^ 10 = " + result); // 출력: 6
        System.out.println("이진수: " + Integer.toBinaryString(result)); // 출력: 110
        
        // XOR의 특성: A ^ B ^ B = A
        int original = 42;
        int key = 123;
        int encrypted = original ^ key;
        int decrypted = encrypted ^ key;
        System.out.println("원본: " + original + ", 복호화: " + decrypted); // 같은 값
    }
}
```

### 4. 비트 NOT 연산 (~)
모든 비트를 반전시키는 연산입니다.

```java
public class BitNotExample {
    public static void main(String[] args) {
        int a = 12;  // 00000000000000000000000000001100
        int result = ~a;  // 11111111111111111111111111110011 = -13
        
        System.out.println("~12 = " + result); // 출력: -13
        
        // byte 타입에서의 NOT 연산
        byte b = 12;
        byte notB = (byte) ~b;
        System.out.println("~(byte)12 = " + notB); // 출력: -13
    }
}
```

### 5. 왼쪽 시프트 연산 (<<)
비트를 왼쪽으로 이동시키는 연산입니다. 2의 거듭제곱 곱셈과 같은 효과를 가집니다.

```java
public class LeftShiftExample {
    public static void main(String[] args) {
        int a = 5;  // 101 (이진수)
        int result1 = a << 1;  // 1010 = 10 (십진수) = 5 * 2^1
        int result2 = a << 2;  // 10100 = 20 (십진수) = 5 * 2^2
        int result3 = a << 3;  // 101000 = 40 (십진수) = 5 * 2^3
        
        System.out.println("5 << 1 = " + result1); // 출력: 10
        System.out.println("5 << 2 = " + result2); // 출력: 20
        System.out.println("5 << 3 = " + result3); // 출력: 40
        
        // 성능 비교: 곱셈 vs 시프트 연산
        long startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            int multiply = a * 8;
        }
        long multiplyTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < 1000000; i++) {
            int shift = a << 3;
        }
        long shiftTime = System.nanoTime() - startTime;
        
        System.out.println("곱셈 시간: " + multiplyTime + "ns");
        System.out.println("시프트 시간: " + shiftTime + "ns");
    }
}
```

### 6. 오른쪽 시프트 연산 (>>)
비트를 오른쪽으로 이동시키는 연산입니다. 부호를 유지하며 2의 거듭제곱 나눗셈과 같은 효과를 가집니다.

```java
public class RightShiftExample {
    public static void main(String[] args) {
        int a = 20;  // 10100 (이진수)
        int result1 = a >> 1;  // 1010 = 10 (십진수) = 20 / 2^1
        int result2 = a >> 2;  // 101 = 5 (십진수) = 20 / 2^2
        
        System.out.println("20 >> 1 = " + result1); // 출력: 10
        System.out.println("20 >> 2 = " + result2); // 출력: 5
        
        // 음수에서의 오른쪽 시프트 (부호 유지)
        int negative = -20;
        int negResult = negative >> 2;
        System.out.println("-20 >> 2 = " + negResult); // 출력: -5
        System.out.println("이진수: " + Integer.toBinaryString(negResult));
    }
}
```

### 7. 무부호 오른쪽 시프트 연산 (>>>)
비트를 오른쪽으로 이동시키되, 부호를 무시하고 0으로 채우는 연산입니다.

```java
public class UnsignedRightShiftExample {
    public static void main(String[] args) {
        int a = -20;
        int signedShift = a >> 2;      // 부호 유지
        int unsignedShift = a >>> 2;   // 부호 무시
        
        System.out.println("-20 >> 2 = " + signedShift);   // 출력: -5
        System.out.println("-20 >>> 2 = " + unsignedShift); // 출력: 1073741819
        
        System.out.println("부호 있는 시프트 이진수: " + Integer.toBinaryString(signedShift));
        System.out.println("무부호 시프트 이진수: " + Integer.toBinaryString(unsignedShift));
    }
}
```

## 실무 활용 사례

### 1. 플래그(Flag) 관리
여러 개의 불린 값을 하나의 정수로 관리할 때 사용합니다.

```java
public class FlagManager {
    // 권한 플래그 정의
    public static final int READ_PERMISSION = 1;    // 001
    public static final int WRITE_PERMISSION = 2;   // 010
    public static final int EXECUTE_PERMISSION = 4; // 100
    
    private int permissions = 0;
    
    // 권한 추가
    public void addPermission(int permission) {
        permissions |= permission;
    }
    
    // 권한 제거
    public void removePermission(int permission) {
        permissions &= ~permission;
    }
    
    // 권한 확인
    public boolean hasPermission(int permission) {
        return (permissions & permission) != 0;
    }
    
    // 권한 토글
    public void togglePermission(int permission) {
        permissions ^= permission;
    }
    
    public static void main(String[] args) {
        FlagManager manager = new FlagManager();
        
        // 읽기, 쓰기 권한 추가
        manager.addPermission(READ_PERMISSION | WRITE_PERMISSION);
        
        System.out.println("읽기 권한: " + manager.hasPermission(READ_PERMISSION));   // true
        System.out.println("쓰기 권한: " + manager.hasPermission(WRITE_PERMISSION));  // true
        System.out.println("실행 권한: " + manager.hasPermission(EXECUTE_PERMISSION)); // false
        
        // 실행 권한 토글
        manager.togglePermission(EXECUTE_PERMISSION);
        System.out.println("실행 권한 (토글 후): " + manager.hasPermission(EXECUTE_PERMISSION)); // true
    }
}
```

### 2. 색상 처리 (RGB)
RGB 색상 값을 하나의 정수로 저장하고 조작할 때 사용합니다.

```java
public class ColorProcessor {
    
    public static int createColor(int red, int green, int blue) {
        return (red << 16) | (green << 8) | blue;
    }
    
    public static int createColor(int red, int green, int blue, int alpha) {
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }
    
    public static int getRed(int color) {
        return (color >> 16) & 0xFF;
    }
    
    public static int getGreen(int color) {
        return (color >> 8) & 0xFF;
    }
    
    public static int getBlue(int color) {
        return color & 0xFF;
    }
    
    public static int getAlpha(int color) {
        return (color >> 24) & 0xFF;
    }
    
    public static void main(String[] args) {
        // RGB 색상 생성 (빨강: 255, 초록: 128, 파랑: 64)
        int color = createColor(255, 128, 64);
        
        System.out.println("색상 값: " + color);
        System.out.println("16진수: 0x" + Integer.toHexString(color).toUpperCase());
        
        // 색상 분해
        System.out.println("빨강: " + getRed(color));   // 255
        System.out.println("초록: " + getGreen(color)); // 128
        System.out.println("파랑: " + getBlue(color));  // 64
        
        // ARGB 색상 (알파 채널 포함)
        int argbColor = createColor(255, 128, 64, 200);
        System.out.println("알파: " + getAlpha(argbColor)); // 200
    }
}
```

### 3. 해시 함수 구현
비트 연산을 활용한 효율적인 해시 함수 구현입니다.

```java
public class BitHashFunction {
    
    // 간단한 해시 함수
    public static int simpleHash(String str) {
        int hash = 0;
        for (int i = 0; i < str.length(); i++) {
            hash = (hash << 5) - hash + str.charAt(i);
            hash = hash & hash; // 32비트로 제한
        }
        return hash;
    }
    
    // FNV-1a 해시 알고리즘
    public static int fnv1aHash(String str) {
        final int FNV_PRIME = 16777619;
        final int FNV_OFFSET_BASIS = -2128831035; // 2166136261의 부호 있는 표현
        
        int hash = FNV_OFFSET_BASIS;
        for (int i = 0; i < str.length(); i++) {
            hash ^= str.charAt(i);
            hash *= FNV_PRIME;
        }
        return hash;
    }
    
    public static void main(String[] args) {
        String[] testStrings = {"hello", "world", "java", "bit", "operations"};
        
        System.out.println("Simple Hash vs FNV-1a Hash:");
        for (String str : testStrings) {
            int simpleHash = simpleHash(str);
            int fnvHash = fnv1aHash(str);
            
            System.out.printf("%-10s: Simple=%10d, FNV-1a=%10d%n", 
                            str, simpleHash, fnvHash);
        }
    }
}
```

### 4. 비트마스크를 이용한 집합 연산
작은 범위의 정수 집합을 효율적으로 관리할 때 사용합니다.

```java
public class BitSetOperations {
    
    // 집합에 원소 추가
    public static int addElement(int set, int element) {
        return set | (1 << element);
    }
    
    // 집합에서 원소 제거
    public static int removeElement(int set, int element) {
        return set & ~(1 << element);
    }
    
    // 원소 존재 확인
    public static boolean contains(int set, int element) {
        return (set & (1 << element)) != 0;
    }
    
    // 집합의 합집합
    public static int union(int set1, int set2) {
        return set1 | set2;
    }
    
    // 집합의 교집합
    public static int intersection(int set1, int set2) {
        return set1 & set2;
    }
    
    // 집합의 차집합
    public static int difference(int set1, int set2) {
        return set1 & ~set2;
    }
    
    // 집합의 크기 (원소 개수)
    public static int size(int set) {
        return Integer.bitCount(set);
    }
    
    // 집합 출력
    public static void printSet(int set, String name) {
        System.out.print(name + ": {");
        boolean first = true;
        for (int i = 0; i < 32; i++) {
            if (contains(set, i)) {
                if (!first) System.out.print(", ");
                System.out.print(i);
                first = false;
            }
        }
        System.out.println("}");
    }
    
    public static void main(String[] args) {
        int setA = 0;
        int setB = 0;
        
        // 집합 A = {1, 3, 5, 7}
        setA = addElement(setA, 1);
        setA = addElement(setA, 3);
        setA = addElement(setA, 5);
        setA = addElement(setA, 7);
        
        // 집합 B = {2, 3, 6, 7}
        setB = addElement(setB, 2);
        setB = addElement(setB, 3);
        setB = addElement(setB, 6);
        setB = addElement(setB, 7);
        
        printSet(setA, "Set A");
        printSet(setB, "Set B");
        printSet(union(setA, setB), "A ∪ B");
        printSet(intersection(setA, setB), "A ∩ B");
        printSet(difference(setA, setB), "A - B");
        
        System.out.println("Set A 크기: " + size(setA));
        System.out.println("Set B 크기: " + size(setB));
        System.out.println("3이 Set A에 있는가? " + contains(setA, 3));
    }
}
```

### 5. 빠른 거듭제곱 (Fast Exponentiation)
비트 연산을 활용한 효율적인 거듭제곱 계산입니다.

```java
public class FastExponentiation {
    
    // 일반적인 거듭제곱 (O(n))
    public static long normalPower(long base, int exponent) {
        long result = 1;
        for (int i = 0; i < exponent; i++) {
            result *= base;
        }
        return result;
    }
    
    // 빠른 거듭제곱 (O(log n))
    public static long fastPower(long base, int exponent) {
        long result = 1;
        while (exponent > 0) {
            // 지수가 홀수인 경우
            if ((exponent & 1) == 1) {
                result *= base;
            }
            base *= base;
            exponent >>= 1; // 지수를 2로 나눔
        }
        return result;
    }
    
    // 모듈러 거듭제곱 (큰 수 처리용)
    public static long modularPower(long base, int exponent, long modulus) {
        long result = 1;
        base %= modulus;
        while (exponent > 0) {
            if ((exponent & 1) == 1) {
                result = (result * base) % modulus;
            }
            base = (base * base) % modulus;
            exponent >>= 1;
        }
        return result;
    }
    
    public static void main(String[] args) {
        long base = 2;
        int exponent = 20;
        
        // 성능 비교
        long startTime = System.nanoTime();
        long normalResult = normalPower(base, exponent);
        long normalTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        long fastResult = fastPower(base, exponent);
        long fastTime = System.nanoTime() - startTime;
        
        System.out.println("2^20 결과:");
        System.out.println("일반 거듭제곱: " + normalResult + " (시간: " + normalTime + "ns)");
        System.out.println("빠른 거듭제곱: " + fastResult + " (시간: " + fastTime + "ns)");
        
        // 큰 수에서의 모듈러 거듭제곱
        long modResult = modularPower(2, 100, 1000000007);
        System.out.println("2^100 mod 1000000007 = " + modResult);
    }
}
```

## 성능 고려사항

### 1. 비트 연산의 속도
비트 연산은 CPU에서 직접 지원하는 가장 빠른 연산 중 하나입니다.

```java
public class PerformanceComparison {
    private static final int ITERATIONS = 10_000_000;
    
    public static void main(String[] args) {
        int a = 1000;
        
        // 곱셈 vs 시프트 연산
        long startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            int result = a * 8;
        }
        long multiplyTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            int result = a << 3;
        }
        long shiftTime = System.nanoTime() - startTime;
        
        // 나눗셈 vs 시프트 연산
        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            int result = a / 8;
        }
        long divideTime = System.nanoTime() - startTime;
        
        startTime = System.nanoTime();
        for (int i = 0; i < ITERATIONS; i++) {
            int result = a >> 3;
        }
        long rightShiftTime = System.nanoTime() - startTime;
        
        System.out.println("성능 비교 (" + ITERATIONS + "회 반복):");
        System.out.println("곱셈 (a * 8): " + multiplyTime + "ns");
        System.out.println("시프트 (a << 3): " + shiftTime + "ns");
        System.out.println("나눗셈 (a / 8): " + divideTime + "ns");
        System.out.println("시프트 (a >> 3): " + rightShiftTime + "ns");
        
        System.out.println("\n속도 향상:");
        System.out.printf("곱셈 대비 시프트: %.2fx 빠름%n", (double)multiplyTime / shiftTime);
        System.out.printf("나눗셈 대비 시프트: %.2fx 빠름%n", (double)divideTime / rightShiftTime);
    }
}
```

### 2. 메모리 효율성
비트 연산을 활용하면 메모리 사용량을 크게 줄일 수 있습니다.

```java
public class MemoryEfficiency {
    
    // 일반적인 불린 배열 사용
    static class BooleanArraySet {
        private boolean[] data;
        
        public BooleanArraySet(int size) {
            this.data = new boolean[size];
        }
        
        public void add(int element) {
            if (element >= 0 && element < data.length) {
                data[element] = true;
            }
        }
        
        public boolean contains(int element) {
            return element >= 0 && element < data.length && data[element];
        }
        
        public int getMemoryUsage() {
            return data.length; // 각 boolean은 1바이트
        }
    }
    
    // 비트셋 사용
    static class BitSet {
        private int[] data;
        private int size;
        
        public BitSet(int size) {
            this.size = size;
            this.data = new int[(size + 31) / 32]; // 32비트씩 저장
        }
        
        public void add(int element) {
            if (element >= 0 && element < size) {
                int index = element / 32;
                int bit = element % 32;
                data[index] |= (1 << bit);
            }
        }
        
        public boolean contains(int element) {
            if (element < 0 || element >= size) return false;
            int index = element / 32;
            int bit = element % 32;
            return (data[index] & (1 << bit)) != 0;
        }
        
        public int getMemoryUsage() {
            return data.length * 4; // 각 int는 4바이트
        }
    }
    
    public static void main(String[] args) {
        int size = 1000;
        
        BooleanArraySet boolSet = new BooleanArraySet(size);
        BitSet bitSet = new BitSet(size);
        
        // 동일한 데이터 추가
        for (int i = 0; i < size; i += 2) {
            boolSet.add(i);
            bitSet.add(i);
        }
        
        System.out.println("메모리 사용량 비교 (1000개 원소):");
        System.out.println("Boolean 배열: " + boolSet.getMemoryUsage() + " bytes");
        System.out.println("BitSet: " + bitSet.getMemoryUsage() + " bytes");
        System.out.printf("메모리 절약: %.1f%%%n", 
            (1.0 - (double)bitSet.getMemoryUsage() / boolSet.getMemoryUsage()) * 100);
        
        // 기능 검증
        System.out.println("\n기능 검증:");
        System.out.println("500이 포함되어 있는가?");
        System.out.println("Boolean 배열: " + boolSet.contains(500));
        System.out.println("BitSet: " + bitSet.contains(500));
        
        System.out.println("501이 포함되어 있는가?");
        System.out.println("Boolean 배열: " + boolSet.contains(501));
        System.out.println("BitSet: " + bitSet.contains(501));
    }
}
```

## 주의사항

### 1. 오버플로우 주의
시프트 연산 시 오버플로우가 발생할 수 있습니다.

```java
public class OverflowExample {
    public static void main(String[] args) {
        int value = Integer.MAX_VALUE; // 2147483647
        System.out.println("원본 값: " + value);
        System.out.println("이진수: " + Integer.toBinaryString(value));
        
        // 왼쪽 시프트로 인한 오버플로우
        int shifted = value << 1;
        System.out.println("시프트 후: " + shifted); // -2 (오버플로우 발생)
        System.out.println("이진수: " + Integer.toBinaryString(shifted));
        
        // 안전한 시프트 연산을 위한 검사
        if (value > Integer.MAX_VALUE >> 1) {
            System.out.println("경고: 시프트 연산 시 오버플로우 발생 가능");
        }
    }
}
```

### 2. 부호 확장 주의
오른쪽 시프트 연산 시 부호 확장에 주의해야 합니다.

```java
public class SignExtensionExample {
    public static void main(String[] args) {
        byte b = -1; // 11111111
        int i = b;   // 11111111111111111111111111111111 (부호 확장)
        
        System.out.println("byte -1: " + Integer.toBinaryString(b & 0xFF));
        System.out.println("int로 변환: " + Integer.toBinaryString(i));
        
        // 부호 확장 방지
        int unsignedByte = b & 0xFF; // 255
        System.out.println("부호 확장 방지: " + unsignedByte);
        
        // 오른쪽 시프트에서의 부호 확장
        int negative = -8;
        System.out.println("원본: " + Integer.toBinaryString(negative));
        System.out.println(">> 2: " + Integer.toBinaryString(negative >> 2));
        System.out.println(">>> 2: " + Integer.toBinaryString(negative >>> 2));
    }
}
```

## 결론

Java의 비트 연산은 다음과 같은 장점을 제공합니다:

1. **성능**: CPU에서 직접 지원하는 가장 빠른 연산
2. **메모리 효율성**: 플래그나 집합 연산에서 메모리 사용량 최소화
3. **암호화**: XOR 연산을 활용한 간단한 암호화
4. **알고리즘 최적화**: 해시 함수, 거듭제곱 등에서 성능 향상

하지만 다음과 같은 주의사항도 있습니다:

1. **가독성**: 비트 연산은 코드의 가독성을 떨어뜨릴 수 있음
2. **오버플로우**: 시프트 연산 시 오버플로우 위험
3. **부호 확장**: 음수에서의 예상치 못한 동작

비트 연산은 성능이 중요한 시스템 프로그래밍, 게임 개발, 암호화, 압축 알고리즘 등에서 특히 유용하며, 적절히 활용하면 프로그램의 성능과 메모리 효율성을 크게 향상시킬 수 있습니다.