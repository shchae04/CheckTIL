# 진법 (Number Systems): 2진수, 8진수, 10진수, 16진수

## 1. 개요

### 1-1. 진법이란?
- **정의**: 수를 표현하는 방법으로, 기수(base/radix)에 따라 자릿값이 결정되는 체계
- **중요성**: 컴퓨터는 2진수로 동작하며, 프로그래밍에서 다양한 진법 이해가 필수적
- **표기법**: 
  - 2진수(Binary): `0b` 또는 `0B` 접두사 (예: 0b1010)
  - 8진수(Octal): `0o` 또는 `0` 접두사 (예: 0o12)
  - 10진수(Decimal): 접두사 없음 (예: 10)
  - 16진수(Hexadecimal): `0x` 또는 `0X` 접두사 (예: 0xA)

## 2. 각 진법의 특징

### 2-1. 2진수 (Binary - Base 2)

#### 기본 개념
- **사용 숫자**: 0, 1
- **자릿값**: 2의 거듭제곱 (2⁰, 2¹, 2², 2³, ...)
- **용도**: 컴퓨터의 기본 데이터 표현 방식 (비트)

#### 특징
```
2진수: 1011₂
= 1×2³ + 0×2² + 1×2¹ + 1×2⁰
= 8 + 0 + 2 + 1
= 11₁₀
```

#### 실무 활용
- **비트 연산**: AND, OR, XOR, NOT, Shift 연산
- **플래그 관리**: 권한 설정, 상태 표시
- **메모리 주소**: 직접적인 하드웨어 제어

```java
// 비트 플래그 예제
public class Permission {
    public static final int READ = 0b0001;    // 1
    public static final int WRITE = 0b0010;   // 2
    public static final int EXECUTE = 0b0100; // 4
    public static final int DELETE = 0b1000;  // 8
    
    public static void main(String[] args) {
        // 읽기 + 쓰기 권한
        int permission = READ | WRITE;  // 0b0011 = 3
        
        // 권한 확인
        boolean canRead = (permission & READ) != 0;   // true
        boolean canExecute = (permission & EXECUTE) != 0; // false
        
        // 권한 추가
        permission |= EXECUTE;  // 0b0111 = 7
        
        // 권한 제거
        permission &= ~WRITE;   // 0b0101 = 5
    }
}
```

### 2-2. 8진수 (Octal - Base 8)

#### 기본 개념
- **사용 숫자**: 0, 1, 2, 3, 4, 5, 6, 7
- **자릿값**: 8의 거듭제곱 (8⁰, 8¹, 8², 8³, ...)
- **용도**: Unix/Linux 파일 권한 표현, 2진수 간략 표현

#### 특징
```
8진수: 17₈
= 1×8¹ + 7×8⁰
= 8 + 7
= 15₁₀
```

#### 실무 활용
- **파일 권한**: Linux/Unix chmod 명령
- **2진수 그룹화**: 3비트씩 묶어서 표현 (2³ = 8)

```bash
# Linux 파일 권한 (rwx r-x r--)
# rwx = 111₂ = 7₈  (소유자)
# r-x = 101₂ = 5₈  (그룹)
# r-- = 100₂ = 4₈  (기타)
chmod 754 file.txt

# 각 숫자의 의미
# 7 = 4(read) + 2(write) + 1(execute)
# 5 = 4(read) + 0(write) + 1(execute)
# 4 = 4(read) + 0(write) + 0(execute)
```

```java
// Java에서 8진수 사용
public class OctalExample {
    public static void main(String[] args) {
        int octal = 0o755;  // 8진수 755
        System.out.println(octal);  // 493 (10진수)
        
        // 2진수로 변환하면: 111 101 101
        System.out.println(Integer.toBinaryString(octal)); // 111101101
    }
}
```

### 2-3. 10진수 (Decimal - Base 10)

#### 기본 개념
- **사용 숫자**: 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
- **자릿값**: 10의 거듭제곱 (10⁰, 10¹, 10², 10³, ...)
- **용도**: 일상생활과 일반적인 계산에서 사용하는 기본 진법

#### 특징
```
10진수: 123₁₀
= 1×10² + 2×10¹ + 3×10⁰
= 100 + 20 + 3
= 123
```

#### 실무 활용
- **사용자 인터페이스**: 모든 UI에서 기본적으로 사용
- **비즈니스 로직**: 금액, 수량 등 일반적인 계산
- **데이터베이스**: 일반적인 숫자 저장 및 연산

### 2-4. 16진수 (Hexadecimal - Base 16)

#### 기본 개념
- **사용 숫자**: 0-9, A-F (A=10, B=11, C=12, D=13, E=14, F=15)
- **자릿값**: 16의 거듭제곱 (16⁰, 16¹, 16², 16³, ...)
- **용도**: 메모리 주소, 색상 코드, 바이트 데이터 표현

#### 특징
```
16진수: 2F₁₆
= 2×16¹ + 15×16⁰
= 32 + 15
= 47₁₀
```

#### 실무 활용
- **색상 코드**: CSS, 디자인 (RGB)
- **메모리 주소**: 디버깅, 포인터
- **바이트 표현**: 2진수 4비트씩 묶어서 표현 (2⁴ = 16)
- **해시값**: MD5, SHA 등

```java
// 16진수 활용 예제
public class HexExample {
    public static void main(String[] args) {
        // 색상 코드
        int red = 0xFF0000;     // 빨강: RGB(255, 0, 0)
        int green = 0x00FF00;   // 초록: RGB(0, 255, 0)
        int blue = 0x0000FF;    // 파랑: RGB(0, 0, 255)
        
        // RGB 분리
        int r = (red >> 16) & 0xFF;  // 255
        int g = (red >> 8) & 0xFF;   // 0
        int b = red & 0xFF;          // 0
        
        System.out.printf("Red: %d, Green: %d, Blue: %d%n", r, g, b);
        
        // 메모리 덤프 형식 출력
        byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F}; // "Hello"
        for (byte b : data) {
            System.out.printf("0x%02X ", b);
        }
        // 출력: 0x48 0x65 0x6C 0x6C 0x6F
    }
}
```

## 3. 진법 변환 방법

### 3-1. 10진수 → 다른 진법

#### 나눗셈 방법
```
10진수 13을 2진수로 변환:
13 ÷ 2 = 6 ... 1
 6 ÷ 2 = 3 ... 0
 3 ÷ 2 = 1 ... 1
 1 ÷ 2 = 0 ... 1

아래에서 위로 읽기: 1101₂
```

```java
public class DecimalConverter {
    // 10진수를 n진수로 변환
    public static String decimalToBase(int decimal, int base) {
        if (decimal == 0) return "0";
        
        StringBuilder result = new StringBuilder();
        String digits = "0123456789ABCDEF";
        
        while (decimal > 0) {
            int remainder = decimal % base;
            result.insert(0, digits.charAt(remainder));
            decimal /= base;
        }
        
        return result.toString();
    }
    
    public static void main(String[] args) {
        int num = 255;
        System.out.println("10진수: " + num);
        System.out.println("2진수: " + decimalToBase(num, 2));   // 11111111
        System.out.println("8진수: " + decimalToBase(num, 8));   // 377
        System.out.println("16진수: " + decimalToBase(num, 16)); // FF
        
        // Java 내장 메서드
        System.out.println(Integer.toBinaryString(num));  // 11111111
        System.out.println(Integer.toOctalString(num));   // 377
        System.out.println(Integer.toHexString(num));     // ff
    }
}
```

### 3-2. 다른 진법 → 10진수

#### 자릿값 곱셈 방법
```
2진수 1101₂를 10진수로:
= 1×2³ + 1×2² + 0×2¹ + 1×2⁰
= 8 + 4 + 0 + 1
= 13₁₀

16진수 FF₁₆를 10진수로:
= 15×16¹ + 15×16⁰
= 240 + 15
= 255₁₀
```

```java
public class BaseToDecimal {
    // n진수 문자열을 10진수로 변환
    public static int baseToDecimal(String number, int base) {
        int result = 0;
        int power = 0;
        
        // 오른쪽부터 처리
        for (int i = number.length() - 1; i >= 0; i--) {
            char digit = number.charAt(i);
            int value;
            
            if (digit >= '0' && digit <= '9') {
                value = digit - '0';
            } else {
                value = Character.toUpperCase(digit) - 'A' + 10;
            }
            
            result += value * Math.pow(base, power);
            power++;
        }
        
        return result;
    }
    
    public static void main(String[] args) {
        System.out.println(baseToDecimal("1101", 2));    // 13
        System.out.println(baseToDecimal("377", 8));     // 255
        System.out.println(baseToDecimal("FF", 16));     // 255
        
        // Java 내장 메서드
        System.out.println(Integer.parseInt("1101", 2));   // 13
        System.out.println(Integer.parseInt("377", 8));    // 255
        System.out.println(Integer.parseInt("FF", 16));    // 255
    }
}
```

### 3-3. 2진수 ↔ 8진수/16진수 (빠른 변환)

#### 2진수 → 8진수 (3비트씩 묶기)
```
2진수: 11010110₂

3비트씩 그룹화: 011 010 110
각각 8진수로:    3   2   6

결과: 326₈
```

#### 2진수 → 16진수 (4비트씩 묶기)
```
2진수: 11010110₂

4비트씩 그룹화: 1101 0110
각각 16진수로:   D    6

결과: D6₁₆
```

```java
public class BinaryConversion {
    // 2진수를 8진수로 (3비트씩)
    public static String binaryToOctal(String binary) {
        // 3의 배수로 만들기
        while (binary.length() % 3 != 0) {
            binary = "0" + binary;
        }
        
        StringBuilder octal = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 3) {
            String group = binary.substring(i, i + 3);
            int value = Integer.parseInt(group, 2);
            octal.append(value);
        }
        
        return octal.toString();
    }
    
    // 2진수를 16진수로 (4비트씩)
    public static String binaryToHex(String binary) {
        // 4의 배수로 만들기
        while (binary.length() % 4 != 0) {
            binary = "0" + binary;
        }
        
        StringBuilder hex = new StringBuilder();
        String hexDigits = "0123456789ABCDEF";
        
        for (int i = 0; i < binary.length(); i += 4) {
            String group = binary.substring(i, i + 4);
            int value = Integer.parseInt(group, 2);
            hex.append(hexDigits.charAt(value));
        }
        
        return hex.toString();
    }
    
    public static void main(String[] args) {
        String binary = "11010110";
        System.out.println("2진수: " + binary);
        System.out.println("8진수: " + binaryToOctal(binary));  // 326
        System.out.println("16진수: " + binaryToHex(binary));   // D6
    }
}
```

## 4. 실무 활용 사례

### 4-1. 비트 마스킹 (Bit Masking)

```java
public class BitMaskExample {
    // IPv4 서브넷 마스킹
    public static void main(String[] args) {
        // IP: 192.168.1.100
        // Subnet: 255.255.255.0
        
        int ip = 0xC0A80164;        // 192.168.1.100
        int subnet = 0xFFFFFF00;    // 255.255.255.0
        
        int network = ip & subnet;  // 192.168.1.0
        int host = ip & ~subnet;    // 0.0.0.100
        
        System.out.printf("IP: %d.%d.%d.%d%n",
            (ip >> 24) & 0xFF,
            (ip >> 16) & 0xFF,
            (ip >> 8) & 0xFF,
            ip & 0xFF
        );
        
        System.out.printf("Network: %d.%d.%d.%d%n",
            (network >> 24) & 0xFF,
            (network >> 16) & 0xFF,
            (network >> 8) & 0xFF,
            network & 0xFF
        );
    }
}
```

### 4-2. 색상 처리

```java
public class ColorUtils {
    // RGB 색상 합성 및 분리
    public static int createColor(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }
    
    public static int[] extractRGB(int color) {
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return new int[]{r, g, b};
    }
    
    public static void main(String[] args) {
        // 보라색 만들기
        int purple = createColor(128, 0, 128);
        System.out.printf("보라색: 0x%06X%n", purple); // 0x800080
        
        // RGB 분리
        int[] rgb = extractRGB(0xFF5733);
        System.out.printf("R: %d, G: %d, B: %d%n", rgb[0], rgb[1], rgb[2]);
        // R: 255, G: 87, B: 51
    }
}
```

### 4-3. 데이터 압축 및 인코딩

```java
public class DataEncoding {
    // 여러 작은 값을 하나의 int에 저장 (비트 패킹)
    public static void main(String[] args) {
        // 게임 캐릭터 상태: level(8bit), health(8bit), mana(8bit), status(8bit)
        int level = 99;
        int health = 85;
        int mana = 120;
        int status = 0b00001111; // 여러 상태 플래그
        
        // 하나의 int로 패킹
        int packed = (level << 24) | (health << 16) | (mana << 8) | status;
        System.out.printf("Packed: 0x%08X%n", packed);
        
        // 언패킹
        int unpackedLevel = (packed >> 24) & 0xFF;
        int unpackedHealth = (packed >> 16) & 0xFF;
        int unpackedMana = (packed >> 8) & 0xFF;
        int unpackedStatus = packed & 0xFF;
        
        System.out.printf("Level: %d, Health: %d, Mana: %d, Status: 0x%02X%n",
            unpackedLevel, unpackedHealth, unpackedMana, unpackedStatus);
    }
}
```

## 5. 음수 표현 (보수)

### 5-1. 1의 보수 (One's Complement)
- **방법**: 모든 비트를 반전 (0→1, 1→0)
- **문제점**: +0과 -0이 두 개 존재

```
5의 1의 보수:
  5₁₀ = 0101₂
 -5₁₀ = 1010₂ (모든 비트 반전)
```

### 5-2. 2의 보수 (Two's Complement)
- **방법**: 1의 보수 + 1
- **장점**: 덧셈 회로만으로 뺄셈 가능, 0이 하나만 존재
- **현대 컴퓨터의 표준 방식**

```
-5의 2의 보수 (8비트):
  5₁₀ = 00000101₂
1의 보수 = 11111010₂
2의 보수 = 11111011₂ (1의 보수 + 1)

검증: 00000101₂ + 11111011₂ = 100000000₂ (오버플로우 무시) = 0
```

```java
public class TwoComplement {
    public static void main(String[] args) {
        byte positive = 5;     // 00000101
        byte negative = -5;    // 11111011 (2의 보수)
        
        System.out.println("5: " + Integer.toBinaryString(positive & 0xFF));
        System.out.println("-5: " + Integer.toBinaryString(negative & 0xFF));
        
        // Java에서는 자동으로 2의 보수 사용
        System.out.println(positive + negative);  // 0
        
        // 수동 2의 보수 계산
        int num = 5;
        int twoComplement = (~num) + 1;  // 1의 보수 + 1
        System.out.println("Manual 2's complement: " + twoComplement); // -5
    }
}
```

## 6. 정리 및 비교표

### 6-1. 진법 비교표

| 10진수 | 2진수 | 8진수 | 16진수 |
|--------|-------|-------|--------|
| 0 | 0000 | 0 | 0 |
| 1 | 0001 | 1 | 1 |
| 2 | 0010 | 2 | 2 |
| 3 | 0011 | 3 | 3 |
| 4 | 0100 | 4 | 4 |
| 5 | 0101 | 5 | 5 |
| 6 | 0110 | 6 | 6 |
| 7 | 0111 | 7 | 7 |
| 8 | 1000 | 10 | 8 |
| 9 | 1001 | 11 | 9 |
| 10 | 1010 | 12 | A |
| 11 | 1011 | 13 | B |
| 12 | 1100 | 14 | C |
| 13 | 1101 | 15 | D |
| 14 | 1110 | 16 | E |
| 15 | 1111 | 17 | F |
| 16 | 10000 | 20 | 10 |
| 255 | 11111111 | 377 | FF |

### 6-2. 각 진법의 특징 요약

| 진법 | 기수 | 주요 용도 | 비트 그룹 | 실무 예시 |
|------|------|-----------|-----------|-----------|
| 2진수 | 2 | 컴퓨터 내부 표현 | - | 비트 연산, 플래그 |
| 8진수 | 8 | 파일 권한, 간략 표현 | 3비트 | chmod 755 |
| 10진수 | 10 | 일반적인 계산 | - | UI, 비즈니스 로직 |
| 16진수 | 16 | 메모리, 색상, 바이트 | 4비트 | #FF5733, 0xDEADBEEF |

### 6-3. 변환 공식 요약

```
10진수 → n진수: 나눗셈 반복, 나머지를 역순으로
n진수 → 10진수: 각 자리 × 기수^위치 의 합

2진수 ↔ 8진수: 3비트씩 그룹화
2진수 ↔ 16진수: 4비트씩 그룹화
```

## 7. 연습 문제

### 문제 1: 진법 변환
1. 10진수 156을 2진수, 8진수, 16진수로 변환하세요.
2. 2진수 10110111을 8진수, 10진수, 16진수로 변환하세요.

### 문제 2: 비트 연산
다음 권한 시스템을 구현하세요:
- READ(4), WRITE(2), EXECUTE(1) 권한을 비트로 표현
- 사용자에게 READ + WRITE 권한 부여
- EXECUTE 권한 추가
- WRITE 권한 제거

### 문제 3: RGB 색상
16진수 색상 코드 #3A7FBE를 RGB 값으로 분리하고, 각 값을 10진수로 출력하세요.

## 8. 참고 자료

- **Java Documentation**: Integer.toBinaryString, Integer.parseInt
- **비트 연산**: AND(&), OR(|), XOR(^), NOT(~), Shift(<<, >>)
- **실무 응용**: 네트워크 주소 계산, 암호화, 데이터 압축

---

**작성일**: 2025-10-14  
**키워드**: #진법 #2진수 #8진수 #10진수 #16진수 #비트연산 #진법변환 #알고리즘
